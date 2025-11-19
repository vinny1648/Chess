package ui;

import exception.ResponseException;
import model.*;
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
        String result = "";
        while (!result.equals("quit")) {
            if (playerState == UNLOGGED) {
                System.out.print("\n" + ERASE_SCREEN + ">>> " + SET_TEXT_COLOR_MAGENTA);
            }
            else {
                System.out.print("\n" + ERASE_SCREEN + username + ">>> " + SET_TEXT_COLOR_MAGENTA);
            }
            String line = scanner.nextLine();

            try {
                result = evalInput(line);
                System.out.print(SET_TEXT_COLOR_GREEN + result);
            } catch (Throwable e) {
                String msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
    private String evalInput(String line) throws ResponseException {
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
        } else if (playerState == MENU) {
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
    private String register(String... params) throws ResponseException{
        if (params.length >= 3) {
            String usrnm = params[0];
            String password = params[1];
            String email = params[2];
            username = server.register(new UserData(usrnm, password, email));
            playerState = MENU;
            return "Registration Successful";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <username> <password> <email>");
    }
    private String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            String usrnm = params[0];
            String password = params[1];
            username = server.login(new LoginUser(usrnm, password));
            playerState = MENU;
            return "Log In Successful";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <username> <password>");
    }
    private String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            String gameName = params[0];
            String gameID = server.createGame(new GameData(0, null, null, gameName, null));
            return "Game Created";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <gamename>");
    }
    private String joinGame(String... params) throws ResponseException {
        if (params.length >= 2) {
            try {
                int gameID = Integer.parseInt(params[0]);
                String color = params[1].toUpperCase();
                if (color.equals("WHITE")) {
                    playerState = WHITE;
                } else if (color.equals("BLACK")) {
                    playerState = BLACK;
                }
                else {
                    return color + " is not valid. must be BLACK or WHITE";
                }
                server.joinGame(new JoinRequest(color, gameID));
                return "Game Joined";
            } catch (NumberFormatException e) {
                throw new ResponseException(ResponseException.Code.ClientError, "gameID must be numerical");
            }
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <gameID> [WHITE|BLACK]");
    }
    private String observe(String... params) throws ResponseException {
        if (params.length >= 1) {
            try {
                int gameID = Integer.parseInt(params[0]);
                server.joinGame(new JoinRequest(null, gameID));
                return "Observing Game";
            } catch (NumberFormatException e) {
                throw new ResponseException(ResponseException.Code.ClientError, "gameID must be numerical");
            }

        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <gameID>");
    }
    private String listGames() throws ResponseException {
        GameList games = server.listGames();
        String gamesList = "ID  : Name\n";
        for (int i = 0; i < games.games().size(); i++) {
            GameData game = games.games().get(i);
            gamesList = gamesList + game.gameID() + ": " + game.gameName() + "\n";
        }
        return gamesList;
    }
    private String logout() {

    }
    private String help() {

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
