package mlogwatcher;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Group;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.*;

import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;

public class Setting {
    public static void init() {
        Core.settings.defaults(
                Constants.Settings.mlogPath, "[lightgray]@none[]",
                Constants.Settings.websocketPort, 9992
        );

        Vars.ui.settings.addCategory("Mlog Watcher", t -> {
            Label label = new Label(() -> Core.settings.getString(Constants.Settings.mlogPath));
            label.setFontScale(0.75f);

            TextField extensionTextField = new TextField();
            extensionTextField.setText("mlog");
            extensionTextField.setFilter((f, c) -> c != ' ' && c != '.');
            extensionTextField.setMessageText("no ext");

            t.add(Constants.Bundles.settingMlogPathLabel).left().colspan(2).row();
            t.add(label).left().colspan(2).row();
            t.button(Constants.Bundles.settingMlogSelectButton, () -> {
                Vars.platform.showFileChooser(true, Constants.Bundles.settingFileChooserTitle, extensionTextField.getText(), fi -> {
                    Core.settings.put(Constants.Settings.mlogPath, fi.absolutePath());
                    FileWatcher.stopWatcherThread();
                    FileWatcher.startWatcherThread();
                });
            }).height(60f).pad(16f).colspan(2).fill().row();
            t.add(Constants.Bundles.settingExtensionInputLabel).left();
            t.add(extensionTextField);
            t.row();

            TextField portTextField = new TextField();
            portTextField.update(() -> portTextField.setText(String.valueOf(Core.settings.getInt(Constants.Settings.websocketPort))));
            portTextField.changed(() -> {
                try {
                    int port = Integer.parseInt(portTextField.getText());
                    Core.settings.put(Constants.Settings.websocketPort, port);
                } catch(NumberFormatException ignored) {

                }
            });

            t.add(Constants.Bundles.settingWebsocketPortLabel).left();
            t.add(portTextField);
            t.row();

            t.button("Restart websocket server", () -> {
                MlogServer.stopServer();
                MlogServer.startServer();
            }).height(60f).pad(16f).colspan(2).fill().row();

            t.button("@settings.reset", () -> {
                Core.settings.remove(Constants.Settings.mlogPath);
                Core.settings.remove(Constants.Settings.websocketPort);
            }).margin(14).width(240f).pad(6).colspan(2).row();
        });
    }
}
