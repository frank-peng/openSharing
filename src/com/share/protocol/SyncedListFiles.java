package com.share.protocol;

import java.nio.ByteBuffer;
import com.share.util.Path;

public class SyncedListFiles extends SyncedMsg {
  /** The path is length-variable, so we need to know its length. */
  protected int pathLength;
  /** Relative path of the directory to list in the root of the server. */
  protected byte[] path;

  public SyncedListFiles() {
    super();
    this.type = MsgType.LIST_FILES;
  }

  @Override
  public int updateLength() {
    super.updateLength();
    this.length += Integer.SIZE / 8 + pathLength;
    return length;
  }

  @Override
  public void readFrom(ByteBuffer data) {
    super.readFrom(data);
    this.pathLength = data.getInt();
    this.path = new byte[pathLength];
    data.get(path);
  }

  @Override
  public void writeTo(ByteBuffer data) {
    super.writeTo(data);
    data.putInt(pathLength);
    data.put(path);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SyncedListFiles)) {
      return false;
    }
    SyncedListFiles other = (SyncedListFiles) obj;
    if (!Path.byteArrayEqual(this.path, other.path)) {
      return false;
    }
    if (this.pathLength != other.pathLength
        || this.pathLength != this.path.length
        || other.pathLength != other.path.length) {
      return false;
    }
    return super.equals(obj);
  }

  public byte[] getPath() {
    return path;
  }

  public void setPath(String p) {
    this.path = p.getBytes();
    this.pathLength = path.length;
  }

  public void setPath(byte[] p) {
    this.path = p;
    this.pathLength = path.length;
  }
}
