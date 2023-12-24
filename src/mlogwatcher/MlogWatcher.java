package mlogwatcher;

import arc.Events;
import mindustry.mod.Mod;
import mindustry.game.EventType;

public class MlogWatcher extends Mod {
    public MlogWatcher(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            Setting.init();
            ProcessorUpdater.init();
            FileWatcher.startWatcherThread();
            MlogServer.startServer();
        });

        Events.on(EventType.DisposeEvent.class, e -> {
            FileWatcher.stopWatcherThread();
            MlogServer.stopServer();
        });
    }
}
