package mlogwatcher;

import arc.Core;
import arc.util.Log;
import arc.util.Nullable;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher {
    @Nullable
    private static Thread fileWatcherThread;
    public static void startWatcherThread() {
        fileWatcherThread = new FileWatcherThread(Core.settings.getString("mlogwatcher-mlog-path"));
        fileWatcherThread.start();
    }

    public static void stopWatcherThread() {
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
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            FileSystems.getDefault().getPath(targetFilePath).getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
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
