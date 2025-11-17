package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.*;
import service.*;

import java.util.Collection;
import java.util.Map;


public class Server {

    private final Javalin server;
    private final UserService userService;
    private DataAccess dataAccess;
    private final GameService gameService;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));
        try {
            dataAccess = new MySqlDataAccess();
        } catch (Exception E) {
            dataAccess = new MemoryDataAccess();
        }
        userService = new UserService(this.dataAccess);
        gameService = new GameService(this.dataAccess);


        server.delete("db", this::delete);
        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.post("game", this::createGame);
        server.get("game", this::listGames);
        server.put("game", this::joinGame);
        // Register your endpoints and exception handlers here.

    }
    //handler
    private void errorHandler(Context ctx, String error) {
        Map<String, String> result = Map.of("message", "Error: " + error);
        var serializer = new Gson();
        var resultSerialized = serializer.toJson(result);
        ctx.result(resultSerialized);
    }
    private void delete(Context ctx) throws DataAccessException {
        try {
            dataAccess.clear();
        } catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }
    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), model.UserData.class);
        try {
            AuthData result = userService.register(request);
            ctx.status(200);
            var resultSerialized = serializer.toJson(result);
            ctx.result(resultSerialized);
        }
        catch (AlreadyTakenException error) {
            ctx.status(403);
            errorHandler(ctx, error.getMessage());
        }
        catch (BadRequestException error) {
            ctx.status(400);
            errorHandler(ctx, error.getMessage());
        } catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }
    private void login(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), LoginUser.class);
        try {
            AuthData result = userService.login(request);
            ctx.status(200);
            var resultSerialized = serializer.toJson(result);
            ctx.result(resultSerialized);
        }
        catch (IncorrectPasswordException error) {
            ctx.status(401);
            errorHandler(ctx, error.getMessage());
        }
        catch (BadRequestException error) {
            ctx.status(400);
            errorHandler(ctx, error.getMessage());
        }
        catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }
    private void logout(Context ctx) {
        var authToken = ctx.header("Authorization");
        try {
            userService.logout(authToken);
            ctx.status(200);
            ctx.result("{}");
        }
        catch (UnauthorizedException error) {
            ctx.status(401);
            errorHandler(ctx, error.getMessage());
        }
        catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }
    private void createGame(Context ctx) {
        var authToken = ctx.header("Authorization");
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), model.GameData.class);
        try {
            userService.checkAuth(authToken);
            int gameID = gameService.createGame(request);
            ctx.status(200);
            var resultSerialized = serializer.toJson(Map.of("gameID", gameID));
            ctx.result(resultSerialized);
        }
        catch (UnauthorizedException error) {
            ctx.status(401);
            errorHandler(ctx, error.getMessage());
        }
        catch (BadRequestException error) {
            ctx.status(400);
            errorHandler(ctx, error.getMessage());
        }
        catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }
    private void listGames(Context ctx) {
        var authToken = ctx.header("Authorization");
        var serializer = new Gson();
        try {
            userService.checkAuth(authToken);
            Collection<GameView> gameList = gameService.listGames();
            ctx.status(200);
            var resultSerialized = serializer.toJson(Map.of("games", gameList));
            ctx.result(resultSerialized);
        }
        catch (UnauthorizedException error) {
            ctx.status(401);
            errorHandler(ctx, error.getMessage());
        }
        catch (BadRequestException error) {
            ctx.status(400);
            errorHandler(ctx, error.getMessage());
        }
        catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }
    private void joinGame(Context ctx) {
        var authToken = ctx.header("Authorization");
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), JoinRequest.class);
        try {
            userService.checkAuth(authToken);
            String username = dataAccess.checkAuthToken(authToken);
            gameService.joinGame(request, username);
            ctx.status(200);
            ctx.result("{}");
        }
        catch (BadRequestException error) {
            ctx.status(400);
            errorHandler(ctx, error.getMessage());
        }
        catch (UnauthorizedException error) {
            ctx.status(401);
            errorHandler(ctx, error.getMessage());
        }
        catch (AlreadyTakenException error) {
            ctx.status(403);
            errorHandler(ctx, error.getMessage());
        } catch (DataAccessException error) {
            ctx.status(500);
            errorHandler(ctx, error.getMessage());
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
