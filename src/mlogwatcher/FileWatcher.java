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

import static mindustry.Vars.*;

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


    public static boolean matchesExtension(String path, String extensionKey) {
        String extension = Core.settings.getString(extensionKey);
        if (extension.isEmpty()) return false;
        return path.endsWith("." + extension);
    }

    public static boolean matchesPattern(Path path, String patternKey) {
        String pattern = Core.settings.getString(patternKey);

        if (pattern.isEmpty()) return false;

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

        //        Log.info("[MlogWatcher] checking file " + path.toString() + " with pattern: " + pattern + " ---> " + (pathMatcher.matches(path) ? " matches" : " does not match"));

        return pathMatcher.matches(path);
    }

    public static void forceUpdateAll() {
        String pathString = Core.settings.getString(Constants.Settings.mlogPath);

        if (pathString.isEmpty()) {
            Log.warn("[MlogWatcher] cannot find mlog path!");

            return;
        }

        Path path = FileSystems.getDefault().getPath(pathString);

        try {
            Log.info("[MlogWatcher] updating all files");
            forceUpdateRecursive(path, path);
            Log.info("[MlogWatcher] all files updated");
            ui.showInfoFade(Constants.Bundles.forceUpdateAllDone);
        } catch (IOException e) {
            Log.warn("[MlogWatcher] error while updating all: " + e.toString());
        }
    }

    private static void forceUpdateRecursive(Path targetPath, Path rootPath) throws IOException {
        if(checkPathIgnored(rootPath.relativize(targetPath))) {
            return;
        }

        Files.walkFileTree(targetPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs)
                    throws IOException {
                if(!checkPathIgnored(rootPath.relativize(filePath))) {
                    Log.info("[MlogWatcher] walking file " + filePath);
                    handleFile(filePath, targetPath);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs)
                    throws IOException {
                if(dirPath != targetPath && !checkPathIgnored(rootPath.relativize(dirPath))) {
                    Log.info("[MlogWatcher] walking directory " + rootPath.relativize(dirPath));
                    forceUpdateRecursive(dirPath, rootPath);
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static boolean checkPathIgnored(Path relativePath){
        String[] ignorePatternArray = Core.settings.getString(Constants.Settings.ignorePathPattern, "").split(" ");

        for (String ignorePattern : ignorePatternArray) {
            if (ignorePattern.isEmpty()) continue;

            if (FileSystems
                    .getDefault()
                    .getPathMatcher("glob:" + ignorePattern)
                    .matches(relativePath)) {
                return true;
            }
        }

        return false;
    }

    public static void handleFile(Path absolutePath, Path basePath) {
        Path relativePath = basePath.relativize(absolutePath);

        if(checkPathIgnored(relativePath)) return;

        String pathString = absolutePath.toString();
        Fi file = Fi.get(pathString);
        String text = file.readString();
        Path mlogRelativePath = FileSystems.getDefault()
                .getPath(Core.settings.getString(Constants.Settings.mlogRootDirectory))
                .relativize(relativePath);

        if (matchesExtension(pathString, Constants.Settings.mlogExtension)) {
            Log.info("[MlogWatcher] updating logic");
            Core.app.post(() -> ProcessorUpdater.insertLogicFromFile(mlogRelativePath, text));
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

    static class FileWatcherThread extends Thread {
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
                    Path watchedPath = (Path) watchKey.watchable();

                    for (WatchEvent<?> event : watchKey.pollEvents()) {
                        Path absolutePath = watchedPath.resolve((Path) event.context());

                        handleFile(absolutePath, targetFilePath);

                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Log.warn("[MlogWatcher] new file detected, restarting watcher thread");
                            FileWatcher.restartWatcherThread();

                            return;
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
    }
}