package com.share.client;

import com.share.protocol.SyncedLocalRequest;
import com.share.server.Configuration;

public class Main {

  private static class ClientTest extends Thread {
    ClientNetworkMgr mgr;

    public ClientTest(ClientNetworkMgr mgr) {
      super();
      this.mgr = mgr;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(2000);
        SyncedLocalRequest localReq = new SyncedLocalRequest();
        localReq.setCode(SyncedLocalRequest.RequestCode.SYNC_DIR);
        localReq.setValue(new String(Configuration.rootPath));
        mgr.getStateMachine().addLocalRequest(localReq);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String rootDir =
        (Configuration.isWindows ? "C:\\tmp\\syncedclient"
            : "/tmp/syncedclient");
    Configuration.rootPath = rootDir.getBytes();

    ClientNetworkMgr netMgr = new ClientNetworkMgr();
    ClientTest testThread = new ClientTest(netMgr);
    testThread.start();
    netMgr.run();
  }
}
