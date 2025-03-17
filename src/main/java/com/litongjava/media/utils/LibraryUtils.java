package com.litongjava.media.utils;

import java.io.File;

import com.litongjava.media.core.Core;

public class LibraryUtils {

  public static void load() {
    String osName = System.getProperty("os.name").toLowerCase();
    String libFileName;
    if (osName.contains("win")) {
      libFileName = Core.WIN_NATIVE_LIBRARY_NAME;
    } else if (osName.contains("mac")) {
      libFileName = Core.MACOS_NATIVE_LIBRARY_NAME;

    } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
      libFileName = Core.UNIX_NATIVE_LIBRARY_NAME;
    } else {
      throw new UnsupportedOperationException("Unsupported OS：" + osName);
    }

    File libDir = new File("lib");
    if (!libDir.exists()) {
      libDir.mkdirs();
    }

    File libFile = new File(libDir, libFileName);
    if (!libFile.exists()) {
      throw new RuntimeException("Not foud libiary：" + libFile.getAbsolutePath());
    }

    System.load(libFile.getAbsolutePath());
  }
}
