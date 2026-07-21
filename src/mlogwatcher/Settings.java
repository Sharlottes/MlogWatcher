package mlogwatcher;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.FileChooserDialog;
import mlogwatcher.ui.ProcessorIdLabel;
import mlogwatcher.websocket.MlogServer;

import static arc.Core.bundle;
import static mindustry.Vars.ui;

public class Settings {
    private static final int columns = 2;
    private static Cell<Label> fileWatcherStatus;
    private static Cell<Label> webSocketStatus;
    private static Cell<Label> numberOfSchematicsLabel;

    public static void init() {
        Core.settings.defaults(
                Constants.Settings.mlogPath, "",
                Constants.Settings.mlogRootDirectory, "",
                Constants.Settings.ignorePathPattern, ".git**",
                Constants.Settings.mlogExtension, "mlog",
                Constants.Settings.mschExtension, "msch",
                Constants.Settings.patchFilePattern, "assets/patch/**/*.{hjson,json,json5}",
                Constants.Settings.contentFilePattern, "assets/content/**/*.{hjson,json,json5}",
                Constants.Settings.translationFilePattern,"assets/translation/**/*.properties",
                Constants.Settings.imageFilePattern,"assets/image/**/*.png",
                Constants.Settings.soundFilePattern, "assets/sound/**/*.{ogg,mp3}",
                Constants.Settings.musicFilePattern,  "assets/music/**/*.{ogg,mp3}",
                Constants.Settings.bundleFilePattern, "assets/bundle/bundle.{hjson,json,json5}",
                Constants.Settings.websocketPort, 9992,
                Constants.Settings.processorTagVariables, "*tag *id"
        );

        Vars.ui.settings.addCategory("Mlog Watcher", Icon.logic, t -> {
            fileWatcherStatus = category(t, true, FileWatcher.running()
                    ? Constants.DirectBundles.settingsFileWatcherRunning : Constants.DirectBundles.settingsFileWatcherStopped);

            TextField watchedDirectory = wideText(t, Constants.Settings.mlogPath,
                    Constants.Bundles.settingMlogPathLabel,
                    Constants.Bundles.settingNoPathSelected,
                    c -> FileWatcher.restartWatcherThread());

            button(t, Constants.Bundles.settingMlogSelectButton, () ->
                    new FileChooserDialog(
                            Constants.Bundles.settingFileChooserTitle,
                            file -> true,
                            true,
                            fi -> {
                                String path = fi.parent().absolutePath();
                                Core.settings.put(Constants.Settings.mlogPath, path);
                                FileWatcher.restartWatcherThread();
                                watchedDirectory.setText(path);
                            }).show());

            wideText(t, Constants.Settings.ignorePathPattern,
                    Constants.Bundles.settingIgnorePathPatternLabel,
                    Constants.Bundles.settingNoIgnorePathSelected,
                    c -> {});
            extension(t, Constants.Settings.mlogRootDirectory, Constants.Bundles.settingMlogRootDirectoryInputLabel);
            extension(t, Constants.Settings.mlogExtension, Constants.Bundles.settingMlogExtensionInputLabel);
            extension(t, Constants.Settings.mschExtension, Constants.Bundles.settingMschExtensionInputLabel);
            check(t, Constants.Settings.mlogWatchByNameOn, Constants.Bundles.settingMlogWatchByNameOn);
            check(t, Constants.Settings.recursiveDirectoryWatchOn, Constants.Bundles.settingRecursiveDirectoryWatchOn);

            button(t, Constants.Bundles.settingRestartFileWatcherButton, FileWatcher::restartWatcherThread);

            webSocketStatus = category(t, false, MlogServer.isOpen()
                    ? Constants.DirectBundles.settingsWebSocketRunning : Constants.DirectBundles.settingsWebSocketStopped);

            TextField portTextField = new TextField();
            portTextField.update(() -> portTextField.setText(String.valueOf(Core.settings.getInt(Constants.Settings.websocketPort))));
            portTextField.changed(() -> {
                try {
                    int port = Integer.parseInt(portTextField.getText());
                    Core.settings.put(Constants.Settings.websocketPort, port);
                    MlogServer.stopServer();
                    MlogServer.startServer();
                } catch (NumberFormatException ignored) {
                }
            });

            t.add(Constants.Bundles.settingWebsocketPortLabel).left();
            t.add(portTextField).row();

            check(t, Constants.Settings.ignoreServerBindError, Constants.Bundles.settingIgnoreServerBindError);
            check(t, Constants.Settings.legacyApiOff, Constants.Bundles.settingLegacyApiOff);

            button(t, Constants.Bundles.settingRestartServerButton, MlogServer::restartServer);

            category(t, false, Constants.DirectBundles.settingsProcessorId);

            wideText(t, Constants.Settings.processorTagVariables,
                    Constants.Bundles.settingProcessorTagVariables,
                    Constants.Bundles.settingNoTagVariables,
                    c -> ProcessorIdLabel.updateVariables());

            category(t, false, Constants.DirectBundles.schematicLibrary);
            numberOfSchematicsLabel = t.add(numberOfSchematicsText()).left().colspan(columns).color(Color.gray);
            numberOfSchematicsLabel.row();
            button(t, Constants.Bundles.purgeSchematics, () -> {
                ui.showConfirm("@confirm", Constants.Bundles.purgeSchematicsPrompt, SchematicsUpdater::purgeSchematics);
                numberOfSchematicsLabel.update(l -> l.setText(numberOfSchematicsText()));
            });

            extension(t, Constants.Settings.assetRootDirectory, Constants.Bundles.settingAssetRootDirectoryInputLabel);
            pattern(t, Constants.Settings.patchFilePattern, Constants.Bundles.settingPatchFilePatternInputLabel);
            pattern(t, Constants.Settings.contentFilePattern, Constants.Bundles.settingContentFilePatternInputLabel);
            pattern(t, Constants.Settings.translationFilePattern, Constants.Bundles.settingTranslationFilePatternInputLabel);
            pattern(t, Constants.Settings.imageFilePattern, Constants.Bundles.settingImageFilePatternInputLabel);
            pattern(t, Constants.Settings.soundFilePattern, Constants.Bundles.settingSoundFilePatternInputLabel);
            pattern(t, Constants.Settings.musicFilePattern, Constants.Bundles.settingMusicFilePatternInputLabel);
            pattern(t, Constants.Settings.bundleFilePattern, Constants.Bundles.settingBundleFilePatternInputLabel);

            t.button("@settings.reset", () -> {
                Core.settings.remove(Constants.Settings.mlogPath);
                Core.settings.remove(Constants.Settings.ignorePathPattern);
                Core.settings.remove(Constants.Settings.mlogRootDirectory);
                Core.settings.remove(Constants.Settings.mlogExtension);
                Core.settings.remove(Constants.Settings.mschExtension);
                Core.settings.remove(Constants.Settings.mlogWatchByNameOn);
                Core.settings.remove(Constants.Settings.recursiveDirectoryWatchOn);
                Core.settings.remove(Constants.Settings.websocketPort);
                Core.settings.remove(Constants.Settings.ignoreServerBindError);
                Core.settings.remove(Constants.Settings.legacyApiOff);
                Core.settings.remove(Constants.Settings.processorTagVariables);

                Core.settings.remove(Constants.Settings.assetRootDirectory);
                Core.settings.remove(Constants.Settings.patchFilePattern);
                Core.settings.remove(Constants.Settings.contentFilePattern);
                Core.settings.remove(Constants.Settings.translationFilePattern);
                Core.settings.remove(Constants.Settings.imageFilePattern);
                Core.settings.remove(Constants.Settings.soundFilePattern);
                Core.settings.remove(Constants.Settings.musicFilePattern);
                Core.settings.remove(Constants.Settings.bundleFilePattern);

                FileWatcher.restartWatcherThread();
                MlogServer.restartServer();
                ProcessorIdLabel.updateVariables();
            }).margin(14).width(240f).pad(6).padTop(12).colspan(columns).row();
        });
    }

    public static void updateWatcherStatus(boolean running) {
        if (fileWatcherStatus == null) return;
        fileWatcherStatus.update(l -> l.setText(Core.bundle.get(running
                ? Constants.DirectBundles.settingsFileWatcherRunning
                : Constants.DirectBundles.settingsFileWatcherStopped)));
    }

    public static void updateWebsocketStatus(boolean running) {
        if (webSocketStatus == null) return;
        webSocketStatus.update(l -> l.setText(Core.bundle.get(running
                ? Constants.DirectBundles.settingsWebSocketRunning
                : Constants.DirectBundles.settingsWebSocketStopped)));
    }

    public static void updateNumberOfSchematics() {
        if (numberOfSchematicsLabel != null) {
            numberOfSchematicsLabel.update(l -> l.setText(numberOfSchematicsText()));
        }
    }

    private static Cell<Label> category(Table t, boolean first, String key) {
        Cell<Label> cell = t.add(bundle.get(key));
        cell.left().color(Color.gray).colspan(columns)
                .pad(10).padTop(first ? 0 : 40).padBottom(4).row();
        t.image().color(Color.gray).fillX().colspan(columns).height(3)
                .pad(0).padBottom(20).row();

        return cell;
    }

    private static TextField wideText(Table t, String key, String name, String message, Cons<String> changed) {
        t.add(name).left().colspan(columns).row();
        TextField field = new TextField();
        field.setMessageText(message);
        field.update(() -> field.setText(String.valueOf(Core.settings.getString(key))));
        field.changed(() -> {
            Core.settings.put(key, field.getText());
            changed.get(field.getText());
        });
        t.add(field).colspan(columns).left().growX().row();
        return field;
    }

    private static void extension(Table t, String key, String name) {
        t.add(name).left();
        TextField field = new TextField();
        field.setFilter((f, c) -> c != ' ' && c != '.');
        field.setMessageText(Constants.Bundles.settingNoExt);
        field.update(() -> field.setText(String.valueOf(Core.settings.getString(key))));
        field.changed(() -> Core.settings.put(key, field.getText()));
        t.add(field).row();
    }

    private static void pattern(Table t, String key, String name) {
        wideText(t, key, name, Constants.Bundles.settingNoPattern, text -> {});
    }

    private static void check(Table t, String key, String name) {
        Cell<CheckBox> check = t.check(name, c -> Core.settings.put(key, c))
                .checked(c -> Core.settings.getBool(key))
                .colspan(columns).growX().padBottom(2f).checked(x -> Core.settings.getBool(key));
        check.get().left();
        check.row();
    }

    private static void button(Table t, String name, Runnable runnable) {
        t.button(name, runnable).height(60f).pad(16f).colspan(columns).width(t.getWidth()).fill().row();
    }

    private static String numberOfSchematicsText() {
        return Core.bundle.format(Constants.DirectBundles.numberOfSchematics, SchematicsUpdater.numberOfSchematics());
    }
}
