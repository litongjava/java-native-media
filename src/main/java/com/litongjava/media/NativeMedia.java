package com.litongjava.media;

import com.litongjava.media.utils.LibraryUtils;

public class NativeMedia {
  static {
    LibraryUtils.load();
  }

  /**
   * init load lib
   */
  public static void init() {

  }

  /**
   * Splits an MP3 file into smaller MP3 files of specified size
   * 
   * @param srcPath Path to the source MP3 file
   * @param size Maximum size in bytes for each output file
   * @return Array of paths to the generated MP3 files
   */
  public static native String[] splitMp3(String srcPath, long size);

  /**
   * Converts an MP4 video file to an MP3 audio file
   * 
   * @param inputPath Path to the input MP4 file
   * @return Path to the output MP3 file on success, or error message on failure
   */
  public static native String mp4ToMp3(String inputPath);

  public static native String toMp3(String inputPath);

  public static native String convertTo(String inputPath,String targetFormat);

  public static native String[] supportFormats();

}