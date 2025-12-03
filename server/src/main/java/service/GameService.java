package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    private int getID () throws DataAccessException {
        int id = (int)(Math.random() * 10000) + 1;
        while (dataAccess.getGame(id) != null) {
            id = (int)(Math.random() * 10000) + 1;
        }
        return id;
    }
    public int createGame(GameData game) throws DataAccessException {
        if (game.gameName() == null) {
            throw new BadRequestException("Game name must include characters");
        }
        int id = getID();
        GameData newGame = new GameData(id, null, null, game.gameName(), new ChessGame());
        dataAccess.createGame(newGame);
        return id;
    }
    public Collection<GameView> listGames() throws DataAccessException {
        ArrayList<GameView> games = new ArrayList<>();
        for (GameData game: dataAccess.getGameList()) {
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();
            GameView gameView = new GameView(game.gameID(), whiteUsername, blackUsername, game.gameName());
            games.add(gameView);
        }
        return games;
    }
    public GameData joinGame(JoinRequest joinRequest, String username) throws DataAccessException {
        GameData game = dataAccess.getGame(joinRequest.gameID());
        GameData adjustedGame;
        if (game == null) {
            throw new BadRequestException("No game with requested ID");
        }
        if (joinRequest.playerColor() == null) {
            adjustedGame = game;
        }
        if (Objects.equals(joinRequest.playerColor(), "WHITE")) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("Color already taken");
            }
            adjustedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        }
        else if (Objects.equals(joinRequest.playerColor(), "BLACK")) {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("Color already taken");
            }
            adjustedGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }
        else if (Objects.equals(joinRequest.playerColor(), null)) {
            adjustedGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        }
        else {
            throw new BadRequestException("No exsisting team color");
        }
        dataAccess.removeGame(game.gameID());
        dataAccess.createGame(adjustedGame);
        return adjustedGame;
    }
}
