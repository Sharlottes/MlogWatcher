package mlogwatcher;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.BindException;
import java.net.InetSocketAddress;

public class MlogServer extends WebSocketServer {
    @Nullable
    private static MlogServer server;

    MlogServer(int port) {
        super(new InetSocketAddress(port));
    }

    public static void startServer() {
        if (server != null) return;
        server = new MlogServer(Core.settings.getInt(Constants.Settings.websocketPort));
        server.start();
    }

    public static void stopServer() {
        if (server == null) return;

        try {
            server.stop();
        } catch (InterruptedException ignored) {

        }
        server = null;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        ProcessorUpdater.InsertLogic(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.err("[MlogWatcher] socket error", ex);

        if (ex instanceof BindException) {
            boolean ignore = Core.settings.getBool(Constants.Settings.ignoreServerBindError);
            if(ignore) return;

            (new Dialog(Constants.Bundles.infoServerBindErrorTitle) {
                {
                    this.getCell(this.cont).growX();
                    this.cont.margin(15.0F).add(Constants.Bundles.infoServerBindError).width(400.0F).wrap().get().setAlignment(1, 1);
                    this.buttons.button("@ok", this::hide).size(110.0F, 50.0F).pad(4.0F);
                    this.keyDown(KeyCode.enter, this::hide);
                    this.closeOnBack();
                    this.row();
                    this.check(Constants.Bundles.settingIgnoreServerBindError, (checked) -> {
                        Core.settings.put(Constants.Settings.ignoreServerBindError, checked);
                    }).checked(Core.settings.getBool(Constants.Settings.ignoreServerBindError));
                }
            }).show();
        }
    }

    @Override
    public void onStart() {
        Log.info("[MlogWatcher] server running on port @", getPort());
    }
}
