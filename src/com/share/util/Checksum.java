package com.share.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {

  public static final int CHECKSUM_LENGTH = 16;
  public static final String CHECKSUM_METHOD = "MD5";
  
  public static byte[] getChecksum(File file) throws NoSuchAlgorithmException,
      IOException {
    InputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[1024];
    MessageDigest md = MessageDigest.getInstance(CHECKSUM_METHOD);
    int numRead;
    do {
      numRead = fis.read(buffer);
      if (numRead > 0) {
        md.update(buffer, 0, numRead);
      }
    } while (numRead != -1);
    fis.close();
    return md.digest();
  }

  public static String convertChecksumToString(byte[] checksum) {
    if(null == checksum)
      return null;
    String result = "";
    for (int i = 0; i < checksum.length; i++) {
      result += Integer.toString((checksum[i] & 0xff) + 0x100, 16).substring(1);
    }
    return result;
  }

  public static boolean compareChecksum(byte[] cs1, byte[] cs2) {
    for (int i = 0; i < CHECKSUM_LENGTH; i++) {
      if (cs1[i] != cs2[i])
        return false;
    }
    return true;
  }

}
