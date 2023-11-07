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
        Label label = new Label(Core.settings.getString("mlogwatcher-mlog-path", "[lightgray]none[]"));
        label.setFontScale(0.75f);

        TextField field = new TextField();
        field.setText("mlog");
        field.setFilter((f, c) -> c != ' ' && c != '.');
        field.setMessageText("no ext");

        Dialog dialog = new BaseDialog("MlogWatcher Setting");
        dialog.addCloseButton();
        dialog.cont.center();
        dialog.cont.add("current mlog target path").row();
        dialog.cont.add(label).row();
        dialog.cont.button("Change watcher target", () -> {
            Vars.platform.showFileChooser(true, "open target file to watch", field.getText(), fi -> {
                label.setText(fi.absolutePath());
                Core.settings.put("mlogwatcher-mlog-path", fi.absolutePath());
                FileWatcher.stopWatcherThread();
                FileWatcher.startWatcherThread();
            });
        }).width(280f).height(60f).pad(16f).row();
        dialog.cont.table(fieldTable -> {
            fieldTable.add("file extension to search: ");
            fieldTable.add(field);
        });

        Vars.ui.settings.shown(() -> {
            Table settingUi = (Table)((Group)((Group)(Vars.ui.settings.getChildren().get(1))).getChildren().get(0)).getChildren().get(0); //This looks so stupid lol
            settingUi.row();
            settingUi.button("Mlog Watcher", Styles.cleart, dialog::show);
        });
    }
}
