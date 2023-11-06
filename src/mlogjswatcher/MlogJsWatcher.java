package mlogjswatcher;

import arc.Events;
import mindustry.mod.Mod;
import mindustry.game.EventType;

public class MlogJsWatcher extends Mod {
    public MlogJsWatcher(){
        Events.on(EventType.ClientLoadEvent.class, e -> {
            Setting.init();
            ProcessorUpdater.init();
            FileWatcher.startWatcherThread();
        });
    }
}