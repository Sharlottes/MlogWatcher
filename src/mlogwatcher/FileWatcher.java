package mlogwatcher;

import arc.Core;
import arc.func.Cons;
import arc.util.Log;
import arc.util.Nullable;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher {
    @Nullable
    private static Thread fileWatcherThread;
    public static void startWatcherThread() {
        if(fileWatcherThread != null) return;
        fileWatcherThread = new FileWatcherThread(Core.settings.getString(Constants.Settings.mlogPath));
        fileWatcherThread.start();
    }

    public static void stopWatcherThread() {
        if(fileWatcherThread == null) return;
        fileWatcherThread.interrupt();
    }
}

class FileWatcherThread extends Thread {
    private final String targetFilePath;

    public FileWatcherThread(String targetFilePath) {
        super();
        setDaemon(true);
        setPriority(MIN_PRIORITY);
        this.targetFilePath = targetFilePath;
    }

    @Override
    public void run() {
        super.run();
        Path targetFilePath = FileSystems.getDefault().getPath(this.targetFilePath).getParent();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            targetFilePath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                final WatchKey watchKey = watchService.take();
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (((Path) event.context()).endsWith("out.mlog")) {
                        ProcessorUpdater.InsertLogic();
                    }
                }

                boolean valid = watchKey.reset();
                if(!valid) throw new Exception("watch mode no longer valid!");
            }
        } catch (IOException e)  {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.warn("mlog watcher's target file has been changed");
        } catch (Exception e) {
            Log.warn("mlog watcher's file watcher restarted");
            FileWatcher.startWatcherThread();
        }
    }
}

