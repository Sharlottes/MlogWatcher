package mlogwatcher;

import arc.Events;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mlogwatcher.ui.ProcessorIdLabel;
import mlogwatcher.ui.SchematicsDialogSubclass;
import mlogwatcher.ui.fragments.ElementInjector;
import mlogwatcher.websocket.MlogServer;

public class MlogWatcher extends Mod {
    public MlogWatcher() {
        ProcessorIdLabel.init();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            Settings.init();
            ProcessorIdLabel.updateVariables();
            ProcessorUpdater.init();
            SchematicsDialogSubclass.init();
            ElementInjector.init();
            FileWatcher.startWatcherThread();
            MlogServer.startServer();
        });

        Events.on(EventType.DisposeEvent.class, e -> {
            FileWatcher.stopWatcherThread();
            MlogServer.stopServer();
        });
    }
}
