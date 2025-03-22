package com.litongjava.media;

import java.io.File;

public class Mp4ToMp3Test {
  public static void main(String[] args) {
    String inputPath="E:\\code\\java\\project-litongjava\\java-native-media-test\\upload\\1.MP4";
    File file = new File(inputPath);
    System.out.println(file.exists());
    
    String result = NativeMedia.mp4ToMp3(inputPath);
    if (result.startsWith("Error:")) {
      System.out.println("Conversion failed: " + result);
    } else {
      System.out.println("Conversion successful! Output file: " + result);
    }
  }
}