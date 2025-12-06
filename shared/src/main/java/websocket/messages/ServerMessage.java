package websocket.messages;

import chess.ChessGame;

public class ServerMessage {
    private ServerMessageType serverMessageType;

    private ChessGame game;       // required for tests
    private String message;       // required for tests
    private String errorMessage;  // required for tests

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public ChessGame getGame() {
        return game;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // SETTERS (needed so you can populate fields before Gson serializes)
    public void setGame(ChessGame game) {
        this.game = game;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
