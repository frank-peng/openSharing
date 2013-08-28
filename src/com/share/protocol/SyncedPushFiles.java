package com.share.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.share.util.Checksum;

public class SyncedPushFiles extends SyncedFileList {
  public SyncedPushFiles() {
    super();
    this.type = MsgType.PUSH_FILES;
  }

  @Override
  public void readFrom(ByteBuffer data) {
    super.readFrom(data);

  }

  @Override
  public void writeTo(ByteBuffer data) {
    super.writeTo(data);

  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SyncedPushFiles)) {
      return false;
    }
    SyncedPushFiles other = (SyncedPushFiles) obj;
    if (this.type != MsgType.PUSH_FILES || other.type != MsgType.PUSH_FILES) {
      return false;
    }
    return super.equals(obj);
  }

  /**
   * Compare the files in this list to the server's list, remove the files which
   * are same with the files in the server's list, or the files which do not
   * exist in this list. Files that are in this list but not in the server's
   * list be added to this list. The resulting list will be the diff to be used
   * to send the push request to the server.
   * 
   * @param other the server's list
   */
  public void extractPushDiff(SyncedFileList server) {
    ArrayList<String> toRemove = new ArrayList<String>();
    for (String name : list.keySet()) {
      if (server.containsName(name)) {
        FileInfo info1 = list.get(name);
        FileInfo info2 = server.getFileInfo(name);
        if (Checksum.compareChecksum(info1.getChecksum(), info2.getChecksum())) {
          // Two files are the same.
          // list.remove(name);
          toRemove.add(name);
        } else if (info1.getTimestamp() < info2.getTimestamp()) {
          // The file in this list is older.
          // XXX - Might not be safe because clients might have different time
          // to server.
          // list.remove(name);
          toRemove.add(name);
        }
      }
    }

    for (String name : toRemove) {
      list.remove(name);
    }

    this.count = list.size();
  }
}
