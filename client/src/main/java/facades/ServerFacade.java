package facades;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String auth;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public String register(UserData user) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/user", user);
        HttpResponse<String> response = sendRequest(request);
        AuthData data = handleResponse(response, AuthData.class);
        auth = data.authToken();
        return auth;
    }

    public void delete() throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/db", null);
        HttpResponse<String> response = sendRequest(request);
        handleResponse(response, null);
        auth = null;
    }

    public String login(LoginUser user) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/session", user);
        HttpResponse<String> response = sendRequest(request);
        AuthData data = handleResponse(response, AuthData.class);
        auth = data.authToken();
        return auth;
    }

    public void logout() throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/session", null);
        HttpResponse<String> response = sendRequest(request);
        handleResponse(response, null);
        auth = null;
    }

    public GameData createGame(GameData newGame) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/game", newGame);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, GameData.class);
    }

    public GameList listGames() throws ResponseException{
        HttpRequest request = buildRequest("GET", "/game", null);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, GameList.class);
    }

    public GameData joinGame(JoinRequest joinRequest) throws ResponseException{
        HttpRequest request = buildRequest("PUT", "/game", joinRequest);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, GameData.class);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (auth != null) {
            request.setHeader("Authorization", auth);
        }
        else if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        if (!isSuccessful(status)) {
            String body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException("other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}