package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;



public class MemoryDataAccess implements DataAccess{
    private HashMap<String, model.UserData> users = new HashMap<>();
    private HashMap<String, String> loginTokens = new HashMap<>();
    private HashMap<Integer, model.GameData> games = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
        loginTokens.clear();
        games.clear();
    }
    @Override
    public void saveUser(model.UserData user) {
        users.put(user.username(), user);
    }
    @Override
    public model.UserData getUser(String username) {
        return users.get(username);
    }
    @Override
    public void saveAuthToken(String authToken, String username) {
        loginTokens.put(authToken, username);
    }
    @Override
    public String checkAuthToken(String authToken) {
        return loginTokens.get(authToken);
    }
    @Override
    public void deleteAuthToken(String authToken) {
        loginTokens.remove(authToken);
    }

    @Override
    public void createGame(model.GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> getGameList() {
        Collection<GameData> gameList = new ArrayList<>(games.values());
        return gameList;
    }
    public void removeGame(int gameID) {
        games.remove(gameID);
    }
}
