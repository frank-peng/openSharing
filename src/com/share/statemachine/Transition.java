package com.share.statemachine;

import com.share.protocol.MsgType;

public class Transition {
  String state;
  MsgType msg;

  public Transition(String s, MsgType m) {
    state = s;
    msg = m;
  }

  @Override
  public int hashCode() {
    final int prime = 97;
    int result = 1;
    result = prime * result + state.hashCode();
    result = prime * result + msg.getTypeValue();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Transition)) {
      return false;
    }
    Transition other = (Transition) obj;
    if (!state.equals(other.state)) {
      return false;
    }
    if (msg != other.msg) {
      return false;
    }
    return true;
  }

  public String getState() {
    return state;
  }

  public MsgType getMsgType() {
    return msg;
  }
}
