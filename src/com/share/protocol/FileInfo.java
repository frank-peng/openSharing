package com.share.protocol;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;

import com.share.util.Checksum;
import com.share.util.Path;

public class FileInfo implements LoadableFromFile, IOableOnByteBuffer {

  public static final int FLAG_EXEC = 0x01;
  public static final int FLAG_WRITE = 0x02;
  public static final int FLAG_READ = 0x04;
  public static final int FLAG_DIR = 0x08;

  protected int nameLength;
  protected byte[] name;
  protected int pathLength;
  protected byte[] path;
  protected int length;
  protected int flags;
  protected long timestamp;
  protected byte[] checksum;

  public FileInfo() {

  }

  @Override
  public void readFrom(ByteBuffer data) {
    // TODO Auto-generated method stub
    this.nameLength = data.getInt();
    this.name = new byte[nameLength];
    data.get(name);
    this.pathLength = data.getInt();
    this.path = new byte[pathLength];
    data.get(path);
    this.length = data.getInt();
    this.flags = data.getInt();
    this.timestamp = data.getLong();
    if ((flags & FileInfo.FLAG_DIR) == 0) {
      this.checksum = new byte[Checksum.CHECKSUM_LENGTH];
      data.get(this.checksum);
    } else
      this.checksum = null;
  }

  @Override
  public void writeTo(ByteBuffer data) {
    // TODO Auto-generated method stub
    data.putInt(nameLength);
    data.put(this.name);
    data.putInt(pathLength);
    data.put(path);
    data.putInt(length);
    data.putInt(flags);
    data.putLong(timestamp);
    if ((flags & FileInfo.FLAG_DIR) == 0) {
      data.put(checksum);
    }
  }

  @Override
  public void loadFromFile(File f, byte[] base) throws Exception {
    // TODO Auto-generated method stub
    this.name = f.getName().getBytes();
    this.nameLength = name.length;
    this.path = Path.getParentRelativePath(f, base);
    this.pathLength = path.length;
    this.length = (int) f.length();
    this.flags = 0;
    if (f.canExecute())
      this.flags |= FileInfo.FLAG_EXEC;
    if (f.canWrite())
      this.flags |= FileInfo.FLAG_WRITE;
    if (f.canRead())
      this.flags |= FileInfo.FLAG_READ;
    if (f.isDirectory())
      this.flags |= FileInfo.FLAG_DIR;
    this.timestamp = f.lastModified();
    if (!f.isDirectory())
      this.checksum = Checksum.getChecksum(f);
    else {
      this.length = 0;
      this.checksum = null;
    }
  }

  public int totalLength() {
    return (Integer.SIZE + Integer.SIZE + Integer.SIZE + Integer.SIZE + Long.SIZE)
        / 8
        + nameLength
        + pathLength
        + (checksum == null ? 0 : Checksum.CHECKSUM_LENGTH);
  }

  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub

    if (obj == this)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof FileInfo))
      return false;

    FileInfo other = (FileInfo) obj;
    if (!Path.byteArrayEqual(this.name, other.name)) {
      return false;
    }
    if (this.nameLength != other.nameLength
        || this.nameLength != this.name.length
        || other.nameLength != other.name.length) {
      return false;
    }
    if (!Path.byteArrayEqual(this.path, other.path)) {
      return false;
    }
    if (this.pathLength != other.pathLength
        || this.pathLength != this.path.length
        || other.pathLength != other.path.length) {
      return false;
    }
    if (this.length != other.length) {
      return false;
    }
    if (this.flags != other.flags) {
      return false;
    }
    if (this.timestamp != other.timestamp) {
      return false;
    }
    if (!Path.byteArrayEqual(this.checksum, other.checksum)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub

    final int prime = 97;
    int result = 1;
    for (byte b : checksum) {
      result = result * prime + b;
    }
    return result;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    // return super.toString();
    return String.format("[%s %d][%s %d][length %d][flags %d][%s][%s]",
        new String(name), nameLength, new String(path), pathLength, length,
        flags, new Date(timestamp), Checksum.convertChecksumToString(checksum));
  }

  public int getNameLength() {
    return nameLength;
  }

  public void setNameLength(int nameLength) {
    this.nameLength = nameLength;
  }

  public byte[] getName() {
    return name;
  }

  public void setName(byte[] name) {
    this.name = name;
  }

  public int getPathLength() {
    return pathLength;
  }

  public void setPathLength(int pathLength) {
    this.pathLength = pathLength;
  }

  public byte[] getPath() {
    return path;
  }

  public void setPath(byte[] path) {
    this.path = path;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getFlags() {
    return flags;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public byte[] getChecksum() {
    return checksum;
  }

  public void setChecksum(byte[] checksum) {
    this.checksum = checksum;
  }

}
