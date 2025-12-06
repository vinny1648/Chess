package client;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import Facades.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void reset() {
        try {
            facade.delete();
        }
        catch (Exception e) {
            System.out.println("reset failure");
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerPositive() {
        try {
            String result = facade.register(new UserData("username", "password", "email"));
            Assertions.assertEquals("username", result);
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void registerNegative() {
        try {

        UserData user = new UserData("username", "password", "email");
        facade.register(user);

        Assertions.assertThrows(ResponseException.class,
                () -> facade.register(user));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void loginPositive() {
        try {
            facade.register(new UserData("username", "password", "email"));
            facade.logout();
            String result = facade.login(new LoginUser("username", "password"));
            Assertions.assertEquals("username", result);
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void loginNegative() {
        try {

            UserData user = new UserData("username", "password", "email");
            facade.register(user);
            facade.logout();

            Assertions.assertThrows(ResponseException.class,
                    () -> facade.login(new LoginUser("username", "notpassword")));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void logoutPositive() {
        try {
            facade.register(new UserData("username", "password", "email"));
            facade.logout();
            Assertions.assertTrue(true);
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void logoutNegative() {
        try {

            UserData user = new UserData("username", "password", "email");
            facade.register(user);
            facade.logout();

            Assertions.assertThrows(ResponseException.class,
                    () -> facade.logout());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void createGamePositive() {
        try {
            facade.register(new UserData("username", "password", "email"));
            GameData result = facade.createGame(new GameData(0, null, null, "game", null));
            Assertions.assertInstanceOf(GameData.class, result);
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void createGameNegative() {
        try {

            facade.register(new UserData("username", "password", "email"));

            Assertions.assertThrows(ResponseException.class,
                    () -> facade.createGame(new GameData(0, null, null, null, null)));
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void listGamesPositive() {
        try {
            facade.register(new UserData("username", "password", "email"));
            facade.createGame(new GameData(0, null, null, "game", null));
            facade.createGame(new GameData(0, null, null, "game", null));
            facade.createGame(new GameData(0, null, null, "india", null));
            facade.createGame(new GameData(0, null, null, "fernando", null));
            GameList result = facade.listGames();
            Assertions.assertInstanceOf(GameList.class, result);
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void listGamesNegative() {
        try {
            Assertions.assertThrows(ResponseException.class,
                    () -> facade.listGames());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void joinGamePositive() {
        try {
            facade.register(new UserData("username", "password", "email"));
            GameData game = facade.createGame(new GameData(0, null, null, "game", null));
            GameData result = facade.joinGame(new JoinRequest("WHITE", game.gameID()));
            Assertions.assertInstanceOf(GameData.class, result);
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void joinGameNegative() {
        try {
            facade.register(new UserData("username", "password", "email"));
            GameData game = facade.createGame(new GameData(0, null, null, "game", null));
            facade.joinGame(new JoinRequest("WHITE", game.gameID()));
            Assertions.assertThrows(ResponseException.class,
                    () -> facade.joinGame(new JoinRequest("WHITE", game.gameID())));
        }
        catch (Exception e) {
            Assertions.fail();
        }
    }

}
