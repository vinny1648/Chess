package service;

import chess.ChessGame;
import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import datamodel.GameView;
import model.*;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {

    private DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    private int getID (){
        int id = (int)(Math.random() * 10000) + 1;
        while (dataAccess.getGame(id) != null) {
            id = (int)(Math.random() * 10000) + 1;
        }
        return id;
    }
    public int createGame(model.GameData game) {
        if (game.gameName() == null) {
            throw new BadRequestException("Game name must include characters");
        }
        int id = getID();
        GameData newGame = new GameData(id, null, null, game.gameName(), new ChessGame());
        dataAccess.createGame(newGame);
        return id;
    }
    public Collection<GameView> listGames() {
        ArrayList<GameView> games = new ArrayList<>();
        for (GameData game: dataAccess.getGameList()) {
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();
            if (whiteUsername == null) {
                whiteUsername = "";
            }
            if (blackUsername == null) {
                blackUsername = "";
            }
            GameView gameView = new GameView(game.gameID(), whiteUsername, blackUsername, game.gameName());
            games.add(gameView);
        }
        return games;
    }
}
