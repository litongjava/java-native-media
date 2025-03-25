package com.litongjava.media;

import java.io.File;

import org.junit.Test;

public class NativeMediaTest {

  @Test
  public void testMp4ToMp3() {
    String inputPath = "E:\\code\\java\\project-litongjava\\yt-dlp-java\\downloads\\AMCUqgu_cTM\\mp4\\一口气了解迪拜经济和它奢华表面的背后....mp4";
    File file = new File(inputPath);
    System.out.println(file.exists());

    String result = NativeMedia.mp4ToMp3(inputPath);
    if (result.startsWith("Error:")) {
      System.out.println("Conversion failed: " + result);
    } else {
      System.out.println("Conversion successful! Output file: " + result);
    }
  }

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
    String inputFile = "G:\\video\\input.flv";
    String outputPath = NativeMedia.toMp3(inputFile);
    System.out.println(outputPath);
  }

  @Test
  public void convertToMp3() {
    String inputFile = "G:\\video\\input.flv";
    String outputPath = NativeMedia.convertTo(inputFile, "libmp3lame");
    System.out.println(outputPath);
  }

  @Test
  public void split() {
    String inputFile = "G:\\video\\input.mp3";
    String[] split = NativeMedia.split(inputFile, 10 * 1024 * 1024);
  }
}