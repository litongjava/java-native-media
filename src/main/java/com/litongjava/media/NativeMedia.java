package com.litongjava.media;

import com.litongjava.media.utils.LibraryUtils;

public class NativeMedia {
  static {
    LibraryUtils.load();
  }

  /**
   * Initializes and loads the library
   */
  public static void init() {

  }

  /**
   * Splits an MP3 file into smaller MP3 files of specified size.
   * 
   * @param srcPath Path to the source MP3 file
   * @param size Maximum size in bytes for each output file
   * @return Array of paths to the generated MP3 files
   */
  public static native String[] splitMp3(String srcPath, long size);

  /**
   * Converts an MP4 video file to an MP3 audio file.
   * 
   * @param inputPath Path to the input MP4 file
   * @return Path to the output MP3 file on success, or an error message on failure
   */
  public static native String mp4ToMp3(String inputPath);

  public static native String toMp3(String inputPath);

  public static native String convertTo(String inputPath, String targetFormat);

  public static native String[] split(String srcPath, long size);

  public static native String[] supportFormats();

  /**
   * Native method to be implemented by C.
   *
   * The implementation should include:
   * 1. Using the native‑media library to convert the MP4 file specified by inputMp4Path into HLS segments,
   *    with each segment lasting segmentDuration seconds, and the starting segment number determined by sceneIndex.
   * 2. Naming the generated TS segment files according to tsPattern, and storing them in the same directory
   *    as the playlistUrl file.
   * 3. Generating an m3u8 segment based on the converted TS segment information (including the #EXTINF tag
   *    and the TS file name for each segment).
   * 4. Appending the generated m3u8 segment content to the playlist file specified by playlistUrl (ensure that
   *    the playlist does not contain the #EXT-X-ENDLIST tag, otherwise the append will be ineffective).
   *
   * @param hlsPath   Full path to the playlist file
   * @param inputMp4Path  Path to the input MP4 file
   * @param tsPattern     Naming template for TS segment files (including the directory path)
   * @param sceneIndex    Current scene index (starting segment number for conversion)
   * @param segmentDuration Segment duration in seconds
   */
  public static native String splitVideoToHLS(String hlsPath, String inputMp4Path, String tsPattern, int segmentDuration);

  /**
   * 初始化持久化 HLS 会话，返回一个表示会话的 native 指针
   * @param playlistUrl HLS 播放列表保存路径，如 "./data/hls/test/playlist.m3u8"
   * @param tsPattern TS 分段文件命名模板，如 "./data/hls/test/segment_%03d.ts"
   * @param startNumber 起始分段编号
   * @param segmentDuration 分段时长（秒）
   * @return 会话指针（long 类型），后续操作需要传入该指针
   */
  public static native long initPersistentHls(String playlistUrl, String tsPattern, int startNumber, int segmentDuration);

  /**
   * 追加一个 MP4 分段到指定的 HLS 会话中
   * @param sessionPtr 会话指针（由 initPersistentHls 返回）
   * @param inputMp4Path 输入 MP4 文件路径
   * @return 状态信息
   */
  public static native String appendVideoSegmentToHls(long sessionPtr, String inputMp4Path);

  /**
   * 在当前音频 HLS 会话中插入一个静音段  
   * 静音段的时长由 duration 指定，单位为秒。
   *
   * @param sessionPtr 会话指针（由 initPersistentHls 返回）
   * @param duration 静音段时长（秒）
   * @return 状态信息
   */
  public static native String insertSilentSegment(long sessionPtr, double duration);

  /**
   * 结束指定的 HLS 会话，写入 EXT‑X‑ENDLIST 并关闭输出，同时释放会话资源
   * @param sessionPtr 会话指针（由 initPersistentHls 返回）
   * @param playlistUrl 播放列表路径
   * @return 状态信息
   */
  public static native String finishPersistentHls(long sessionPtr, String playlistUrl);

  /**
   * Merges multiple video/audio files into a single output file using stream copy.
   * This method calls a native C function that utilizes the FFmpeg command-line tool.
   * The input files should ideally have compatible stream parameters (codec, resolution, etc.)
   * for stream copy to work reliably and efficiently.
   *
   * @param inputPaths An array of absolute paths to the input media files.
   * @param outputPath The absolute path for the merged output media file.
   * @return true if the merging process initiated by FFmpeg completes successfully (exit code 0), false otherwise.
   * @throws NullPointerException if inputPaths or outputPath is null, or if inputPaths contains null elements.
   * @throws IllegalArgumentException if inputPaths contains fewer than 2 files.
   */
  public static native boolean merge(String[] inputPaths, String outputPath);

}
