package com.share.protocol;

public class SyncedLocalRequest extends SyncedMsg {
  public enum RequestCode {
    SYNC_DIR,
  }

  protected RequestCode code;
  protected String value;

  public SyncedLocalRequest() {
    super();
    this.type = MsgType.LOCAL_REQ;
  }

  public RequestCode getCode() {
    return code;
  }

  public void setCode(RequestCode c) {
    this.code = c;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String v) {
    this.value = v;
  }
}
