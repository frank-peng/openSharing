package com.share.statemachine;

import com.share.protocol.SyncedMsg;

public abstract class MsgHandler {
  protected StateMachine sm;

  public MsgHandler(StateMachine s) {
    this.sm = s;
  }

  public abstract String execute(SyncedMsg msg) throws Exception;
}
