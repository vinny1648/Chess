package dataaccess;


import model.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Tests {

    DataAccess dataAccess;
    UserData userOne;
    UserData userTwo;
    GameData gameOne;
    GameData gameTwo;
    AuthData authOne;
    AuthData authTwo;

    @BeforeAll
    public static void init() {

    }
    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        userOne = new UserData("Xx_gamer_xX", "fortNITE36", "gamerguy@gmail.com");
        userTwo = new UserData("BigGuy69", "WetDart36", "guywholikestrians@hotmail.com");
        gameOne = new GameData(1243, null, null, "CoolGame", null);
        gameTwo = new GameData(6789, null, null, "ChessMasterz", null);
        authOne = new AuthData(UUID.randomUUID().toString(), userOne.username());
        authTwo = new AuthData(UUID.randomUUID().toString(), userTwo.username());
        dataAccess.clear();
    }
    @Test
    @Order(1)
    @DisplayName("Clear Positive")
    public void clearPositiveTest() throws DataAccessException {
        dataAccess.saveUser(userOne);
        dataAccess.clear();
        Assertions.assertNull(dataAccess.getUser(userOne.username()));

    }
    @Test
    @Order(2)
    @DisplayName("saveUser Positive")
    public void saveUserPositiveTest() throws DataAccessException {
        dataAccess.saveUser(userOne);
        Assertions.assertEquals(dataAccess.getUser(userOne.username()), userOne);
    }
    @Test
    @Order(3)
    @DisplayName("saveUser Negative")
    public void saveUserNegativeTest() throws DataAccessException {
        dataAccess.saveUser(userOne);
        Assertions.assertNotEquals(dataAccess.getUser(userOne.username()), userTwo);
    }
    @Test
    @Order(4)
    @DisplayName("getUser Positive")
    public void getUserPositiveTest() throws DataAccessException {
        dataAccess.saveUser(userTwo);
        Assertions.assertEquals(dataAccess.getUser(userTwo.username()), userTwo);
    }
    @Test
    @Order(5)
    @DisplayName("getUser Negative")
    public void getUserNegativeTest() throws DataAccessException {
        dataAccess.saveUser(userTwo);
        Assertions.assertNotEquals(dataAccess.getUser(userTwo.username()), userOne);
    }
    @Test
    @Order(6)
    @DisplayName("saveAuth Positive")
    public void saveAuthPositiveTest() throws DataAccessException {
        dataAccess.saveAuthToken(authOne);
        Assertions.assertEquals(dataAccess.checkAuthToken(authOne.authToken()), userOne.username());
    }
    @Test
    @Order(7)
    @DisplayName("saveAuth Negative")
    public void saveAuthNegativeTest() throws DataAccessException {
        dataAccess.saveAuthToken(authTwo);
        Assertions.assertNotEquals(dataAccess.checkAuthToken(authTwo.authToken()), userOne.username());
    }
    @Test
    @Order(8)
    @DisplayName("checkAuth Positive")
    public void checkAuthPositiveTest() throws DataAccessException {
        dataAccess.saveAuthToken(authTwo);
        Assertions.assertEquals(dataAccess.checkAuthToken(authTwo.authToken()), userTwo.username());
    }
    @Test
    @Order(9)
    @DisplayName("checkAuth Negative")
    public void checkAuthNegativeTest() throws DataAccessException {
        dataAccess.saveAuthToken(authOne);
        Assertions.assertNotEquals(dataAccess.checkAuthToken(authOne.authToken()), userTwo.username());
    }
    @Test
    @Order(10)
    @DisplayName("deleteAuth Positive")
    public void deleteAuthPositiveTest() throws DataAccessException {
        dataAccess.saveAuthToken(authOne);
        dataAccess.deleteAuthToken(authOne.authToken());
        Assertions.assertNull(dataAccess.checkAuthToken(authOne.authToken()));
    }
    @Test
    @Order(11)
    @DisplayName("deleteAuth Negative")
    public void deleteAuthNegativeTest() throws DataAccessException {
        dataAccess.saveAuthToken(authTwo);
        dataAccess.saveAuthToken(authOne);
        dataAccess.deleteAuthToken(authTwo.authToken());
        Assertions.assertNotNull(dataAccess.checkAuthToken(authOne.authToken()));
    }
    @Test
    @Order(12)
    @DisplayName("createGame Positive")
    public void createGamePositiveTest() throws DataAccessException {
        dataAccess.createGame(gameOne);
        Assertions.assertEquals(dataAccess.getGame(gameOne.gameID()), gameOne);
    }
    @Test
    @Order(13)
    @DisplayName("createGame Negative")
    public void createGameNegativeTest() throws DataAccessException {
        dataAccess.createGame(gameOne);
        Assertions.assertNotEquals(dataAccess.getGame(gameOne.gameID()), gameTwo);
    }
    @Test
    @Order(14)
    @DisplayName("getGame Positive")
    public void getGamePositiveTest() throws DataAccessException {
        dataAccess.createGame(gameTwo);
        Assertions.assertEquals(dataAccess.getGame(gameTwo.gameID()), gameTwo);
    }
    @Test
    @Order(15)
    @DisplayName("getGame Negative")
    public void getGameNegativeTest() throws DataAccessException {
        dataAccess.createGame(gameTwo);
        Assertions.assertNotEquals(dataAccess.getGame(gameTwo.gameID()), gameOne);
    }
    @Test
    @Order(16)
    @DisplayName("getGameList Positive")
    public void getGameListPositiveTest() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        games.add(gameOne);
        games.add(gameTwo);
        dataAccess.createGame(gameOne);
        dataAccess.createGame(gameTwo);
        Assertions.assertEquals(dataAccess.getGameList(), games);
    }
    @Test
    @Order(17)
    @DisplayName("removeGame Positive")
    public void removeGamePositiveTest() throws DataAccessException {
        dataAccess.createGame(gameOne);
        dataAccess.createGame(gameTwo);
        dataAccess.removeGame(gameOne.gameID());
        Assertions.assertNull(dataAccess.getGame(gameOne.gameID()));
    }
    @Test
    @Order(18)
    @DisplayName("removeGame Negative")
    public void removeGameNegativeTest() throws DataAccessException {
        dataAccess.createGame(gameOne);
        dataAccess.createGame(gameTwo);
        dataAccess.removeGame(gameOne.gameID());
        Assertions.assertNotNull(dataAccess.getGame(gameTwo.gameID()));
    }
}

