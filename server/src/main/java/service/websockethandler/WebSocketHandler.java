package service.websockethandler;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (userGameCommand.getCommandType()) {
                case CONNECT -> connect(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
                case MAKE_MOVE -> makeMove(userGameCommand.getAuthToken(), userGameCommand.getGameID(), userGameCommand.getMove(), ctx.session);
                case LEAVE -> leave(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
                case RESIGN -> resign(userGameCommand.getAuthToken(), userGameCommand.getGameID(), ctx.session);
            }
        } catch (IOException | DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String authToken, Integer gameID, Session session) throws IOException, DataAccessException {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        if (dataAccess.checkAuthToken(authToken) == null) {
            msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            msg.setErrorMessage("Unauthorized Connection");
            session.getRemote().sendString(gson.toJson(msg));
            throw  new DataAccessException("Unauthorized Connection");
        }
        connections.add(session, gameID);
        GameData data = dataAccess.getGame(gameID);
        ChessGame game = data.game();
        String message = "Connected to game: " + data.gameName();

        msg.setGame(game);
        msg.setMessage(message);

        connections.broadcast(gameID, msg);
    }
    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws IOException {
        String message = "";
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        connections.broadcast(gameID, notification);
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException, DataAccessException {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        if (dataAccess.checkAuthToken(authToken) == null) {
            msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            msg.setErrorMessage("Unauthorized");
            session.getRemote().sendString(gson.toJson(msg));
            throw  new DataAccessException("Unauthorized Disconnect");
        }
        String message = dataAccess.checkAuthToken(authToken) + "left the game.";
        msg.setMessage(message);
        connections.remove(session, gameID);
        connections.broadcast(gameID, msg);
    }
    private void resign(String authToken, Integer gameID, Session session) throws IOException {
        var message = "";
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(gameID, notification);
        connections.remove(session, gameID);
    }
}