package com.share.client;

import java.io.File;
import java.io.IOException;

import com.share.protocol.BufferedIOStream;
import com.share.protocol.MsgType;
import com.share.protocol.SyncedFile;
import com.share.protocol.SyncedFileList;
import com.share.protocol.SyncedListFiles;
import com.share.protocol.SyncedLocalRequest;
import com.share.protocol.SyncedMsg;
import com.share.protocol.SyncedPullFiles;
import com.share.protocol.SyncedPushFiles;
import com.share.server.Configuration;
import com.share.statemachine.MsgHandler;
import com.share.statemachine.StateMachine;
import com.share.statemachine.Transition;
import com.share.util.Path;

public class ConnectedServerSM extends StateMachine {
  private static final String[] allStates = { "idle", "waiting", "receiving" };

  private static class FileListHandler extends MsgHandler {
    public FileListHandler(StateMachine s) {
      super(s);
    }

    @Override
    public String execute(SyncedMsg msg) throws Exception {
      if (!(msg instanceof SyncedFileList)) {
        System.out
            .println("ListFilesHandler expecting SyncedFileList, while getting "
                + msg.getClass().toString());
        return "idle";
      } else {
        SyncedFileList fromServer = (SyncedFileList) msg;
        SyncedPushFiles pushFiles = new SyncedPushFiles();
        File dir =
            new File(new String(Configuration.rootPath)
                + new String(fromServer.getPath()));
        pushFiles.loadFromFile(dir, Configuration.rootPath);
        pushFiles.extractPushDiff(fromServer);
        if (pushFiles.getCount() > 0) {
          sm.getIOStream().write(pushFiles);
          String path =
              new String(Configuration.rootPath)
                  + new String(pushFiles.getPath()) + Configuration.separator;
          for (String name : pushFiles.getFileNames()) {
            File file = new File(path + name);
            sm.getIOStream().sendFileContent(file, Configuration.rootPath);
          }
        }

        SyncedPullFiles pullFiles = new SyncedPullFiles();
        pullFiles.loadFromFile(dir, Configuration.rootPath);
        pullFiles.extractPullDiff(fromServer);
        if (pullFiles.getCount() > 0) {
          for (String name : pullFiles.getFileNames()) {
            sm.getPendingFiles().add(pullFiles.getFileInfo(name));
          }
          sm.getIOStream().write(pullFiles);
          return "receiving";
        } else {
          return "idle";
        }
      }
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

  private static class LocalRequestHandler extends MsgHandler {
    public LocalRequestHandler(StateMachine s) {
      super(s);
    }

    protected String handleSyncDir(String dir) throws IOException {
      SyncedListFiles listFiles = new SyncedListFiles();
      listFiles.setPath(Path.getRelativePath(dir.getBytes(),
          Configuration.rootPath));
      sm.getIOStream().write(listFiles);
      return "waiting";
    }

    @Override
    public String execute(SyncedMsg msg) throws Exception {
      if (!(msg instanceof SyncedLocalRequest)) {
        System.out
            .println("ListFilesHandler expecting SyncedLocalRequest, while getting "
                + msg.getClass().toString());
        return "idle";
      }

      SyncedLocalRequest req = (SyncedLocalRequest) msg;
      if (req.getCode() == SyncedLocalRequest.RequestCode.SYNC_DIR) {
        return handleSyncDir(req.getValue());
      } else {
        return "idle";
      }
    }
  }

  public ConnectedServerSM(BufferedIOStream io) {
    super(allStates, io);
    current = "idle";

    // Now adding the transition graph in the state machine.
    map.put(new Transition("waiting", MsgType.FILE_LIST), new FileListHandler(
        this));
    map.put(new Transition("receiving", MsgType.FILE), new FileHandler(this));
    map.put(new Transition("idle", MsgType.LOCAL_REQ), new LocalRequestHandler(
        this));
  }

  @Override
  public boolean isCurrentStateIdle() {
    return current.matches("idle");
  }
}
