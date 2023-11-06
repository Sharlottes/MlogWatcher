package mlogjswatcher;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Nullable;

import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.world.blocks.logic.LogicBlock;

public class ProcessorUpdater {
    @Nullable
    private static LogicBlock.LogicBuild lastTappedLogicBuild;

    public static void init() {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.tile.build instanceof LogicBlock.LogicBuild logicBuild) {
                lastTappedLogicBuild = logicBuild;
            }
        });
    }

    static void InsertLogic() {
        if (lastTappedLogicBuild == null) {
            Log.warn("cannot find any selected logic block!");
            return;
        }

        String mlogjsPath = Core.settings.getString("mlogjswatcher-mlogjs-path");
        String asmCode = Fi.get(mlogjsPath).readString().replace("\r\n", "\n");
        lastTappedLogicBuild.configure(LogicBlock.compress(asmCode, lastTappedLogicBuild.relativeConnections()));
        Fx.spawn.at(lastTappedLogicBuild.x, lastTappedLogicBuild.y);
    }
}
