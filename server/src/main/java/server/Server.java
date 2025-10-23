package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
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

        server.delete("db", ctx -> ctx.result("{}"));
        //ctx -> register(ctx) can replace this::register
        server.post("user", this::register);

        // Register your endpoints and exception handlers here.

    }
    //handler
    private void register(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), RegersterRequest.class);
        req.put("authToken", "cow");

        userService.register(req);

        var res = serializer.toJson(req);
        ctx.result(res);
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
