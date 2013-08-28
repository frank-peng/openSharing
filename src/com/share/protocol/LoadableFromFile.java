package com.share.protocol;

import java.io.File;

public interface LoadableFromFile {
  public void loadFromFile(File f, byte[] base) throws Exception;
}
