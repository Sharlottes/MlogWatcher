package mlogwatcher;

public class Constants {
    // Direct bundle references (without the "@" prefix)
    public static class DirectBundles {
        public static final String settingsFileWatcherRunning = "mlogwatcher.setting.fileWatcherRunning";
        public static final String settingsFileWatcherStopped = "mlogwatcher.setting.fileWatcherStopped";
        public static final String settingsWebSocketRunning = "mlogwatcher.setting.websocketRunning";
        public static final String settingsWebSocketStopped = "mlogwatcher.setting.websocketStopped";
        public static final String settingsProcessorId = "mlogwatcher.setting.processorId";
        public static final String schematicLibrary = "mlogwatcher.setting.schematicLibrary";
        public static final String numberOfSchematics = "mlogwatcher.setting.numberOfSchematics";
    }


    public static class Bundles {
        public static final String settingMlogPathLabel = "@mlogwatcher.setting.mlogLabel";
        public static final String settingIgnorePathPatternLabel = "@mlogwatcher.setting.ignorePathPatternLabel";
        public static final String settingNoPathSelected = "@mlogwatcher.setting.noPath";
        public static final String settingNoIgnorePathSelected = "@mlogwatcher.setting.noIgnorePath";
        public static final String settingMlogSelectButton = "@mlogwatcher.setting.mlogSelectButton";
        public static final String settingFileChooserTitle = "@mlogwatcher.setting.fileChooserTitle";
        public static final String settingMlogRootDirectoryInputLabel = "@mlogwatcher.setting.mlogRootDirectoryInputLabel";
        public static final String settingMlogExtensionInputLabel = "@mlogwatcher.setting.mlogExtensionInputLabel";
        public static final String settingMschExtensionInputLabel = "@mlogwatcher.setting.mschExtensionInputLabel";
        public static final String settingPatchFilePatternInputLabel = "@mlogwatcher.setting.patchFilePatternInputLabel";
        public static final String settingContentFilePatternInputLabel = "@mlogwatcher.setting.contentFilePatternInputLabel";
        public static final String settingTranslationFilePatternInputLabel = "@mlogwatcher.setting.translationFilePatternInputLabel";
        public static final String settingImageFilePatternInputLabel = "@mlogwatcher.setting.imageFilePatternInputLabel";
        public static final String settingSoundFilePatternInputLabel = "@mlogwatcher.setting.soundFilePatternInputLabel";
        public static final String settingMusicFilePatternInputLabel = "@mlogwatcher.setting.musicFilePatternInputLabel";
        public static final String settingBundleFilePatternInputLabel = "@mlogwatcher.setting.bundleFilePatternInputLabel";
        public static final String settingMlogWatchByNameOn = "@mlogwatcher.setting.mlogWatchByNameOn";
        public static final String settingRecursiveDirectoryWatchOn = "@mlogwatcher.setting.recursiveDirectoryWatchOn";
        public static final String settingNoExt = "@mlogwatcher.setting.noExtension";
        public static final String settingNoPattern = "@mlogwatcher.setting.noPattern";
        public static final String settingRestartFileWatcherButton = "@mlogwatcher.setting.restartFileWatcherButton";

        public static final String settingWebsocketPortLabel = "@mlogwatcher.setting.websocketPortLabel";
        public static final String settingRestartServerButton = "@mlogwatcher.setting.restartServerButton";
        public static final String settingIgnoreServerBindError = "@mlogwatcher.setting.ignoreServerBindError";
        public static final String settingLegacyApiOff = "@mlogwatcher.setting.legacyApiOff";

        public static final String settingProcessorTagVariables = "@mlogwatcher.setting.processorTagVariables";
        public static final String settingNoTagVariables = "@mlogwatcher.setting.noTagVariables";

        public static final String purgeSchematics = "@mlogwatcher.setting.purgeSchematics";
        public static final String purgeSchematicsPrompt = "@mlogwatcher.setting.purgeSchematicsPrompt";

        public static final String infoServerBindErrorTitle = "@mlogwatcher.info.serverBindErrorTitle";
        public static final String infoServerBindError = "@mlogwatcher.info.serverBindError";

        public static final String forceUpdateAll = "@mlogwatcher.editor.forceUpdateAll";
        public static final String forceUpdateAllDone = "@mlogwatcher.editor.forceUpdateAllDone";
    }

    public static class Settings {
        public static final String mlogPath = "mlogwatcher-mlog-path";
        public static final String ignorePathPattern = "mlogwatcher-ignore-path-pattern";
        public static final String mlogRootDirectory = "mlogwatcher-mlog-root-directory";
        public static final String mlogExtension = "mlogwatcher-mlog-extension";
        public static final String mschExtension = "mlogwatcher-msch-extension";
        public static final String patchFilePattern = "mlogwatcher-patch-file-pattern";
        public static final String contentFilePattern = "mlogwatcher-content-file-pattern";
        public static final String translationFilePattern = "mlogwatcher-translation-file-pattern";
        public static final String imageFilePattern = "mlogwatcher-iamge-file-pattern";
        public static final String soundFilePattern = "mlogwatcher-sound-file-pattern";
        public static final String musicFilePattern = "mlogwatcher-music-file-pattern";
        public static final String bundleFilePattern = "mlogwatcher-bundle-file-pattern";
        public static final String mlogWatchByNameOn = "mlogwatcher-mlog-watch-by-name-on";
        public static final String recursiveDirectoryWatchOn = "mlogwatcher-recursive-directory-watch-on";
        public static final String websocketPort = "mlogwatcher-websocket-port";
        public static final String ignoreServerBindError = "mlogwatcher-ignore-websocket-error";
        public static final String legacyApiOff = "mlogwatcher-legacy-api-off";
        public static final String processorTagVariables = "mlogwatcher-processor-tag-variables";
    }
}
