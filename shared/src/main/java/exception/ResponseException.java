package exception;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseException extends Exception {

    public enum Code {
        ServerError,
        ClientError,
    }

    final private Code code;

    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }

    public static ResponseException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        Object statusObj = map.get("status");
        Code status = Code.ClientError;
        if (statusObj != null) {
            try {
                status = Code.valueOf(statusObj.toString());
            } catch (IllegalArgumentException ignored) {}
        }
        String message = map.get("message").toString();
        return new ResponseException(status, message);
    }


    public Code code() {
        return code;
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        if (httpStatusCode >= 500) {
            return Code.ServerError;
        } else if (httpStatusCode >= 400) {
            return Code.ClientError;
        } else {
            throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        }
    }


    public int toHttpStatusCode() {
        return switch (code) {
            case ServerError -> 500;
            case ClientError -> 400;
        };
    }
}