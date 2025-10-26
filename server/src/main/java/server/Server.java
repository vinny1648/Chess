package server;

import com.google.gson.Gson;
import dataaccess.*;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.Map;


public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(this.dataAccess);


        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);
        server.post("session", this::login);
        // Register your endpoints and exception handlers here.

    }
    //handler
    private void errorHandler(Context ctx, String error) {
        Map<String, String> result = Map.of("message", "Error: " + error);
        var serializer = new Gson();
        var resultSerialized = serializer.toJson(result);
        ctx.result(resultSerialized);
    }
    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), RegisterUser.class);
        try {
            RequestResult result = userService.register(request);
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
        }
    }
    private void login(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), LoginUser.class);
        try {
            RequestResult result = userService.login(request);
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
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
