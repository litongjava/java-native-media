package com.litongjava.media;

import java.io.File;

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
   * Converts an MP4 file into HLS segments, and appends the generated TS files and m3u8
   * playlist segments to the specified playlist file.
   * Note: The playlistUrl must include the full path, for example "./data/hls/{sessionId}/playlist.m3u8",
   * and the TS segment files will be stored in that directory.
   *
   * @param playlistUrl   Path to the playlist file
   * @param inputMp4Path  Path to the input MP4 file
   * @param sceneIndex    Current scene index (used to determine the starting segment number for conversion)
   * @param segmentDuration Segment duration in seconds
   */
  public static String appendMp4ToHLS(String playlistUrl, String inputMp4Path, int sceneIndex, int segmentDuration) {
    // Extract the directory from the playlistUrl
    File playlistFile = new File(playlistUrl);
    String directory = playlistFile.getParent();

    // Construct the naming pattern for TS segment files, for example: ./data/hls/{sessionId}/segment_%03d.ts
    String tsPattern = directory + "/segment_%03d.ts";

    // Call the native method to complete the conversion from MP4 to HLS and update the playlist
    return splitMp4ToHLS(playlistUrl, inputMp4Path, tsPattern, sceneIndex, segmentDuration);
  }

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
   * @param playlistUrl   Full path to the playlist file
   * @param inputMp4Path  Path to the input MP4 file
   * @param tsPattern     Naming template for TS segment files (including the directory path)
   * @param sceneIndex    Current scene index (starting segment number for conversion)
   * @param segmentDuration Segment duration in seconds
   */
  public static native String splitMp4ToHLS(String playlistUrl, String inputMp4Path, String tsPattern, int sceneIndex, int segmentDuration);
}
