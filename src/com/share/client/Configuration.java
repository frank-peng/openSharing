package com.share.client;

public class Configuration {
  public static final int serverPort = 9527;
  public static String serverIP = "127.0.0.1";
  public static byte[] rootPath;
  public static final boolean isWindows = System.getProperty("os.name")
      .contains("Windows");
  public static final String separator = (isWindows ? "\\" : "/");
}
