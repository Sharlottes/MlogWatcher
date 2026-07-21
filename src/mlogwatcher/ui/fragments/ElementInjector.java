package mlogwatcher.ui.fragments;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mlogwatcher.Constants;
import mlogwatcher.FileWatcher;

public class ElementInjector {
    public static void init() {
        Core.app.post(() -> {
            Vars.ui.editor.shown(() -> {
               Table mid = (Table) ((Table) Vars.ui.editor
                                .getChildren().get(0)) // cont
                                .getChildren().get(0);

               mid.row();
               mid.button(Constants.Bundles.forceUpdateAll, Icon.refresh, Styles.flatt, FileWatcher::forceUpdateAll)
                  .growX().margin(9f);
            });
        });
    }
}
