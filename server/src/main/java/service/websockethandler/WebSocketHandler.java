package service.websockethandler;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
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
import java.util.Map;
import java.util.Objects;

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
        String playerName = dataAccess.checkAuthToken(authToken);
        if (playerName == null) {
            msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            msg.setErrorMessage("Unauthorized Connection");
            session.getRemote().sendString(gson.toJson(msg));
            throw  new DataAccessException("Unauthorized Connection");
        }
        connections.add(session, gameID);
        GameData data = dataAccess.getGame(gameID);
        ChessGame game = data.game();
        String message = playerName + " connected to " + data.gameName() + " as ";
        if (playerName.equals(data.whiteUsername())) {
            message += "WHITE player.";
        }
        else if (playerName.equals(data.blackUsername())) {
            message += "BLACK player";
        }
        else {
            message += "an observer";
        }
        msg.setGame(game);
        msg.setMessage(message);

        connections.broadcast(gameID, msg);
    }
    private void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws IOException, DataAccessException {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        if (dataAccess.checkAuthToken(authToken) == null) {
            msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            msg.setErrorMessage("Unauthorized Connection");
            session.getRemote().sendString(gson.toJson(msg));
            throw  new DataAccessException("Unauthorized Connection");
        }
        GameData data = dataAccess.getGame(gameID);
        ChessGame game = data.game();
        String playerTurn;
        ChessGame.TeamColor afterMove;
        if (game.getTeamTurn() == ChessGame.TeamColor.WHITE) {
            playerTurn = data.whiteUsername();
            afterMove = ChessGame.TeamColor.BLACK;
        }
        else {
            playerTurn = data.blackUsername();
            afterMove = ChessGame.TeamColor.WHITE;
        }
        String message;
        if (!Objects.equals(playerTurn, dataAccess.checkAuthToken(authToken))) {
            message = "It is not your turn";
            msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            msg.setErrorMessage(message);
            session.getRemote().sendString(gson.toJson(msg));
        } else {
            try {
                if (game.gameIsOver()) {
                    throw new InvalidMoveException("Game is Over.");
                }
                game.makeMove(move);
                Map<Integer, String> vtranslation = Map.of(
                        1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6, "f", 7, "g", 8, "h"
                );
                message = playerTurn + " moved " + vtranslation.get(move.getStartPosition().getRow()) + move.getStartPosition().getColumn() +
                        " to " + vtranslation.get(move.getEndPosition().getRow()) + move.getEndPosition().getColumn();
                if (game.isInCheck(afterMove)) {
                    message += " CHECK!";
                }
                if (game.isInCheckmate(afterMove)) {
                    message += "\nCHECKMATE! " + playerTurn + "HAS WON";
                }
                if (game.isInStalemate(afterMove)) {
                    message += "\nStalemate. GAME DRAW";
                }
                data = new GameData(gameID, data.whiteUsername(), data.blackUsername(), data.gameName(), game);
                dataAccess.removeGame(gameID);
                dataAccess.createGame(data);
                msg.setMessage(message);
                msg.setGame(game);
                connections.broadcast(gameID, msg);
            } catch (InvalidMoveException e) {
                msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                message = "Move can not be made. " + e.getMessage();
                msg.setErrorMessage(message);
                session.getRemote().sendString(gson.toJson(msg));
            }

        }
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException, DataAccessException {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        String playerName = dataAccess.checkAuthToken(authToken);
        if (playerName == null) {
            msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            msg.setErrorMessage("Unauthorized");
            session.getRemote().sendString(gson.toJson(msg));
            throw  new DataAccessException("Unauthorized Disconnect");
        }
        GameData data = dataAccess.getGame(gameID);
        String message = playerName + " left the game.";
        msg.setMessage(message);
        connections.remove(session, gameID);
        if (Objects.equals(dataAccess.getGame(gameID).whiteUsername(), playerName)) {
            dataAccess.removeGame(gameID);
            dataAccess.createGame(new GameData(gameID, null, data.blackUsername(), data.gameName(), data.game()));
        } else if (Objects.equals(dataAccess.getGame(gameID).whiteUsername(), playerName)) {
            dataAccess.removeGame(gameID);
            dataAccess.createGame(new GameData(gameID, data.blackUsername(), null, data.gameName(), data.game()));
        }
        if (connections.gameEmpty(gameID) && data.game().gameIsOver()) {
            dataAccess.removeGame(gameID);
        } else {
            connections.broadcast(gameID, msg);
        }
        message = "left game.";
        msg.setMessage(message);
        session.getRemote().sendString(gson.toJson(msg));
    }
    private void resign(String authToken, Integer gameID, Session session) throws IOException {

        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(gameID, notification);
        connections.remove(session, gameID);
    }
}