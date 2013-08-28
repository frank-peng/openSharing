package com.share.server;

public class Main {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String rootDir =
        (Configuration.isWindows ? "C:\\tmp\\syncedroot" : "/tmp/syncedroot");
    Configuration.rootPath = rootDir.getBytes();

    ServerNetworkMgr netMgr = new ServerNetworkMgr();
    netMgr.run();
  }
}
