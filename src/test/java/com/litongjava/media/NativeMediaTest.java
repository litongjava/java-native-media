package com.litongjava.media;

import org.junit.Test;

public class NativeMediaTest {

  @Test
  public void testSplitMp3() {
    String testFile = "E:\\code\\java\\project-litongjava\\yt-dlp-java\\downloads\\490920099690696704\\01.mp3";
    //long splitSize = 25 * 1024 * 1024; // 25MB
    long splitSize = 10 * 1024 * 1024; // 10MB

    String[] result = NativeMedia.splitMp3(testFile, splitSize);
    for (String string : result) {
      System.out.println(string);
    }
  }

  @Test
  public void supportFormats() {
    String[] supportFormats = NativeMedia.supportFormats();
    for (String string : supportFormats) {
      System.out.println(string);
    }
  }

  @Test
  public void toMp3() {
    String inputFile = "G:\\video\\03.软件开发学习视频\\java\\jbolt\\jbolt内训教程\\input.flv";
    String outputPath = NativeMedia.toMp3(inputFile);
    System.out.println(outputPath);
  }
  
  @Test
  public void convertToMp3() {
    String inputFile = "G:\\video\\input.flv";
    String outputPath = NativeMedia.convertTo(inputFile, "libmp3lame");
    System.out.println(outputPath);
  }
}