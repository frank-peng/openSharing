package com.share.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MsgFactory {
  public SyncedMsg getMessage(MsgType t) {
    return t.newInstance();
  }

  public List<SyncedMsg> parseMessages(ByteBuffer data)
      throws MessageParseException {
    List<SyncedMsg> msglist = new ArrayList<SyncedMsg>();
    SyncedMsg msg = null;

    while (data.remaining() >= SyncedMsg.MINIMUM_LENGTH) {
      int originalPos = data.position();
      msg = this.parseMessageOne(data);
      if (msg == null) {
        data.position(originalPos);
        break;
      } else {
        msglist.add(msg);
      }
    }

    return msglist;
  }

  public SyncedMsg parseMessageOne(ByteBuffer data)
      throws MessageParseException {
    try {
      SyncedMsg demux = new SyncedMsg();
      SyncedMsg ofm = null;

      if (data.remaining() < SyncedMsg.MINIMUM_LENGTH)
        return ofm;

      int originalPos = data.position();
      demux.readFrom(data);
      data.position(originalPos);

      if (demux.getLength() > data.remaining())
        return ofm;

      ofm = getMessage(demux.getType());
      if (ofm == null)
        return null;

      ofm.readFrom(data);
      if (SyncedMsg.class.equals(ofm.getClass())) {
        // This message is not implemented.
      }

      return ofm;
    } catch (Exception e) {
      throw new MessageParseException(e);
    }
  }
}
