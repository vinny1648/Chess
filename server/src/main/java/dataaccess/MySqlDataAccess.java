package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import static dataaccess.DatabaseManager.createDatabase;
import static java.sql.Types.VARCHAR;


public class MySqlDataAccess implements DataAccess{

    public MySqlDataAccess() throws DataAccessException {
        createDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE game")) {
                preparedStatement.executeUpdate();
            }
            try (var preparedStatement = conn.prepareStatement("TRUNCATE authtoken")) {
                preparedStatement.executeUpdate();
            }
            try (var preparedStatement = conn.prepareStatement("TRUNCATE user")) {
                preparedStatement.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new DataAccessException("Unable to update database: " + e.getMessage());
        }
    };

    @Override
    public void saveUser(UserData user) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO user (username, password, email) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, user.password());
                preparedStatement.setString(3, user.email());

                preparedStatement.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new DataAccessException("Unable to update database: " + e.getMessage());
        }
    };

    @Override
    public UserData getUser(String requestedUsername) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
                preparedStatement.setString(1, requestedUsername);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        String password = rs.getString("password");
                        String email = rs.getString("email");

                        return new UserData(username, password, email);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to query database: " + e.getMessage());
        }
    };

    @Override
    public void saveAuthToken(AuthData authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO authtoken (username, token) VALUES (?, ?)")) {
                preparedStatement.setString(1, authToken.username());
                preparedStatement.setString(2, authToken.authToken());

                preparedStatement.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new DataAccessException("Unable to update database: " + e.getMessage());
        }
    };

    @Override
    public String checkAuthToken(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT username, token FROM authtoken WHERE token=?")) {
                preparedStatement.setString(1, authToken);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to query database: " + e.getMessage());
        }
    };

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM authtoken WHERE token=?")) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to query database: " + e.getMessage());
        }
    };

    @Override
    public void createGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO game (gameid, whiteusername, blackusername, gamename, chessgame) VALUES (?, ?, ?, ?, ?)")) {
                preparedStatement.setInt(1, game.gameID());
                if (game.whiteUsername() != null) {
                    preparedStatement.setString(2, game.whiteUsername());
                } else {
                    preparedStatement.setNull(2, VARCHAR);
                }

                if (game.blackUsername() != null) {
                    preparedStatement.setString(3, game.blackUsername());
                } else {
                    preparedStatement.setNull(3, VARCHAR);
                }
                preparedStatement.setString(4, game.gameName());
                var json = new Gson().toJson(game.game());
                preparedStatement.setString(5, json);

                preparedStatement.executeUpdate();
            }
        }
        catch (Exception e) {
            throw new DataAccessException("Unable to update database: " + e.getMessage());
        }
    };

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT whiteusername, blackusername, gamename, chessgame FROM game WHERE gameid=?")) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        String whiteUsername = rs.getString("whiteusername");
                        String blackUsername = rs.getString("blackusername");
                        String gameName = rs.getString("gamename");
                        String chessGameSerialized = rs.getString("chessgame");
                        ChessGame chessGame = new Gson().fromJson(chessGameSerialized, ChessGame.class);

                        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to query database: " + e.getMessage());
        }
    };

    @Override
    public Collection<GameData> getGameList() throws DataAccessException {
        Collection<GameData> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT gameid, whiteusername, blackusername, gamename FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new GameData(rs.getInt("gameid"), rs.getString("whiteusername"), rs.getString("blackusername"), rs.getString("gamename"), null));
                    }
                }
            }
        } catch (Exception e) {
        throw new DataAccessException("Unable to read data: " + e.getMessage());
    }
        return result;

    };

    @Override
    public void removeGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DELETE FROM game WHERE gameid=?")) {
                preparedStatement.setInt(1, gameID);
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to query database: " + e.getMessage());
        }
    };
}
