package com.litongjava.media.utils;

import java.io.IOException;

import org.junit.Test;

public class VideoWaterUtilsTest {

  @Test
  public void test() {
    String video = "G:\\manim\\main.mp4";
    String output = "G:\\manim\\main_video_tutor.mp4";
    try {
      VideoWaterUtils.addWatermark(video, output, 24, "videotutor.io");
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

}
