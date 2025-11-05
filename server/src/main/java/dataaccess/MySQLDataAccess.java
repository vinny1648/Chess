package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

import static dataaccess.DatabaseManager.createDatabase;

public class MySQLDataAccess implements DataAccess{

    public void MySqlDataAccess() throws DataAccessException {
        createDatabase();
    }

    @Override
    public void clear(){

    };

    @Override
    public void saveUser(UserData user){

    };

    @Override
    public UserData getUser(String username){

    };

    @Override
    public void saveAuthToken(AuthData authToken){

    };

    @Override
    public String checkAuthToken(String authToken){

    };

    @Override
    public void deleteAuthToken(String authToken){

    };

    @Override
    public void createGame(GameData game){

    };

    @Override
    public GameData getGame(int gameID){

    };

    @Override
    public Collection<GameData> getGameList(){

    };

    @Override
    public void removeGame(int gameID){

    };
}
