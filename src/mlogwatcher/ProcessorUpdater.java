package mlogwatcher;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.graphics.Pal;
import mindustry.logic.LVar;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicBuild;
import mlogwatcher.websocket.api.LogicProcessor;
import mlogwatcher.websocket.api.ProcessorUpdateResults;
import mlogwatcher.websocket.api.ProgramId;
import mlogwatcher.websocket.api.Response;

import java.util.ArrayList;
import java.util.List;

public class ProcessorUpdater {
    @Nullable
    private static LogicBuild lastTappedLogicBuild = null;

    public static void init() {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.tile.build instanceof LogicBuild logicBuild && accessible(logicBuild)) {
                lastTappedLogicBuild = logicBuild;
            } else {
                lastTappedLogicBuild = null;
            }
        });

        Events.run(EventType.Trigger.draw, () -> {
            if (lastTappedLogicBuild == null || lastTappedLogicBuild.dead) return;
            Draw.reset();
            Lines.stroke(1f);
            Draw.color(Pal.accent);
            Lines.poly(lastTappedLogicBuild.x, lastTappedLogicBuild.y, 4, 8f);
            Draw.reset();
        });

        Events.on(EventType.ResetEvent.class, e -> lastTappedLogicBuild = null);
    }

    public static boolean accessible(LogicBuild logicBuild) {
        return ((LogicBlock) logicBuild.block).accessible();
    }

    public static String insertLogic(String asmCode) {
        if (lastTappedLogicBuild == null || lastTappedLogicBuild.dead || !accessible(lastTappedLogicBuild)) {
            Log.warn("[MlogWatcher] cannot find any selected logic block!");
            return Response.ERR_NO_PROCESSOR_ATTACHED;
        }

        return insertLogic(lastTappedLogicBuild, asmCode);
    }

    public static String insertLogic(@Nullable LogicBuild build, String asmCode) {
        if (build == null || build.dead) {
            Log.warn("[MlogWatcher] cannot find any selected logic block!");
            return Response.ERR_NO_PROCESSOR_ATTACHED;
        }

        byte[] compressed = LogicBlock.compress(asmCode, build.relativeConnections());

        // Intentionally not using the in-game constant to be compatible with older releases as well
        if (compressed.length > 16_000) {
            Log.warn("[MlogWatcher] code size too large!");

            return Response.ERR_CODE_SIZE_TOO_LARGE;
        }

        build.configure(compressed);
        Fx.spawn.at(build.x, build.y);
        Log.info("[MlogWatcher] successfully injected code into logic block");

        return Response.STATUS_SUCCESS;
    }

    public static String extractLogic() {
        return extractLogic(lastTappedLogicBuild);
    }

    private static String extractLogic(LogicBuild build) {
        if (build == null || build.dead || !accessible(build)) {
            Log.warn("[MlogWatcher] cannot find any selected logic block!");
            return null;
        } else {
            Fx.colorTrail.at(build.x, build.y, 4f * build.block.size, Color.gold);
            return build.code;
        }
    }

    public enum VersionSelection {exact, compatible, any}

    public static ProcessorUpdateResults updateAllProcessorsOnMap(String asmCode, ProgramId newId, String variableName,
            VersionSelection versionSelection) {
        List<LogicProcessor> updates = new ArrayList<>();
        Groups.build.forEach(b -> {
            if (b instanceof LogicBuild logicBuild && accessible(logicBuild)) {
                updateBuild(logicBuild, asmCode, newId, variableName, versionSelection, updates);
            }
        });
        return new ProcessorUpdateResults(updates);
    }

    private static void updateBuild(LogicBuild logicBuild, String asmCode, ProgramId newId, String variableName,
                                    VersionSelection versionSelection, List<LogicProcessor> updates) {
        LVar lVar = logicBuild.executor.optionalVar(variableName);
        if (lVar != null && lVar.obj() instanceof String id) {
            ProgramId oldId = ProgramId.parse(id);

            final String updateStatus;
            if (oldId == null) {
                updateStatus = LogicProcessor.MISSING_PROGRAM_ID;
            } else if (oldId.getIdPrefix().equals(newId.getIdPrefix())) {
                if (shouldUpdate(oldId, newId, versionSelection)) {
                    String result = insertLogic(logicBuild, asmCode);
                    updateStatus = Response.isSuccess(result) ? LogicProcessor.UPDATED : result;
                } else {
                    updateStatus = LogicProcessor.INCOMPATIBLE_VERSION;
                }
            } else {
                return;
            }

            updates.add(new LogicProcessor(logicBuild.tile.x, logicBuild.tile.y, logicBuild.block.name, oldId, updateStatus));
        }
    }

    public static boolean shouldUpdate(ProgramId oldId, ProgramId newId, VersionSelection versionSelection) {
        return oldId.getIdPrefix().equals(newId.getIdPrefix()) && switch (versionSelection) {
            case exact -> oldId.exactVersionMatch(newId);
            case compatible -> oldId.compatibleVersionMatch(newId);
            case any -> true;
        };
    }
}
