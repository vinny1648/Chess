package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {

    void clear();

    void saveUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void saveAuthToken(AuthData authToken) throws DataAccessException;

    String checkAuthToken(String authToken) throws DataAccessException;

    void deleteAuthToken(String authToken) throws DataAccessException;

    void createGame(GameData game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    Collection<GameData> getGameList() throws DataAccessException;

    void removeGame(int gameID) throws DataAccessException;
}
