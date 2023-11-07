package mlogwatcher;

import arc.Core;
import arc.scene.Group;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;

import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class Setting {
    public static void init(){
        Dialog dialog = new BaseDialog("MlogWatcher Setting");
        dialog.addCloseButton();
        dialog.cont.center();
        dialog.cont.add("current .mlog target path").row();
        Label label = dialog.cont.add(Core.settings.getString("mlogwatcher-mlog-path", "[lightgray]none[]")).fontScale(0.75f).get();
        dialog.row();
        dialog.cont.button("Change watcher target", () -> {
            Vars.platform.showFileChooser(true, "open target file to watch", "mlog", fi -> {
                label.setText(fi.absolutePath());
                Core.settings.put("mlogwatcher-mlog-path", fi.absolutePath());
                FileWatcher.stopWatcherThread();
                FileWatcher.startWatcherThread();
            });
        }).width(280f).height(60f).pad(16f);

        Vars.ui.settings.shown(() -> {
            Table settingUi = (Table)((Group)((Group)(Vars.ui.settings.getChildren().get(1))).getChildren().get(0)).getChildren().get(0); //This looks so stupid lol
            settingUi.row();
            settingUi.button("Mlog Watcher", Styles.cleart, dialog::show);
        });
    }
}
