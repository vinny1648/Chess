package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.RegistrationResult;
import datamodel.User;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;


public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));
        DataAccess dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);

        server.delete("db", ctx -> ctx.result("{}"));
        //ctx -> register(ctx) can replace this::register
        server.post("user", this::register);

        // Register your endpoints and exception handlers here.

    }
    //handler
    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), User.class);
        try {
            RegistrationResult result = userService.register(request);
            ctx.status(200);
            var resultSerialized = serializer.toJson(result);
            ctx.result(resultSerialized);
        }
        catch (AlreadyTakenException error) {
            Map<String, String> result = Map.of("message", "Error: " + error.getMessage());
            ctx.status(403);
            var resultSerialized = serializer.toJson(result);
            ctx.result(resultSerialized);
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
