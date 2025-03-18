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

  public static void load() {
    // Determine the current operating system and platform to identify the library file name and resource directory

    String osName = System.getProperty("os.name").toLowerCase();
    String userHome = System.getProperty("user.home").toLowerCase();
    String archName;
    String libFileName;
    if (osName.contains("win")) {
      libFileName = Core.WIN_NATIVE_LIBRARY_NAME;
      archName = WIN_AMD64;
    } else if (osName.contains("mac")) {
      libFileName = Core.MACOS_NATIVE_LIBRARY_NAME;
      archName = DARWIN_ARM64;
    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
      libFileName = Core.UNIX_NATIVE_LIBRARY_NAME;
      archName = LINUX_AMD64;
    } else {
      throw new UnsupportedOperationException("Unsupported OS: " + osName);
    }

    // Create the directory for storing the extracted library file, e.g.: lib/win_amd64/
    File libDir = new File("lib");
    if (!libDir.exists()) {
      libDir.mkdirs();
    }
    File archDir = new File(libDir, archName);
    if (!archDir.exists()) {
      archDir.mkdirs();
    }

    // Target library file path
    File libFile = new File(archDir, libFileName);
    // If the file does not exist, copy it from the jar resources to the local directory
    if (!libFile.exists()) {
      extractResource(userHome + "/lib/" + archName + "/" + libFileName, libFile);
    }

    // If the OS is Windows, additional dependent DLL files need to be loaded
    if (WIN_AMD64.equals(archName)) {
      String[] dlls = { "avutil-59.dll", "swresample-5.dll", "libmp3lame.DLL", "avcodec-61.dll", "avformat-61.dll" };
      for (String dll : dlls) {
        File dllFile = new File(archDir, dll);
        if (!dllFile.exists()) {
          extractResource("/lib/" + archName + "/" + dll, dllFile);
        }
      }
      // The order of loading DLLs must not be changed
      for (String dll : dlls) {
        System.load(new File(archDir, dll).getAbsolutePath());
      }
    }

    // Load the main library file
    String absolutePath = libFile.getAbsolutePath();
    System.out.println("load " + absolutePath);
    System.load(absolutePath);
  }

  /**
   * Copies a resource file from the jar to the specified destination.
   *
   * @param resourcePath The resource path inside the jar, e.g.: /lib/win_amd64/xxx.dll
   * @param destination  The destination file
   */
  private static void extractResource(String resourcePath, File destination) {
    try (InputStream in = LibraryUtils.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new RuntimeException("Resource does not exist: " + resourcePath);
      }
      Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to extract resource: " + resourcePath + " to " + destination.getAbsolutePath(), e);
    }
  }
}
