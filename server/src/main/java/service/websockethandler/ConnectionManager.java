package service.websockethandler;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ArrayList<Session>> connections = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    public void add(Session session, Integer gameID) {
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ArrayList<>());
        }
        connections.get(gameID).add(session);
    }

    public void remove(Session session, Integer gameID) {
        connections.get(gameID).remove(session);
        if (connections.get(gameID).isEmpty()) {
            connections.remove(gameID);
        }
    }

    public boolean gameEmpty(Integer gameID) {
        return !connections.containsKey(gameID);
    }

    public void broadcast(Integer gameID, ServerMessage notification) throws IOException {
        String msg = gson.toJson(notification);
        for (Session c : connections.get(gameID)) {
            if (c.isOpen()) {
                c.getRemote().sendString(msg);
            }
        }
    }
}