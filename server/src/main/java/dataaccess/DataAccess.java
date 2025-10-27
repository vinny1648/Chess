package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {

    void clear();

    void saveUser(UserData user);

    UserData getUser(String username);

    void saveAuthToken(AuthData authToken);

    String checkAuthToken(String authToken);

    void deleteAuthToken(String authToken);

    void createGame(GameData game);

    GameData getGame(int gameID);

    Collection<GameData> getGameList();

    void removeGame(int gameID);
}
