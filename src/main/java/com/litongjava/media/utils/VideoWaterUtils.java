package com.litongjava.media.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VideoWaterUtils {

  public static String linux_font_path = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc";
  public static final String LOG_FOLDER = "ffmpeg_logs";
  static {
    File file = new File(LOG_FOLDER);
    if (!file.exists()) {
      file.mkdirs();
    }
  }

  /**
   * ffmpeg 最长允许跑多久（分钟）。超时会被强制杀掉。
   *
   * <p>为什么需要：这里用的是 ffmpeg 命令行，它会向 stdin 提问（最常见的就是
   * 「File ... already exists. Overwrite? [y/N]」）。ProcessBuilder 默认给子进程的是
   * 一个管道，我们既不写也不关，ffmpeg 一旦去读 stdin 就会**永久阻塞**，
   * waitFor() 也就永远不返回，把调用线程（HTTP 请求线程）一直占住。
   * 2026-09-23 线上就是这么把 java-kit 卡死的，所以除了加 -y，再加一层超时兜底。
   */
  public static int FFMPEG_TIMEOUT_MINUTES = 15;

  /**
   * 使用 ffmpeg 给视频添加右下角水印，并将标准输出和错误输出分别写入日志文件。
   *
   * @param inputFile     输入视频文件路径
   * @param outputFile    输出视频文件路径
   * @param fontSize      水印文字的字号
   * @param watermarkText 水印文本
   * @return ffmpeg 的退出码，0 表示成功；超时被杀返回 -1
   * @throws IOException          当执行命令时发生 I/O 错误
   * @throws InterruptedException 当线程等待 ffmpeg 进程结束时被中断
   */
  public static int addWatermark(String inputFile, String outputFile, int fontSize, String watermarkText)
      throws IOException, InterruptedException {

    String osName = System.getProperty("os.name").toLowerCase();
    String fontFile;
    if (osName.contains("win")) {
      fontFile = "C\\:/Windows/Fonts/simhei.ttf";
    } else if (osName.contains("mac")) {
      fontFile = "/Library/Fonts/Arial Unicode.ttf";
    } else {
      if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
        fontFile = linux_font_path;
      } else {
        fontFile = linux_font_path;
      }
    }

    // 构造 drawtext 过滤器参数
    // x=w-tw-10:y=h-th-10 表示让水印距离右下角各留10像素
    String template = "drawtext=fontfile='%s':text='%s':x=w-tw-10:y=h-th-10:fontsize=%d:fontcolor=black";
    String filterSpec = String.format(template, fontFile, watermarkText, fontSize);

    // 构造 ffmpeg 命令参数列表
    List<String> command = new ArrayList<>();
    command.add("ffmpeg");
    // -y：输出文件已存在时直接覆盖。少了它 ffmpeg 会去读 stdin 问 Overwrite?，
    // 而那个管道没人写也没人关，进程会卡在那里永不返回
    command.add("-y");
    command.add("-i");
    command.add(inputFile);
    command.add("-vf");
    command.add(filterSpec);
    command.add("-codec:a");
    command.add("copy");
    command.add(outputFile);

    System.out.println("cmd：" + String.join(" ", command));

    ProcessBuilder pb = new ProcessBuilder(command);

    // 设置将标准输出和错误输出分别重定向到文件

    File stdoutFile = new File(LOG_FOLDER, "ffmpeg_stdout.log");
    File stderrFile = new File(LOG_FOLDER, "ffmpeg_stderr.log");
    pb.redirectOutput(ProcessBuilder.Redirect.to(stdoutFile));
    pb.redirectError(ProcessBuilder.Redirect.to(stderrFile));

    // 启动进程并等待完成
    Process process = pb.start();

    // 关掉子进程的 stdin：即便将来又出现别的交互式提问，ffmpeg 读到的也是 EOF 而不是永远阻塞
    try {
      process.getOutputStream().close();
    } catch (IOException e) {
      // 关流失败不影响主流程
    }

    // 超时兜底：绝不允许一个卡住的 ffmpeg 把调用线程永久占住
    if (!process.waitFor(FFMPEG_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
      process.destroyForcibly();
      System.err.println("ffmpeg timeout after " + FFMPEG_TIMEOUT_MINUTES + " minutes, killed: "
          + String.join(" ", command));
      return -1;
    }
    return process.exitValue();
  }
}
