package mlogjswatcher;

import arc.Core;
import arc.scene.Group;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.*;

import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class Setting {
    public static void init(){
        Vars.ui.settings.shown(() -> {
            Dialog dialog = new BaseDialog("MlogJs Watcher Setting");
            dialog.addCloseButton();
            dialog.cont.center();
            dialog.cont.add("current dir").row();
            dialog.cont.add(Core.settings.getString("mlogjswatcher-mlogjs-path", "[lightgray]none[]")).fontScale(0.75f).row();
            dialog.cont.button("Change watcher target", () -> {
                Vars.platform.showFileChooser(true, "open target file to watch", "mlog", fi -> {
                    Core.settings.put("mlogjswatcher-mlogjs-path", fi.absolutePath());
                    FileWatcher.stopWatcherThread();
                    FileWatcher.startWatcherThread();
                });
            }).width(280f).height(60f).pad(16f);

            Table settingUi = (Table)((Group)((Group)(Vars.ui.settings.getChildren().get(1))).getChildren().get(0)).getChildren().get(0); //This looks so stupid lol
            settingUi.row();
            settingUi.button("MlogJs Watcher", Styles.cleart, dialog::show);
        });
    }
}
