package com.share.server;

public class Configuration {

  public static final int ListenPort = 9527;
  public static byte[] rootPath;
  public static final boolean isWindows = System.getProperty("os.name")
      .contains("Windows");
  public static final String separator = (isWindows ? "\\" : "/");
}
