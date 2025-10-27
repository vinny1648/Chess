package dataaccess;

import model.GameData;

import java.util.Collection;

public interface DataAccess {

    void clear();

    void saveUser(model.UserData user);

    model.UserData getUser(String username);

    void saveAuthToken(String authToken, String username);

    String checkAuthToken(String authToken);

    void deleteAuthToken(String authToken);

    void createGame(model.GameData game);

    GameData getGame(int gameID);

    Collection<GameData> getGameList();

    void removeGame(int gameID);
}
