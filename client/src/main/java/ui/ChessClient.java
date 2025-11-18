package ui;

import exception.ResponseException;
import server.ServerFacade;

import static ui.ChessClient.PlayerState.*;
import static ui.EscapeSequences.*;

import java.util.Arrays;
import java.util.Scanner;

public class ChessClient {

    private final ServerFacade server;
    private PlayerState playerState = UNLOGGED;
    private String username;

    enum PlayerState {
        WHITE,
        BLACK,
        OBSERVER,
        MENU,
        UNLOGGED
    }

    public ChessClient(String serverUrl) throws ResponseException{
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type Help to get started");
        System.out.print(menu());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            if (playerState == UNLOGGED) {
                System.out.print("\n" + ERASE_SCREEN + username ">>> " + SET_TEXT_COLOR_MAGENTA);
            }
            else {
                System.out.print("\n" + ERASE_SCREEN + ">>> " + SET_TEXT_COLOR_MAGENTA);
            }
            String line = scanner.nextLine();

            try {
                result = evalInput(line);
                System.out.print(SET_TEXT_COLOR_GREEN + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
    private void evalInput(String line) {
        String[] tokens = line.toLowerCase().split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (playerState == UNLOGGED) {
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "help" -> help();
                case "quit" -> "quit";
                default -> help();
            };
        } else if (playerState = MENU) {
            return switch (cmd) {
                case "creategame" -> createGame(params);
                case "joingame" -> joinGame(params);
                case "observe" -> observe(params);
                case "listgames" -> listGames();
                case "logout" -> logout();
                case "help" -> help();
                case "quit" -> "quit";
                default -> help();
            };
        }
    }
    private String menu() {
        if (playerState == UNLOGGED) {
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - help
                    - quit
                    """;
        }
        return """
                - creategame <gamename>
                - joingame <gameID> [WHITE|BLACK]
                - observe <gameID>
                - listgames
                - logout <username> <password>
                - help
                - quit
                """;
    }
}
