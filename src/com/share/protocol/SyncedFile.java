package com.share.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.share.util.Path;

public class SyncedFile extends SyncedMsg implements LoadableFromFile {
  public static final int BLOCK_MAX_LENGTH = 16384; // 16K is default block

  protected FileInfo fileInfo;
  protected int blockLength;
  protected int blockseq;
  protected byte[] block;

  private FileInputStream fis;

  public SyncedFile() {
    super();
    // this.type = MsgType.FILE;
  }

  @Override
  public void readFrom(ByteBuffer data) {
    // TODO Auto-generated method stub
    super.readFrom(data);
    this.fileInfo = new FileInfo();
    fileInfo.readFrom(data);
    this.blockLength = data.getInt();
    this.blockseq = data.getInt();
    if (blockLength != 0) {
      this.block = new byte[blockLength];
      data.get(block);
    } else {
      this.block = null;
    }
  }

  @Override
  public void writeTo(ByteBuffer data) {
    // TODO Auto-generated method stub
    super.writeTo(data);
    fileInfo.writeTo(data);
    data.putInt(this.blockLength);
    data.putInt(this.blockseq);
    if (blockLength != 0)
      data.put(this.block);
  }

  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SyncedFile)) {
      return false;
    }
    SyncedFile other = (SyncedFile) obj;
    if (!this.fileInfo.equals(other.fileInfo)) {
      return false;
    }
    if (this.blockLength != other.blockLength
        || this.blockseq != other.blockseq) {
      return false;
    }
    if (!Path.byteArrayEqual(this.block, other.block)) {
      return false;
    }
    return super.equals(obj);
  }

  @Override
  public int updateLength() {
    // TODO Auto-generated method stub
    super.updateLength();
    this.length += fileInfo.totalLength();
    this.length += (Integer.SIZE + Integer.SIZE) / 8 + blockLength;
    return length;
  }

  public void loadBlockFromFile(File f, int seq) throws Exception {
    // TODO Auto-generated method stub
    if (seq * BLOCK_MAX_LENGTH >= f.length()) {
      this.blockLength = 0;
      this.blockseq = -1;
      throw new Exception(" sequence number exceeeds file length! ");
    }
    this.blockLength = BLOCK_MAX_LENGTH;
    if ((f.length() - seq * BLOCK_MAX_LENGTH) < BLOCK_MAX_LENGTH) {
      this.blockLength = (int) f.length() - seq * BLOCK_MAX_LENGTH;
    }
    this.blockseq = seq;
    if (block == null || block.length != blockLength) {
      this.block = new byte[blockLength];
    }
    int numRead = fis.read(block);
    if (numRead != blockLength) {
      throw new Exception("File read length does not match!");
    }
    if (fis.available() == 0) {
      fis.close();
      this.fis = null;
    }
  }

  @Override
  public void loadFromFile(File f, byte[] base) throws Exception {
    // TODO Auto-generated method stub
    this.fileInfo = new FileInfo();
    fileInfo.loadFromFile(f, base);
    if (fileInfo.length != 0) {
      this.fis = new FileInputStream(f);
      loadBlockFromFile(f, 0);
    } else {
      this.blockLength = 0;
      this.blockseq = -1;
      this.block = null;
    }
  }

  public void writeToFile(FileOutputStream fos) throws IOException {
    fos.write(this.block);
  }

  public FileInfo getFileInfo() {
    return fileInfo;
  }

  public void setFileInfo(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
  }

  public int getBlockseq() {
    return blockseq;
  }

  public void setBlockseq(int blockseq) {
    this.blockseq = blockseq;
  }

  public byte[] getBlock() {
    return block;
  }

  public void setBlock(byte[] block) {
    this.block = block;
  }

  public int getBlockLength() {
    return blockLength;
  }

  public void setBlockLength(int blockLength) {
    this.blockLength = blockLength;
  }

}
