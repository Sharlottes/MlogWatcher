package mlogwatcher;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.graphics.Pal;
import mindustry.world.blocks.logic.LogicBlock;

public class ProcessorUpdater {
    @Nullable
    private static LogicBlock.LogicBuild lastTappedLogicBuild = null;

    public static void init() {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.tile.build instanceof LogicBlock.LogicBuild logicBuild) {
                lastTappedLogicBuild = logicBuild;
            } else {
                lastTappedLogicBuild = null;
            }
        });

        Events.run(EventType.Trigger.draw, () -> {
            if (lastTappedLogicBuild == null) return;
            Draw.reset();
            Lines.stroke(1f);
            Draw.color(Pal.accent);
            Lines.poly(lastTappedLogicBuild.x, lastTappedLogicBuild.y, 4, 8f);
            Draw.reset();
        });
    }

    public static void InsertLogic() {
        String mlogPath = Core.settings.getString(Constants.Settings.mlogPath);
        String asmCode = Fi.get(mlogPath).readString().replace("\r\n", "\n");
        InsertLogic(asmCode);
    }

    public static void InsertLogic(String asmCode) {
        if (lastTappedLogicBuild == null) {
            Log.warn("cannot find any selected logic block!");
            return;
        }

        lastTappedLogicBuild.configure(LogicBlock.compress(asmCode, lastTappedLogicBuild.relativeConnections()));
        Fx.spawn.at(lastTappedLogicBuild.x, lastTappedLogicBuild.y);
    }
}
