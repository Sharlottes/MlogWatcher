package mlogjswatcher;

import arc.Core;
import arc.util.Log;
import arc.util.Nullable;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher                                {
    @Nullable
    private static Thread fileWatcherThread;
    public static void startWatcherThread() {
        fileWatcherThread = new FileWatcherThread();
        fileWatcherThread.start();
    }

    public static void stopWatcherThread() {
        fileWatcherThread.interrupt();
    }
}

class FileWatcherThread extends Thread {
    public FileWatcherThread() {
        super();
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
        super.run();
        String mlogjsPath = Core.settings.getString("mlogjswatcher-mlogjs-path");

        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            FileSystems.getDefault().getPath(mlogjsPath).getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
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
            Log.warn("mlogjs target file has been changed");
        } catch (Exception e) {
            Log.warn("mlogjs file watcher restarted");
            FileWatcher.startWatcherThread();
        }
    }
}
