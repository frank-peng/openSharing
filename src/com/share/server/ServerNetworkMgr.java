package com.share.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;

import com.share.exception.ConnectionException;
import com.share.protocol.BufferedIOStream;
import com.share.protocol.MsgFactory;
import com.share.statemachine.StateMachine;

public class ServerNetworkMgr {
  HashMap<SocketChannel, StateMachine> chn2sm;
  MsgFactory factory;
  Selector s;
  ServerSocketChannel acceptChannel;

  public void run() throws Exception {
    chn2sm = new HashMap<SocketChannel, StateMachine>();
    factory = new MsgFactory();

    s = Selector.open();
    acceptChannel = ServerSocketChannel.open();
    acceptChannel.configureBlocking(false);
    byte[] ip = { 0, 0, 0, 0 };
    InetAddress lh = InetAddress.getByAddress(ip);
    InetSocketAddress isa = new InetSocketAddress(lh, Configuration.ListenPort);
    acceptChannel.socket().bind(isa);
    acceptChannel.socket().setReuseAddress(true);
    acceptChannel.register(s, SelectionKey.OP_ACCEPT);
    while (true) {
      s.select();
      Set<SelectionKey> readyKeys = s.selectedKeys();
      for (SelectionKey k : readyKeys) {
        if (k.isAcceptable()) {
          SocketChannel channel = ((ServerSocketChannel) k.channel()).accept();
          if (channel == null) {
            continue;
          }
          channel.configureBlocking(false);
          channel.register(s, SelectionKey.OP_READ);
          BufferedIOStream ios = new BufferedIOStream(channel, factory);
          StateMachine sm = new ConnectedClientSM(ios);
          chn2sm.put(channel, sm);
          System.out.println(channel.socket().toString() + " has connected.");
        } else if (k.isReadable()) {
          SocketChannel channel = (SocketChannel) k.channel();
          StateMachine sm = chn2sm.get(channel);
          try {
            sm.processPendingMsgs();
          } catch (ConnectionException e) {
            channel.close();
            chn2sm.remove(channel);
            System.out.println(e.getMessage());
          }
        }
      }
    }
  }

}
