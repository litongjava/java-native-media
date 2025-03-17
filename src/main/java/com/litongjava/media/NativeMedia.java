package com.litongjava.media;

import com.litongjava.media.utils.LibraryUtils;

public class NativeMedia {
  static {
    LibraryUtils.load();
  }

  public static native String[] splitMp3(String srcPath, long size);
}