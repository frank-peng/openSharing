package com.share.server;

import java.io.File;

import com.share.protocol.*;
import com.share.statemachine.*;

public class ConnectedClientSM extends StateMachine {
  private static final String[] allStates = { "idle", "receiving" };

  private static class ListFilesHandler extends MsgHandler {
    public ListFilesHandler(StateMachine s) {
      super(s);
    }

    @Override
    public String execute(SyncedMsg msg) throws Exception {
      if (!(msg instanceof SyncedListFiles)) {
        System.out
            .println("ListFilesHandler expecting SyncedListFiles, while getting "
                + msg.getClass().toString());
      } else {
        SyncedListFiles listFiles = (SyncedListFiles) msg;
        SyncedFileList fileList = new SyncedFileList();
        File dir =
            new File(new String(Configuration.rootPath)
                + new String(listFiles.getPath()));
        fileList.loadFromFile(dir, Configuration.rootPath);
        sm.getIOStream().write(fileList);
        sm.getIOStream().flush();
      }
      return "idle";
    }
  }

  private static class PullFilesHandler extends MsgHandler {
    public PullFilesHandler(StateMachine s) {
      super(s);
    }

    @Override
    public String execute(SyncedMsg msg) throws Exception {
      if (!(msg instanceof SyncedPullFiles)) {
        System.out
            .println("ListFilesHandler expecting SyncedPullFiles, while getting "
                + msg.getClass().toString());
      } else {
        SyncedPullFiles pullFiles = (SyncedPullFiles) msg;
        String path =
            new String(Configuration.rootPath)
                + new String(pullFiles.getPath()) + Configuration.separator;
        for (String name : pullFiles.getFileNames()) {
          File file = new File(path + name);
          sm.getIOStream().sendFileContent(file, Configuration.rootPath);
        }
      }
      return "idle";
    }
  }

  private static class PushFilesHandler extends MsgHandler {
    public PushFilesHandler(StateMachine s) {
      super(s);
    }

    @Override
    public String execute(SyncedMsg msg) throws Exception {
      if (!(msg instanceof SyncedPushFiles)) {
        System.out
            .println("ListFilesHandler expecting SyncedPushFiles, while getting "
                + msg.getClass().toString());
      } else {
        SyncedPushFiles pushFiles = (SyncedPushFiles) msg;
        for (String name : pushFiles.getFileNames()) {
          sm.getPendingFiles().add(pushFiles.getFileInfo(name));
        }
      }
      return "receiving";
    }
  }

  private static class FileHandler extends MsgHandler {
    public FileHandler(StateMachine s) {
      super(s);
    }

    @Override
    public String execute(SyncedMsg msg) throws Exception {
      if (!(msg instanceof SyncedFile)) {
        System.out
            .println("ListFilesHandler expecting SyncedFile, while getting "
                + msg.getClass().toString());
      } else {
        SyncedFile file = (SyncedFile) msg;
        String path =
            new String(Configuration.rootPath)
                + new String(file.getFileInfo().getPath())
                + Configuration.separator;
        File f = new File(path + new String(file.getFileInfo().getName()));
        if (sm.getIOStream().assembleFileContent(file, f)) {
          sm.getPendingFiles().remove(file.getFileInfo());
        }
      }
      if (sm.getPendingFiles().size() == 0) {
        return "idle";
      } else {
        return "receiving";
      }
    }
  }

  public ConnectedClientSM(BufferedIOStream io) {
    super(allStates, io);
    current = "idle";

    // Now adding the transition graph in the state machine.
    map.put(new Transition("idle", MsgType.LIST_FILES), new ListFilesHandler(
        this));
    map.put(new Transition("idle", MsgType.PULL_FILES), new PullFilesHandler(
        this));
    map.put(new Transition("idle", MsgType.PUSH_FILES), new PushFilesHandler(
        this));
    map.put(new Transition("receiving", MsgType.FILE), new FileHandler(this));
  }

  @Override
  public boolean isCurrentStateIdle() {
    return current.matches("idle");
  }
}
