package com.share.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import com.share.util.Checksum;

public class SyncedPullFiles extends SyncedFileList {
  public SyncedPullFiles() {
    super();
    this.type = MsgType.PULL_FILES;
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
    if (!(obj instanceof SyncedPullFiles)) {
      return false;
    }
    SyncedPullFiles other = (SyncedPullFiles) obj;
    if (this.type != MsgType.PULL_FILES || other.type != MsgType.PULL_FILES) {
      return false;
    }
    return super.equals(obj);
  }

  /**
   * Compare the files in this list to the server's list, remove the files which
   * are same with the files in the server's list, or the files which do not
   * exist in the server's list. Files that are in the server's list but not in
   * this list will be added to this list. The resulting list will be the diff
   * to be used to send the pull request to the server.
   * 
   * @param other the server's list
   */
  public void extractPullDiff(SyncedFileList server) {
    ArrayList<String> toRemove = new ArrayList<String>();
    for (String name : list.keySet()) {
      if (!server.containsName(name)) {
        // list.remove(name);
        toRemove.add(name);
      } else {
        FileInfo info1 = list.get(name);
        FileInfo info2 = server.getFileInfo(name);
        if (Checksum.compareChecksum(info1.getChecksum(), info2.getChecksum())) {
          // Two files are the same.
          // list.remove(name);
          toRemove.add(name);
        } else if (info1.getTimestamp() > info2.getTimestamp()) {
          // The file in this list is newer.
          // XXX - Might not be safe because clients might have different time
          // to server.
          // list.remove(name);
          toRemove.add(name);
        }
      }
    }

    for (String name : server.getFileNames()) {
      if (!this.containsName(name)) {
        list.put(name, server.getFileInfo(name));
      }
    }

    for (String name : toRemove) {
      list.remove(name);
    }

    this.count = list.size();
  }
}
