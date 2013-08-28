package com.share.protocol;

import java.nio.ByteBuffer;

public interface IOableOnByteBuffer {

  public void readFrom(ByteBuffer data);
  public void writeTo(ByteBuffer data);
}
