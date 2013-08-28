package com.share.protocol;

import java.nio.ByteBuffer;

public class SyncedMsg implements IOableOnByteBuffer {

  public static final int VERSION = 0x000001;
  public static final int MINIMUM_LENGTH = 13;

  protected int version;
  protected MsgType type;
  protected int length;
  protected int source;// assume source of server always equals to 1

  public SyncedMsg() {
    this.version = SyncedMsg.VERSION;
  }

  public int updateLength() {
    this.length = (Integer.SIZE + Byte.SIZE + Integer.SIZE + Integer.SIZE) / 8;
    return length;
  }

  public int getLength() {
    return length;
  }

  @Override
  public void readFrom(ByteBuffer data) {
    // TODO Auto-generated method stub
    this.version = data.getInt();
    this.type = MsgType.valueOf(data.get());
    this.length = data.getInt();
    this.source = data.getInt();
  }

  @Override
  public void writeTo(ByteBuffer data) {
    // TODO Auto-generated method stub
  }

  @Override
  public int hashCode() {
    final int prime = 97;
    int result = 1;
    result = prime * result + length;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + length;
    result = prime * result + version;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub
    // return super.equals(obj);
    if (obj == this)
      return true;

    if (obj == null)
      return false;

    if (!(obj instanceof SyncedMsg))
      return false;
    SyncedMsg other = (SyncedMsg) obj;

    if (length != other.length)
      return false;

    if (version != other.version)
      return false;

    if (!type.equals(other.type))
      return false;

    return true;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public MsgType getType() {
    return type;
  }

  public void setType(MsgType type) {
    this.type = type;
  }

  public int getSource() {
    return source;
  }

  public void setSource(int source) {
    this.source = source;
  }

}
