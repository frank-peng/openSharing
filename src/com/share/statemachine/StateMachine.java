package com.share.statemachine;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.share.protocol.BufferedIOStream;
import com.share.protocol.FileInfo;
import com.share.protocol.SyncedLocalRequest;
import com.share.protocol.SyncedMsg;

public abstract class StateMachine {
  protected HashSet<String> states;
  protected HashMap<Transition, MsgHandler> map;
  /** The current state the state machine is in. */
  protected String current;
  protected BufferedIOStream ios;
  protected HashSet<FileInfo> pendingFiles;
  protected LinkedList<SyncedLocalRequest> pendingRequests;

  protected StateMachine(String[] ss, BufferedIOStream io) {
    states = new HashSet<String>();
    map = new HashMap<Transition, MsgHandler>();
    for (String s : ss) {
      states.add(s);
    }
    this.ios = io;
    this.pendingFiles = new HashSet<FileInfo>();
    this.pendingRequests = new LinkedList<SyncedLocalRequest>();
  }

  protected void processMsg(SyncedMsg msg) throws Exception {
    MsgHandler handler = map.get(new Transition(current, msg.getType()));
    if (handler == null) {
      throw new Exception(
          String.format("Cannot find handler for msg %s in state %s!",
              msg.getType(), current));
    }
    String next = handler.execute(msg);
    if (!states.contains(next)) {
      throw new Exception("Invalid next state!");
    }
    current = next;
  }

  public void processPendingMsgs() throws Exception {
    List<SyncedMsg> msgs = ios.read();
    for (SyncedMsg msg : msgs) {
      processMsg(msg);
    }

    while (isCurrentStateIdle() && pendingRequests.size() > 0) {
      SyncedLocalRequest req = pendingRequests.removeFirst();
      processMsg(req);
    }
  }

  public abstract boolean isCurrentStateIdle();

  public void addLocalRequest(SyncedLocalRequest req) throws Exception {
    if (isCurrentStateIdle()) {
      processMsg(req);
    } else {
      pendingRequests.addLast(req);
    }
  }

  public BufferedIOStream getIOStream() {
    return ios;
  }

  public void setIOStream(BufferedIOStream io) {
    this.ios = io;
  }

  public HashSet<FileInfo> getPendingFiles() {
    return pendingFiles;
  }
}
