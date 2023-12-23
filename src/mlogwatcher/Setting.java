package mlogwatcher;

import arc.Core;
import arc.scene.Group;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.*;

import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class Setting {
    public static void init(){
        Label label = new Label(Core.settings.getString(Constants.Settings.mlogPath, "[lightgray]@none[]"));
        label.setFontScale(0.75f);

        TextField field = new TextField();
        field.setText("mlog");
        field.setFilter((f, c) -> c != ' ' && c != '.');
        field.setMessageText("no ext");

        Vars.ui.settings.addCategory("Mlog Watcher", t -> {
            t.add(Constants.Bundles.settingMlogPathLabel).row();
            t.add(label).row();
            t.button(Constants.Bundles.settingMlogSelectButton, () -> {
                Vars.platform.showFileChooser(true, Constants.Bundles.settingFileChooserTitle, field.getText(), fi -> {
                    label.setText(fi.absolutePath());
                    Core.settings.put(Constants.Settings.mlogPath, fi.absolutePath());
                    FileWatcher.stopWatcherThread();
                    FileWatcher.startWatcherThread();
                });
            }).width(280f).height(60f).pad(16f).row();
            t.table(fieldTable -> {
                fieldTable.add(Constants.Bundles.settingExtensionInputLabel);
                fieldTable.add(field);
            });
        });
    }
}
