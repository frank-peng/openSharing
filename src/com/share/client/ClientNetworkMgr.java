package com.share.client;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.share.exception.ConnectionException;
import com.share.protocol.BufferedIOStream;
import com.share.protocol.MsgFactory;
import com.share.statemachine.StateMachine;

public class ClientNetworkMgr {
  protected StateMachine serverSm;
  MsgFactory factory;
  Selector s;
  SocketChannel serverChannel;
  private boolean connected;

  public ClientNetworkMgr() {
    serverSm = null;
    factory = null;
    s = null;
    serverChannel = null;
    connected = false;
  }

  public void run() throws Exception {
    factory = new MsgFactory();
    s = Selector.open();

    tryConnectToServer();
    while (true) {
      s.select();
      Set<SelectionKey> readyKeys = s.selectedKeys();
      for (SelectionKey k : readyKeys) {
        if (k.isReadable()) {
          try {
            serverSm.processPendingMsgs();
          } catch (ConnectionException e) {
            serverChannel.close();
            connected = false;
            serverChannel = null;
            serverSm.setIOStream(null);
            System.out.println("Connection to server lost, going to retry");
            tryConnectToServer();
            break;
          }
        }
      }
    }
  }

  protected void tryConnectToServer() throws Exception {
    byte[] ip = { 127, 0, 0, 1 };
    InetAddress lh = InetAddress.getByAddress(ip);
    InetSocketAddress isa = new InetSocketAddress(lh, Configuration.serverPort);
    while (!connected) {
      try {
        serverChannel = SocketChannel.open(isa);
        connected = true;
      } catch (ConnectException e) {
        System.out.println("Unable to connect to server, going to retry");
        Thread.sleep(1000);
      }
    }
    serverChannel.configureBlocking(false);
    serverChannel.register(s, SelectionKey.OP_READ);
    BufferedIOStream ios = new BufferedIOStream(serverChannel, factory);
    if (serverSm == null) {
      serverSm = new ConnectedServerSM(ios);
    } else {
      serverSm.setIOStream(ios);
    }
  }

  public boolean isConnectedToServer() {
    return connected;
  }

  public StateMachine getStateMachine() {
    return serverSm;
  }
}
