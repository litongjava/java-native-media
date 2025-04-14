package com.litongjava.media;

import org.junit.Test;

public class AddWatermarkToVideoTest {

  @Test
  public void test() {
    String video = "G:\\manim\\main.mp4";
    String output = "G:\\manim\\main_video_tutor.mp4";
    NativeMedia.addWatermarkToVideo(video, output, "Video Tutor", null);
  }
}