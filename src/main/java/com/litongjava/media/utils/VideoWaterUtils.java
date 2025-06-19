package com.litongjava.media.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoWaterUtils {

  public static final String LOG_FOLDER = "ffmpeg_logs";
  static {
    new File(LOG_FOLDER).mkdirs();
  }

  /**
   * 使用 ffmpeg 给视频添加右下角水印，并将标准输出和错误输出分别写入日志文件。
   *
   * @param inputFile     输入视频文件路径
   * @param outputFile    输出视频文件路径
   * @param fontSize      水印文字的字号
   * @param watermarkText 水印文本
   * @throws IOException          当执行命令时发生 I/O 错误
   * @throws InterruptedException 当线程等待 ffmpeg 进程结束时被中断
   */
  public static int addWatermark(String inputFile, String outputFile, int fontSize, String watermarkText) throws IOException, InterruptedException {

    String osName = System.getProperty("os.name").toLowerCase();
    String fontFile;
    if (osName.contains("win")) {
      fontFile = "C\\:/Windows/Fonts/simhei.ttf";
    } else if (osName.contains("mac")) {
      fontFile = "/Library/Fonts/Arial Unicode.ttf";
    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
      fontFile = "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc";
    } else {
      fontFile = "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc";
    }

    // 构造 drawtext 过滤器参数
    // x=w-tw-10:y=h-th-10 表示让水印距离右下角各留10像素
    String template = "drawtext=fontfile='%s':text='%s':x=w-tw-10:y=h-th-10:fontsize=%d:fontcolor=yellow";
    String filterSpec = String.format(template, fontFile, watermarkText, fontSize);

    // 构造 ffmpeg 命令参数列表
    List<String> command = new ArrayList<>();
    command.add("ffmpeg");
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
    return process.waitFor();
  }
}
