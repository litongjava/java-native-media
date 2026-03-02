package com.litongjava.media.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LibraryUtils {
  private static final String LINUX_AMD64 = "linux_amd64";
  private static final String[] UNIX_CANDIDATES = { "libnative_media_av59.so", "libnative_media_av61.so" };
  private static final String[] DEPENDENCIES = { "libavformat.so", "libavcodec.so", "libavutil.so", "libswresample.so",
      "libswscale.so", "libavfilter.so" };

  public static void load() {
    String osName = System.getProperty("os.name").toLowerCase();
    boolean isLinux = osName.contains("nux") || osName.contains("nix") || osName.contains("linux");
    String archName = LINUX_AMD64;
    String tmpBase = System.getProperty("java.io.tmpdir");

    if (!isLinux) {
      throw new UnsupportedOperationException("This example focuses on linux.");
    }

    // 1) If system already has a compatible lib (try ldconfig), prefer system
    String found = findSystemLib("libavformat");
    if (found != null) {
      // system has libavformat.* — we can try to rely on system loader
      // Optionally preload some deps if full paths found:
      tryPreloadDeps(found);
    }

    // 2) Extract candidate libs to a safe tmp dir per JVM/per-run
    File dstDir = new File(tmpBase,
        "myapp-native-" + System.getProperty("user.name") + "-" + System.getProperty("user.dir").hashCode());
    if (!dstDir.exists())
      dstDir.mkdirs();

    // 3) Extract all candidates (if resources exist) to dstDir
    for (String candidate : UNIX_CANDIDATES) {
      File f = new File(dstDir, candidate);
      if (!f.exists()) {
        try {
          extractResource("/lib/" + archName + "/" + candidate, f);
          // ensure readable
          f.setReadable(true, false);
        } catch (RuntimeException ex) {
          // resource might not be bundled; ignore and continue
        }
      }
    }

    // 4) Try to load each candidate in order; if one fails, try next.
    UnsatisfiedLinkError lastErr = null;
    for (String candidate : UNIX_CANDIDATES) {
      File libFile = new File(dstDir, candidate);
      if (!libFile.exists())
        continue;
      try {
        // Optionally preload known dependency absolute files if present in same dir
        preloadLocalDeps(dstDir);
        System.load(libFile.getAbsolutePath());
        System.out.println("Loaded native library: " + libFile.getAbsolutePath());
        return;
      } catch (UnsatisfiedLinkError e) {
        lastErr = e;
        System.err.println("Failed to load " + libFile.getAbsolutePath() + " -> " + e.getMessage());
        // continue to next candidate
      }
    }

    // 5) If still not loaded, rethrow an informative error
    if (lastErr != null) {
      throw lastErr;
    } else {
      throw new RuntimeException("No suitable native library found (tried candidates).");
    }
  }

  private static void preloadLocalDeps(File dir) {
    for (String dep : DEPENDENCIES) {
      // attempt to find a file like libavformat.so.59 or libavformat.so.61 in dir
      File[] matches = dir.listFiles((d, name) -> name.startsWith(dep));
      if (matches != null && matches.length > 0) {
        for (File m : matches) {
          try {
            System.load(m.getAbsolutePath());
            System.out.println("Preloaded dependency: " + m.getAbsolutePath());
          } catch (UnsatisfiedLinkError ignored) {
            // try next; it's fine if preload fails — main load may still work if system has
            // them
          }
        }
      }
    }
  }

  // Try to preload from a system absolute path (found via ldconfig -p output)
  private static void tryPreloadDeps(String libavformatPath) {
    // libavformatPath might be like "/lib/x86_64-linux-gnu/libavformat.so.59"
    File lib = new File(libavformatPath);
    if (lib.exists()) {
      try {
        System.load(lib.getAbsolutePath());
        System.out.println("Preloaded system lib: " + lib.getAbsolutePath());
      } catch (UnsatisfiedLinkError e) {
        // ignore; will try other ways
      }
    }
  }

  // Tries to discover system library via ldconfig -p
  private static String findSystemLib(String shortName) {
    try {
      Process p = new ProcessBuilder("ldconfig", "-p").redirectErrorStream(true).start();
      try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
        String line;
        while ((line = r.readLine()) != null) {
          if (line.contains(shortName + ".so")) {
            // parse path at the end
            int idx = line.lastIndexOf(" => ");
            if (idx > 0) {
              return line.substring(idx + 4).trim();
            }
          }
        }
      }
    } catch (IOException ignored) {
    }
    return null;
  }

  private static void extractResource(String resourcePath, File destination) {
    try (InputStream in = LibraryUtils.class.getResourceAsStream(resourcePath)) {
      if (in == null)
        throw new RuntimeException("Resource not found: " + resourcePath);
      Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}