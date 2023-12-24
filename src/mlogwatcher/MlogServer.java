package mlogwatcher;

import arc.Core;
import arc.util.Log;
import arc.util.Nullable;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class MlogServer extends WebSocketServer {
    @Nullable private static MlogServer server;

    MlogServer(int port) {
        super(new InetSocketAddress(port));
    }

    public static void startServer() {
        if(server != null) return;
        server = new MlogServer(Core.settings.getInt(Constants.Settings.websocketPort));
        server.start();
    }

    public static void stopServer() {
        if(server == null) return;

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
    }

    @Override
    public void onStart() {
        Log.info("[MlogWatcher] server running on port @", getPort());
    }
}
