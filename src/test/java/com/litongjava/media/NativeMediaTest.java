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
}