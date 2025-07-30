package com.litongjava.media.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.litongjava.media.NativeMedia;

/**
 * HlsSessionCleaner 定时任务类
 * 
 * 每隔 1 小时运行一次：
 * 1. 调用 NativeMedia.listHlsSession 获取当前所有活跃的 HLS 会话，返回 JSON 字符串；
 * 2. 使用硬编码提取方式（正则表达式）解析每个会话的 sessionPtr 和 createdTime；
 * 3. 如果某个会话的创建时间距离当前时间超过 1 小时，则调用 NativeMedia.freeHlsSession 关闭该会话。
 */
public class HlsSessionCleaner {
  // 定义日期格式，与 native 层返回的 createdTime 格式一致
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  // 定时任务采用 ScheduledExecutorService
  private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  static {
    // 安排任务：初始延迟 0，之后每小时执行一次
    Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          // 调用 NativeMedia.listHlsSession 获取会话列表，返回 JSON 格式字符串
          String sessionListJson = NativeMedia.listHlsSession();
          // 如果返回值为 "[]" 或者为空，则直接返回
          if (sessionListJson == null || sessionListJson.trim().equals("[]")) {
            return;
          }else {
            System.out.println("Current active sessions: " + sessionListJson);
          }

          // 去掉前后中括号
          sessionListJson = sessionListJson.trim();
          if (sessionListJson.startsWith("[")) {
            sessionListJson = sessionListJson.substring(1);
          }
          if (sessionListJson.endsWith("]")) {
            sessionListJson = sessionListJson.substring(0, sessionListJson.length() - 1);
          }

          // 按 "}," 分割字符串（这里假设每个会话对象都以 '}' 结尾，且对象之间以 "}," 分割）
          String[] sessions = sessionListJson.split("\\},");
          for (String sessionJson : sessions) {
            sessionJson = sessionJson.trim();
            // 如果没有闭合右括号，则补上
            if (!sessionJson.endsWith("}")) {
              sessionJson = sessionJson + "}";
            }

            // 提取 sessionPtr
            Pattern ptrPattern = Pattern.compile("\"sessionPtr\"\\s*:\\s*(-?\\d+)");
            Matcher ptrMatcher = ptrPattern.matcher(sessionJson);
            long sessionPtr = 0;
            if (ptrMatcher.find()) {
              sessionPtr = Long.parseLong(ptrMatcher.group(1));
            }
            // 如果没提取到有效的 sessionPtr，则跳过此项
            if (sessionPtr == 0) {
              System.out.println("Skipped a session because sessionPtr is 0.");
              continue;
            }

            // 提取 createdTime
            Pattern timePattern = Pattern.compile("\"createdTime\"\\s*:\\s*\"([^\"]+)\"");
            Matcher timeMatcher = timePattern.matcher(sessionJson);
            String createdTimeStr = "";
            if (timeMatcher.find()) {
              createdTimeStr = timeMatcher.group(1);
            }
            // 如果 createdTime 字段为空，则跳过该会话
            if (createdTimeStr.isEmpty()) {
              System.out.println("Skipped a session because createdTime is empty for sessionPtr: " + sessionPtr);
              continue;
            }

            // 将 createdTime 字符串转换为 Date 对象
            Date createdDate = DATE_FORMAT.parse(createdTimeStr);
            long createdMillis = createdDate.getTime();
            long nowMillis = System.currentTimeMillis();
            // 判断是否超过 1 小时（此处可调整测试时间，此示例使用 10秒进行测试，正式环境使用 3600000L，即一小时）
            if (nowMillis - createdMillis > 3600000L) {
              //if (nowMillis - createdMillis > 10000) {
              // 调用 NativeMedia.freeHlsSession 释放会话资源
              String result = NativeMedia.freeHlsSession(sessionPtr);
              System.out.println("Closed HLS session " + sessionPtr + ", result: " + result);
            }
          }
        } catch (ParseException e) {
          System.err.println("Failed to parse createdTime: " + e.getMessage());
          e.printStackTrace();
        } catch (Exception e) {
          System.err.println("Exception in HLS session cleaner: " + e.getMessage());
          e.printStackTrace();
        }
      }
    };

    scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    //scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
  }

  public static void start() {
  }
}
