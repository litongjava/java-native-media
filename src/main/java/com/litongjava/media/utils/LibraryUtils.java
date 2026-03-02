package com.litongjava.media.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.litongjava.media.core.Core;

public class LibraryUtils {

  public static final String WIN_AMD64 = "win_amd64";
  public static final String DARWIN_ARM64 = "darwin_arm64";
  public static final String LINUX_AMD64 = "linux_amd64";

  public static final String[] dlls = { "avutil-59.dll", "swresample-5.dll", "libmp3lame.DLL", "avcodec-61.dll",
      "avformat-61.dll", "swscale-8.dll", "avfilter-10.dll" };

  public static void load() {

    String osName = System.getProperty("os.name").toLowerCase();
    String userHome = System.getProperty("user.home");

    System.out.println("os name: " + osName + " user.home: " + userHome);

    String archName;
    String libFileName = null;

    if (osName.contains("win")) {
      libFileName = Core.WIN_NATIVE_LIBRARY_NAME;
      archName = WIN_AMD64;

    } else if (osName.contains("mac")) {
      libFileName = Core.MACOS_NATIVE_LIBRARY_NAME;
      archName = DARWIN_ARM64;

    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix") || osName.contains("linux")) {
      archName = LINUX_AMD64;

    } else {
      throw new UnsupportedOperationException("Unsupported OS: " + osName);
    }

    String dstDir = userHome + File.separator + "lib" + File.separator + archName;
    File dir = new File(dstDir);
    if (!dir.exists())
      dir.mkdirs();

    // ================= WINDOWS =================
    if (WIN_AMD64.equals(archName)) {

      File libFile = new File(dstDir, libFileName);
      extractResource("/lib/" + archName + "/" + libFileName, libFile);

      for (String dll : dlls) {
        File dllFile = new File(dstDir, dll);
        extractResource("/lib/" + archName + "/" + dll, dllFile);
        System.load(dllFile.getAbsolutePath());
      }

      System.load(libFile.getAbsolutePath());
      return;
    }

    // ================= MAC =================
    if (DARWIN_ARM64.equals(archName)) {
      File libFile = new File(dstDir, libFileName);
      extractResource("/lib/" + archName + "/" + libFileName, libFile);
      System.load(libFile.getAbsolutePath());
      return;
    }

    // ================= LINUX =================
    int abi = detectFFmpegAbi();

    if (abi <= 0) {
      throw new RuntimeException("Cannot detect FFmpeg (libavformat) version via ldconfig");
    }

    System.out.println("Detected FFmpeg ABI: " + abi);

    // 拼接库名
    String target = Core.UNIX_NATIVE_LIBRARY_PREFIX + abi + ".so";

    File soFile = new File(dstDir, target);
    extractResource("/lib/" + archName + "/" + target, soFile);

    System.out.println("Loading native: " + soFile.getAbsolutePath());
    System.load(soFile.getAbsolutePath());
  }

  // ================= FFmpeg ABI 检测 =================
  private static int detectFFmpegAbi() {
    try {
      Process p = new ProcessBuilder("ldconfig", "-p").start();

      try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

        String line;
        while ((line = r.readLine()) != null) {
          int idx = line.indexOf("libavformat.so.");
          if (idx >= 0) {
            String ver = line.substring(idx + "libavformat.so.".length()).trim();
            int space = ver.indexOf(' ');
            if (space > 0)
              ver = ver.substring(0, space);
            return Integer.parseInt(ver);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return -1;
  }

  private static void extractResource(String resourcePath, File destination) {
    if (destination.exists())
      return;

    System.out.println("copy from " + resourcePath + " to " + destination.getAbsolutePath());

    try (InputStream in = LibraryUtils.class.getResourceAsStream(resourcePath)) {

      if (in == null) {
        throw new RuntimeException("Resource does not exist: " + resourcePath);
      }

      Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException e) {
      throw new RuntimeException("Failed to extract resource: " + resourcePath, e);
    }
  }
}