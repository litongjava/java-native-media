package com.litongjava.media;

public class Mp4ToMp3Test {
  public static void main(String[] args) {
    String inputPath="E:\\code\\cpp\\project-ping\\native-media\\samples\\01.mp4";
    String result = NativeMedia.mp4ToMp3(inputPath);
    if (result.startsWith("Error:")) {
      System.out.println("Conversion failed: " + result);
    } else {
      System.out.println("Conversion successful! Output file: " + result);
    }
  }
}