package mlogwatcher;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Reflect;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.editor.MapInfoDialog;
import mindustry.editor.MapProcessorsDialog;
import mindustry.gen.Groups;
import mindustry.mod.data.DataAssetType;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static mindustry.Vars.state;

public class FileWatcher {
    @Nullable
    private static Thread fileWatcherThread;

    public static void startWatcherThread() {
        String path = Core.settings.getString(Constants.Settings.mlogPath);
        if (fileWatcherThread != null || path.isEmpty()) return;
        fileWatcherThread = new FileWatcherThread(path);
        fileWatcherThread.start();
    }

    public static void stopWatcherThread() {
        if (fileWatcherThread == null) return;
        fileWatcherThread.interrupt();
        fileWatcherThread = null;
    }

    public static void restartWatcherThread() {
        stopWatcherThread();
        startWatcherThread();
    }

    public static boolean running() {
        return fileWatcherThread != null && fileWatcherThread.isAlive();
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

        FileSystem fileSystem = FileSystems.getDefault();
        Path targetFilePath = fileSystem.getPath(this.targetFilePath);

        try (final WatchService watchService = fileSystem.newWatchService()) {
            Files.walkFileTree(targetFilePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    dir.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY
                    );

                    return FileVisitResult.CONTINUE;
                }
            });

            targetFilePath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            Log.info("[MlogWatcher] watching directory " + targetFilePath);
            Settings.updateWatcherStatus(true);

            while (true) {
                final WatchKey watchKey = watchService.take();

                outer: for (WatchEvent<?> event : watchKey.pollEvents()) {
                    Path absolutePath = ((Path) watchKey.watchable()).resolve((Path)event.context());
                    Path relativePath = targetFilePath.relativize(absolutePath);
                    String[] ignorePatternArray = Core.settings.getString(Constants.Settings.ignorePathPattern, "").split(" ");

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Log.warn("[MlogWatcher] new file detected, restarting watcher thread");
                        FileWatcher.restartWatcherThread();

                        return;
                    }

                    for (String ignorePattern : ignorePatternArray) {
                        if (ignorePattern.isEmpty()) continue;

                        if (fileSystem.getPathMatcher("glob:" + ignorePattern).matches(relativePath)) {
                            continue outer;
                        }
                    }

                    String pathString = absolutePath.toString();
                    Fi file = Fi.get(pathString);
                    String text = file.readString();

                    Log.info("[MlogWatcher] found modified file " + pathString);

                    if (matchesExtension(pathString, Constants.Settings.mlogExtension)) {
                        Log.info("[MlogWatcher] updating logic");
                        Core.app.post(() -> ProcessorUpdater.insertLogicFromFile(pathString));
                    } else if (matchesExtension(pathString, Constants.Settings.mschExtension)) {
                        Log.info("[MlogWatcher] updating schematics");
                        Core.app.post(() -> SchematicsUpdater.importSchematicsFromFile(pathString));
                    } else if (matchesPattern(relativePath, Constants.Settings.patchFilePattern)) {
                        Log.info("[MlogWatcher] updating patch files");
                        Core.app.post(() -> MapMetadataUpdater.updatePatch(relativePath, text));
                    } else if (matchesPattern(relativePath, Constants.Settings.contentFilePattern)) {
                        Log.info("[MlogWatcher] updating content files");
                        Core.app.post(() -> MapMetadataUpdater.updateContent(relativePath, text));
                    } else if (matchesPattern(relativePath, Constants.Settings.translationFilePattern)) {
                        Log.info("[MlogWatcher] updating translation files");
                        Core.app.post(() -> MapMetadataUpdater.updateTranslation(relativePath, text));
                    } else if (matchesPattern(relativePath, Constants.Settings.imageFilePattern)) {
                        Log.info("[MlogWatcher] updating image files");
                        Core.app.post(() -> MapMetadataUpdater.updateImageFile(file));
                    } else if (matchesPattern(relativePath, Constants.Settings.soundFilePattern)) {
                        Log.info("[MlogWatcher] updating sound files");
                        Core.app.post(() -> MapMetadataUpdater.updateAudioFile(DataAssetType.sound, file));
                    } else if (matchesPattern(relativePath, Constants.Settings.musicFilePattern)) {
                        Log.info("[MlogWatcher] updating music files");
                        Core.app.post(() -> MapMetadataUpdater.updateAudioFile(DataAssetType.music, file));
                    } else if (matchesPattern(relativePath, Constants.Settings.bundleFilePattern)) {
                        Log.info("[MlogWatcher] updating bundle files");
                        Core.app.post(() -> MapMetadataUpdater.updateBundle(text));
                    }
                }

                boolean valid = watchKey.reset();
                if (!valid) throw new Exception("watch mode no longer valid!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Log.warn("[MlogWatcher] directory has been changed");
        } catch (Exception e) {
            Log.warn("[MlogWatcher] file watcher restarted due to exception: " + e.toString());
            FileWatcher.restartWatcherThread();
        } finally {
            Settings.updateWatcherStatus(false);
        }
    }

    private boolean matchesExtension(String path, String extensionKey) {
        String extension = Core.settings.getString(extensionKey);
        if (extension.isEmpty()) return false;
        return path.endsWith("." + extension);
    }

    private boolean matchesPattern(Path path, String patternKey) {
        String pattern = Core.settings.getString(patternKey);

        if (pattern.isEmpty()) return false;

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

//        Log.info("[MlogWatcher] checking file " + path.toString() + " with pattern: " + pattern + " ---> " + (pathMatcher.matches(path) ? " matches" : " does not match"));

        return pathMatcher.matches(path);
    }
}
