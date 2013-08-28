package com.share.util;

import java.io.File;
import java.io.IOException;
public class Path {

  public static byte[] getRelativePath(byte[] parent, byte[] base) {
    if (parent.length < base.length)
      return null;
    for (int i = 0; i < base.length; i++) {
      if (base[i] != parent[i])
        return null;
    }
    // get relative path
    byte[] relative = new byte[parent.length - base.length];
    for (int i = base.length; i < parent.length; i++) {
      relative[i - base.length] = parent[i];
    }
    return relative;
  }

  public static byte[] getParentRelativePath(File f, byte[] base)
      throws IOException {
if(f == null || base == null)
      return null;
    byte[] parent = f.getParentFile().getCanonicalPath().getBytes();
    return getRelativePath(parent, base);
  }

  public static boolean byteArrayEqual(byte[] array1, byte[] array2) {
    if (array1 == array2)
      return true;
    if (array1 == null || array2 == null)
      return false;
    if (array1.length != array2.length)
      return false;
    for (int i = 0; i < array1.length; i++) {
      if (array1[i] != array2[i])
        return false;
    }
    return true;
  }
}
