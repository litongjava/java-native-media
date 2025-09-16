package com.litongjava.media.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    // Determine the current operating system and platform to identify the library
    // file name and resource directory

    String osName = System.getProperty("os.name").toLowerCase();
    String userHome = System.getProperty("user.home").toLowerCase();
    System.out.println("os name:" + osName + " user.home:" + userHome);
    String archName;
    String libFileName;
    if (osName.contains("win")) {
      libFileName = Core.WIN_NATIVE_LIBRARY_NAME;
      archName = WIN_AMD64;
    } else if (osName.contains("mac")) {
      libFileName = Core.MACOS_NATIVE_LIBRARY_NAME;
      archName = DARWIN_ARM64;
    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix") || osName.contains("linux")) {
      libFileName = Core.UNIX_NATIVE_LIBRARY_NAME;
      archName = LINUX_AMD64;
    } else {
      throw new UnsupportedOperationException("Unsupported OS: " + osName);
    }

    // Create the directory for storing the extracted library file, e.g.:
    // lib/win_amd64/
    String dstDir = userHome + File.separator + "lib" + File.separator + archName;
    File libFile = new File(dstDir, libFileName);

    File parentDir = libFile.getParentFile();
    if (!parentDir.exists()) {
      parentDir.mkdirs();
    }
    // Now extract the resource
    if (Core.UNIX_NATIVE_LIBRARY_NAME.equals(libFileName)) {
      libFile = new File(dstDir, Core.UNIX_NATIVE_LIBRARY_NAME_59);
      extractResource("/lib/" + archName + "/" + Core.UNIX_NATIVE_LIBRARY_NAME_59, libFile);
      libFile = new File(dstDir, Core.UNIX_NATIVE_LIBRARY_NAME_61);
      extractResource("/lib/" + archName + "/" + Core.UNIX_NATIVE_LIBRARY_NAME_61, libFile);

    } else {
      extractResource("/lib/" + archName + "/" + libFileName, libFile);
    }

    // If the OS is Windows, additional dependent DLL files need to be loaded
    if (WIN_AMD64.equals(archName)) {

      for (String dll : dlls) {
        File dllFile = new File(dstDir, dll);
        extractResource("/lib/" + archName + "/" + dll, dllFile);
        System.load(dllFile.getAbsolutePath());
      }
    }

    // Load the main library file
    String absolutePath = libFile.getAbsolutePath();

    if (Core.UNIX_NATIVE_LIBRARY_NAME.equals(libFileName)) {
      libFile = new File(dstDir, Core.UNIX_NATIVE_LIBRARY_NAME_59);
      absolutePath = libFile.getAbsolutePath();
      System.out.println("load " + absolutePath);
      try {
        System.load(absolutePath);
      } catch (UnsatisfiedLinkError e) {
        System.out.println("failed to load " + absolutePath);
        libFile = new File(dstDir, Core.UNIX_NATIVE_LIBRARY_NAME_61);
        absolutePath = libFile.getAbsolutePath();
        System.out.println("load " + absolutePath);
        System.load(absolutePath);
      }
    } else {
      System.load(absolutePath);
    }
  }

  /**
   * Copies a resource file from the jar to the specified destination.
   *
   * @param resourcePath The resource path inside the jar, e.g.:
   *                     /lib/win_amd64/xxx.dll
   * @param destination  The destination file
   */
  private static void extractResource(String resourcePath, File destination) {
    System.out.println("copy from " + resourcePath + " to " + destination.getAbsolutePath());
    try (InputStream in = LibraryUtils.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new RuntimeException("Resource does not exist: " + resourcePath);
      }
      Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to extract resource: " + resourcePath + " to " + destination.getAbsolutePath(),
          e);
    }
  }
}
