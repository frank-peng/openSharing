package com.share.protocol;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;

import com.share.util.Path;

public class SyncedFileList extends SyncedMsg implements LoadableFromFile {
  /** The path is length-variable, so we need to know its length. */
  protected int pathLength;
  /** Relative path of this directory in the root of the server. */
  protected byte[] path;
  /** Count of the files/dirs in this list. */
  protected int count;
  /** FileInfo of the files/dirs in this list. */
  protected HashMap<String, FileInfo> list;

  public SyncedFileList() {
    super();
    this.type = MsgType.FILE_LIST;
  }

  @Override
  public int updateLength() {
    super.updateLength();
    this.length += (Integer.SIZE + Integer.SIZE) / 8 + pathLength;
    for (FileInfo fileInfo : list.values()) {
      this.length += fileInfo.totalLength();
    }
    return length;
  }

  @Override
  public void loadFromFile(File f, byte[] base) throws Exception {
    if (!f.isDirectory()) {
      throw new Exception("File must be a directory!");
    }
    this.path = Path.getParentRelativePath(f, base);
    this.pathLength = path.length;
    File[] files = f.listFiles();
    this.count = files.length;
    this.list = new HashMap<String, FileInfo>();
    for (File file : files) {
      FileInfo fileInfo = new FileInfo();
      fileInfo.loadFromFile(file, base);
      list.put(new String(fileInfo.getName()), fileInfo);
    }
  }

  @Override
  public void readFrom(ByteBuffer data) {
    super.readFrom(data);
    this.pathLength = data.getInt();
    this.path = new byte[pathLength];
    data.get(path);
    this.count = data.getInt();
    this.list = new HashMap<String, FileInfo>();
    for (int i = 0; i < count; i++) {
      FileInfo fileInfo = new FileInfo();
      fileInfo.readFrom(data);
      list.put(new String(fileInfo.getName()), fileInfo);
    }
  }

  @Override
  public void writeTo(ByteBuffer data) {
    super.writeTo(data);
    data.putInt(pathLength);
    data.put(path);
    data.putInt(count);
    for (FileInfo fileInfo : list.values()) {
      fileInfo.writeTo(data);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SyncedFileList)) {
      return false;
    }
    SyncedFileList other = (SyncedFileList) obj;
    if (!Path.byteArrayEqual(this.path, other.path)) {
      return false;
    }
    if (this.pathLength != other.pathLength
        || this.pathLength != this.path.length
        || other.pathLength != other.path.length) {
      return false;
    }
    if (this.count != other.count) {
      return false;
    }
    for (String name : list.keySet()) {
      FileInfo fileInfo = other.list.get(name);
      if (fileInfo == null) {
        return false;
      }
      if (!this.list.get(name).equals(fileInfo)) {
        return false;
      }
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

  public int getCount() {
    return count;
  }

  public Set<String> getFileNames() {
    return list.keySet();
  }

  public boolean containsName(String name) {
    return list.containsKey(name);
  }

  public FileInfo getFileInfo(String name) {
    return list.get(name);
  }

  public void addFileInfo(FileInfo fileInfo) {
    if (list == null) {
      this.list = new HashMap<String, FileInfo>();
    }
    list.put(new String(fileInfo.getName()), fileInfo);
    this.count = list.size();
  }

  public void removeFileInfo(String name) {
    list.remove(name);
    this.count = list.size();
  }
}
