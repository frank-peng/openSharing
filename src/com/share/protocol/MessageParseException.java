package com.share.protocol;

public class MessageParseException extends Exception {
  private static final long serialVersionID = -75893812926304726L;

  public MessageParseException() {
    super();
  }

  public MessageParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public MessageParseException(String message) {
    super(message);
  }

  public MessageParseException(Throwable cause) {
    super(cause);
  }
}
