package com.share.protocol;

import java.lang.reflect.Constructor;

public enum MsgType {
  FILE(0, SyncedFile.class, new Instantiable<SyncedMsg>() {
    @Override
    public SyncedMsg instantiate() {
      return new SyncedFile();
    }
  }), FILE_LIST(1, SyncedFileList.class, new Instantiable<SyncedMsg>() {
    @Override
    public SyncedMsg instantiate() {
      return new SyncedFileList();
    }
  }),

  PULL_FILES(2, SyncedPullFiles.class, new Instantiable<SyncedMsg>() {
    @Override
    public SyncedMsg instantiate() {
      return new SyncedPullFiles();
    }
  }),

  PUSH_FILES(3, SyncedPushFiles.class, new Instantiable<SyncedMsg>() {
    @Override
    public SyncedMsg instantiate() {
      return new SyncedPushFiles();
    }
  }), LIST_FILES(4, SyncedListFiles.class, new Instantiable<SyncedMsg>() {
    @Override
    public SyncedMsg instantiate() {
      return new SyncedListFiles();
    }
  }), LOCAL_REQ(5, SyncedLocalRequest.class, new Instantiable<SyncedMsg>() {
    @Override
    public SyncedMsg instantiate() {
      return new SyncedLocalRequest();
    }
  });

  static MsgType[] mapping;

  protected Class<? extends SyncedMsg> clazz;
  protected Constructor<? extends SyncedMsg> constructor;
  protected Instantiable<SyncedMsg> instantiable;
  protected byte type;

  MsgType(int type, Class<? extends SyncedMsg> clazz,
      Instantiable<SyncedMsg> instantiator) {
    this.type = (byte) type;
    this.clazz = clazz;
    this.instantiable = instantiator;
    try {
      this.constructor = clazz.getConstructor(new Class[] {});
    } catch (Exception e) {
      throw new RuntimeException("Failure getting constructor for class: "
          + clazz, e);
    }
    MsgType.addMapping(this.type, this);
  }

  public String toString() {
    return String.format("%d", type);
  }

  /**
   * Adds a mapping from type value to MsgType enum.
   * 
   * @param v Synced protocol type number
   * @param t type
   */
  static public void addMapping(byte v, MsgType t) {
    if (mapping == null)
      mapping = new MsgType[32];
    MsgType.mapping[v] = t;
  }

  /**
   * Remove a mapping from type value to MsgType enum.
   * 
   * @param v Synced protocol type number
   */
  static public void removeMapping(byte v) {
    MsgType.mapping[v] = null;
  }

  /**
   * Given a Synced message type number, return the MsgType associated with it.
   * 
   * @param Synced protocol type number
   * @return SyncedType enum type
   */

  static public MsgType valueOf(byte v) {
    return MsgType.mapping[v];
  }

  /**
   * @return Returns the Synced protocol type number corresponding to this
   *         MsgType.
   */
  public byte getTypeValue() {
    return this.type;
  }

  /**
   * @return return the SyncedMsg subclass corresponding to this MsgType.
   */
  public Class<? extends SyncedMsg> toClass() {
    return clazz;
  }

  /**
   * Returns the no-argument Constructor of the implementation class for this
   * MsgType.
   * 
   * @return the constructor
   */
  public Constructor<? extends SyncedMsg> getConstructor() {
    return constructor;
  }

  /**
   * Returns a new instance of the SyncedMsg represented by this MsgType.
   * 
   * @return the new object
   */
  public SyncedMsg newInstance() {
    return instantiable.instantiate();
  }

  /**
   * @return the instantiable
   */
  public Instantiable<SyncedMsg> getInstantiable() {
    return instantiable;
  }

  /**
   * @param instantiable the instantiable to set
   */
  public void setInstantiable(Instantiable<SyncedMsg> instantiable) {
    this.instantiable = instantiable;
  }
}
