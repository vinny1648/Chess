package service;

import dataaccess.*;
import datamodel.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Collection;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Tests {

    DataAccess dataAccess;
    GameService gameService;
    UserService userService;
    UserData userOne;
    UserData userTwo;
    LoginUser userOneData;
    LoginUser userTwoData;
    GameData gameOne;
    GameData gameTwo;

    @BeforeAll
    public static void init() {

    }
    @BeforeEach
    public void setup() {
        dataAccess = new MemoryDataAccess();
        gameService = new GameService(dataAccess);
        userService = new UserService(dataAccess);
        userOne = new UserData("Xx_gamer_xX", "fortNITE36", "gamerguy@gmail.com");
        userTwo = new UserData("BigGuy69", "WetDart36", "guywholikestrians@hotmail.com");
        userOneData = new LoginUser("Xx_gamer_xX", "fortNITE36");
        userTwoData = new LoginUser("BigGuy69", "WetDart36");
        gameOne = new GameData(0, null, null, "CoolGame", null);
        gameTwo = new GameData(0, null, null, "ChessMasterz", null);
    }
    @Test
    @Order(1)
    @DisplayName("Clear Positive")
    public void clearPositiveTest() {
        userService.register(userOne);
        userService.register(userTwo);
        int game1ID = gameService.createGame(gameOne);
        int game2ID = gameService.createGame(gameTwo);
        Assertions.assertNotNull(dataAccess.getGame(game1ID));
        Assertions.assertNotNull(dataAccess.getGame(game2ID));
        Assertions.assertNotNull(dataAccess.getUser(userOne.username()));
        Assertions.assertNotNull(dataAccess.getUser(userTwo.username()));
        dataAccess.clear();
        Assertions.assertNull(dataAccess.getGame(game1ID));
        Assertions.assertNull(dataAccess.getGame(game2ID));
        Assertions.assertNull(dataAccess.getUser(userOne.username()));
        Assertions.assertNull(dataAccess.getUser(userTwo.username()));

    }
    @Test
    @Order(2)
    @DisplayName("Register Positive")
    public void registerPositiveTest() {
        Assertions.assertEquals("Xx_gamer_xX", userService.register(userOne).username());
        Assertions.assertEquals("BigGuy69", userService.register(userTwo).username());
    }
    @Test
    @Order(3)
    @DisplayName("Register Negative")
    public void registerNegativeTest() {
        Assertions.assertEquals("Xx_gamer_xX", userService.register(userOne).username());
        Assertions.assertThrows(AlreadyTakenException.class, () -> {
            userService.register(userOne);
        });
    }
    @Test
    @Order(4)
    @DisplayName("Login Positive")
    public void loginPositiveTest() {

        String authTOne = userService.register(userOne).authToken();
        userService.logout(authTOne);
        String authTokenOne = userService.login(userOneData).authToken();
        String authTTwo = userService.register(userTwo).authToken();
        userService.logout(authTTwo);
        String authTokenTwoFirst = userService.login(userTwoData).authToken();
        Assertions.assertNotEquals(authTokenOne, authTokenTwoFirst);
        userService.logout(authTokenTwoFirst);
        String authTokenTwoSecond = userService.login(userTwoData).authToken();
        Assertions.assertNotEquals(authTokenTwoFirst, authTokenTwoSecond);
    }
    @Test
    @Order(5)
    @DisplayName("Login Negative")
    public void loginNegativeTest() {
        String authTOne = userService.register(userOne).authToken();
        userService.logout(authTOne);
        Assertions.assertThrows(IncorrectPasswordException.class, () -> {
            userService.login(userTwoData);
        });
    }
    @Test
    @Order(6)
    @DisplayName("Logout Positive")
    public void logoutPositiveTest() {
        String authToken = userService.register(userOne).authToken();
        Assertions.assertDoesNotThrow(() -> {
            userService.logout(authToken);
        });
    }
    @Test
    @Order(7)
    @DisplayName("Logout Negative")
    public void logoutNegativeTest() {

        userService.register(userOne);
        String falseAuthToken = userService.register(userTwo).authToken();
        userService.logout(falseAuthToken);
        Assertions.assertThrows(UnauthorizedException.class, () -> {
            userService.logout(falseAuthToken);
        });
    }
    @Test
    @Order(8)
    @DisplayName("List Games Positive")
    public void listGamesPositiveTest() {
        gameService.createGame(gameOne);
        gameService.createGame(gameTwo);
        Collection<GameView> games = gameService.listGames();
        Assertions.assertFalse(games.isEmpty());
    }
    @Test
    @Order(9)
    @DisplayName("List Games Negative")
    public void listGamesNegativeTest() {
        Collection<GameView> games = gameService.listGames();
        Assertions.assertTrue(games.isEmpty());
    }
    @Test
    @Order(10)
    @DisplayName("Create Game Positive")
    public void createGamePositiveTest() {
        Assertions.assertDoesNotThrow(() -> {
            gameService.createGame(gameOne);
        });
    }
    @Test
    @Order(11)
    @DisplayName("Create Game Negative")
    public void createGameNegativeTest() {
        GameData noNameGame = new GameData(2355, null, null, null, null);
        Assertions.assertThrows(BadRequestException.class, () -> {
            gameService.createGame(noNameGame);
        });
    }
    @Test
    @Order(12)
    @DisplayName("Join Game Positive")
    public void joinGamePositiveTest() {
        int id = gameService.createGame(gameOne);
        JoinRequest join = new JoinRequest("BLACK", id);
        Assertions.assertDoesNotThrow(() -> {
            gameService.joinGame(join, userOne.username());
        });
    }
    @Test
    @Order(13)
    @DisplayName("Join Game Negative")
    public void joinGameNegativeTest() {
        gameService.createGame(gameOne);
        JoinRequest join = new JoinRequest("BLACK", 4591);
        Assertions.assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(join, userOne.username());
        });
    }
}

