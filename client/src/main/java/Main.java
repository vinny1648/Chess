import chess.*;
import exception.ResponseException;
import ui.ChessClient;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        try {
            new ChessClient(serverUrl).run();
        }
        catch (ResponseException error) {
            System.out.printf("Unable to start client: %s%n", error.getMessage());
        }
    }
}