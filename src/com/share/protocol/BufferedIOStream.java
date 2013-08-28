package com.share.protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import com.share.exception.ConnectionException;

public class BufferedIOStream {
  static public int defaultBufferSize = 65536;

  private MsgFactory msgFactory;
  protected ByteBuffer inBuf, outBuf;
  protected SocketChannel sock;
  protected int partialReadCount = 0;

  public BufferedIOStream(SocketChannel sock, MsgFactory messageFactory)
      throws IOException {
    inBuf = ByteBuffer.allocateDirect(BufferedIOStream.defaultBufferSize);
    outBuf = ByteBuffer.allocateDirect(BufferedIOStream.defaultBufferSize);
    this.sock = sock;
    this.msgFactory = messageFactory;
    // this.sock.configureBlocking(false);
  }

  public List<SyncedMsg> read() throws Exception {
    List<SyncedMsg> l;
    int read = sock.read(inBuf);
    if (read == -1) {
      throw new ConnectionException(sock.socket() + " has left.");
    }
    inBuf.flip();
    l = msgFactory.parseMessages(inBuf);
    if (inBuf.hasRemaining())
      inBuf.compact();
    else
      inBuf.clear();
    return l;
  }

  protected void appendMessageToOutBuf(SyncedMsg m) throws IOException {
    int msglen = m.getLength();
    if (outBuf.remaining() < msglen) {
      throw new IOException("Message length exceeds buffer capacity: " + msglen);
    }
    m.writeTo(outBuf);
  }

  /**
   * Buffers a single outgoing SyncedMsg.
   */
  public void write(SyncedMsg m) throws IOException {
    appendMessageToOutBuf(m);
    flush();
  }

  /**
   * Buffers a list of SyncedMsg.
   */
  public void write(List<SyncedMsg> l) throws IOException {
    for (SyncedMsg m : l) {
      appendMessageToOutBuf(m);
      flush();
    }
  }

  /**
   * Flush buffered outgoing data. Keep flushing until outBuf is empty. Each
   * flush() corresponds to a SocketChannel.write().
   */
  public void flush() throws IOException {
    while (outBuf.position() > 0) {
      outBuf.flip(); // swap pointers; limit = position; position = 0;
      sock.write(outBuf); // write data starting at position up to limit.
      outBuf.compact();
    }
  }

  public void sendFileContent(File f, byte[] root) throws Exception {
    SyncedFile file = new SyncedFile();
    file.loadFromFile(f, root);
    write(file);
    int totalRead = file.getBlockLength();
    while (totalRead < file.getFileInfo().getLength()) {
      file.loadBlockFromFile(f, file.getBlockseq() + 1);
      write(file);
      totalRead += file.getBlockLength();
    }
  }

  /**
   * State variables for assembling files from pieces of SyncedFile.
   * 
   * @return whether this file has been completely written.
   */
  private FileOutputStream fos = null;
  private FileInfo pendingFileInfo = null;
  private int totalWrote = 0;
  private int lastSeq = -1;

  public boolean assembleFileContent(SyncedFile file, File f) throws Exception {
    if (fos == null) {
      fos = new FileOutputStream(f);
      pendingFileInfo = file.getFileInfo();
    } else {
      if (!pendingFileInfo.equals(file.getFileInfo())) {
        throw new Exception("Received a block not of the current pending file!");
      }
    }
    if (file.getFileInfo().getLength() > 0) { // File is not a directory.
      file.writeToFile(fos);
      totalWrote += file.getBlockLength();
      if (file.getBlockseq() != lastSeq + 1) {
        throw new Exception(
            "Sequence number mismatch when reading from buffer!");
      }
      lastSeq = file.getBlockseq();
      if (totalWrote == file.getFileInfo().getLength()) {
        fos.close();
        fos = null;
        pendingFileInfo = null;
        totalWrote = 0;
        lastSeq = -1;
        f.setLastModified(file.getFileInfo().getTimestamp());
        return true;
      }
      return false;
    } else {
      f.mkdir();
      f.setLastModified(file.getFileInfo().getTimestamp());
      return true;
    }
  }
}
