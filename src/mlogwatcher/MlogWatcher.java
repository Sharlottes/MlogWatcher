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

            Thread shutdownThread = new Thread(() -> {
               FileWatcher.stopWatcherThread();

               // needs to be called before closing
                // or else the jvm will keep running
               MlogServer.stopServer();
            });
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        });

        Events.on(EventType.StateChangeEvent.class, e -> {
            switch(e.to) {
                case menu -> MlogServer.stopServer();
                case playing -> MlogServer.startServer();
            }
        });
    }
}
