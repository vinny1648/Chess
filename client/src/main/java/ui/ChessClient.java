package ui;

import chess.*;
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
    private ChessGame currentGame;

    enum PlayerState {
        WHITETEAM,
        BLACKTEAM,
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
                System.out.print(SET_TEXT_COLOR_MAGENTA + result);
            } catch (Throwable e) {
                String msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
    private String evalInput(String line) throws ResponseException {
        String[] tokens = line.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (playerState == UNLOGGED) {
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "help" -> menu();
                case "delete" -> delete();
                case "quit" -> "quit";
                default -> menu();
            };
        } else if (playerState == MENU) {
            return switch (cmd) {
                case "creategame" -> createGame(params);
                case "joingame" -> joinGame(params);
                case "observe" -> observe(params);
                case "listgames" -> listGames();
                case "logout" -> logout();
                case "help" -> menu();
                case "quit" -> "quit";
                default -> menu();
            };
        }
        if (playerState == BLACKTEAM) {
            gameViewBlack();
        }
        else {
            gameView();
        }
        return switch (cmd) {
            case "concede", "leave" -> concedeGame();
            default -> move(line);
        };
    }
    private String delete() throws ResponseException {
        server.delete();
        return "deleted database";
    }
    private void gameView() {
        String boardView = SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_BOLD +
                "    a   b  c   d   e  f   g  h     " +
                RESET_TEXT_BOLD_FAINT;
        boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "\n";
        ChessBoard board = currentGame.getBoard();
        for (int i = 8; i >= 1; i--) {
            for (int j = 0; j <= 9; j++) {
                if (j == 0 || j == 9) {
                    boardView += SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + " " + i + " ";
                    boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK;
                } else {
                    ChessPosition pos = new ChessPosition(i, j);
                    String piece = buildPiece(board.getPiece(pos));
                    if (i % 2 == j % 2) {
                        boardView += SET_BG_COLOR_WHITE;
                    }
                    else {
                        boardView += SET_BG_COLOR_BLACK;
                    }
                    boardView += piece;
                }
            }
            boardView += "\n";
        }
        boardView += SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_BOLD +
                "    a   b  c   d   e  f   g  h     " +
                RESET_TEXT_BOLD_FAINT;
        boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "\n";
        System.out.print(boardView);
    }
    private void gameViewBlack() {
        String boardView = SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_BOLD +
                "    h   g  f   e   d  c   b  a     " +
                RESET_TEXT_BOLD_FAINT;
        boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "\n";
        ChessBoard board = currentGame.getBoard();
        for (int i = 1; i <= 8; i++) {
            for (int j = 0; j <= 9; j++) {
                if (j == 0 || j == 9) {
                    boardView += SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + " " + i + " ";
                    boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK;
                } else {
                    ChessPosition pos = new ChessPosition(i, j);
                    String piece = buildPiece(board.getPiece(pos));
                    if (i % 2 == j % 2) {
                        boardView += SET_BG_COLOR_WHITE;
                    }
                    else {
                        boardView += SET_BG_COLOR_BLACK;
                    }
                    boardView += piece;
                }
            }
            boardView += "\n";
        }
        boardView += SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_BOLD +
                "    h   g  f   e   d  c   b  a     " +
                RESET_TEXT_BOLD_FAINT;
        boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "\n";
        System.out.print(boardView);
    }
    private String buildPiece(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        String color = switch (piece.getTeamColor()) {
            case WHITE -> SET_TEXT_COLOR_BLUE;
            case BLACK -> SET_TEXT_COLOR_RED;
        };
        String type = switch (piece.getPieceType()) {
            case KING -> WHITE_KING;
            case QUEEN -> WHITE_QUEEN;
            case BISHOP -> WHITE_BISHOP;
            case KNIGHT -> WHITE_KNIGHT;
            case ROOK -> WHITE_ROOK;
            case PAWN -> WHITE_PAWN;
        };
        return color + type;
    }
    private String move(String move) {
        return "moves not implemented";
    }
    private String concedeGame() {
        playerState = MENU;
        return "Game Conceded";
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
        throw new ResponseException("Expected: <username> <password> <email>");
    }
    private String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            String usrnm = params[0];
            String password = params[1];
            username = server.login(new LoginUser(usrnm, password));
            playerState = MENU;
            return "Log In Successful";
        }
        throw new ResponseException("Expected: <username> <password>");
    }
    private String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            String gameName = params[0];
            GameData game = server.createGame(new GameData(0, null, null, gameName, null));
            return "Game Created with ID " + game.gameID();
        }
        throw new ResponseException("Expected: <gamename>");
    }
    private String joinGame(String... params) throws ResponseException {
        if (params.length >= 2) {
            try {
                int gameID = Integer.parseInt(params[0]);
                String color = params[1].toUpperCase();
                GameData gameData = server.joinGame(new JoinRequest(color, gameID));
                if (gameData == null) {
                    throw new ResponseException("unable to join game");
                }
                if (color.equals("WHITE")) {
                    playerState = WHITETEAM;
                } else if (color.equals("BLACK")) {
                    playerState = BLACKTEAM;
                }
                else {
                    return color + " is not valid. must be BLACK or WHITE";
                }
                currentGame = gameData.game();
                if (playerState == BLACKTEAM) {
                    gameViewBlack();
                }
                else {
                    gameView();
                }
                return "Game Joined";
            } catch (NumberFormatException e) {
                throw new ResponseException("gameID must be numerical");
            }
        }
        throw new ResponseException("Expected: <gameID> [WHITE|BLACK]");
    }
    private String observe(String... params) throws ResponseException {
        if (params.length >= 1) {
            try {
                int gameID = Integer.parseInt(params[0]);
                server.joinGame(new JoinRequest(null, gameID));
                playerState = OBSERVER;
                return "Observing Game";
            } catch (NumberFormatException e) {
                throw new ResponseException("gameID must be numerical");
            }

        }
        throw new ResponseException("Expected: <gameID>");
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
    private String logout() throws ResponseException {
        server.logout();
        playerState = UNLOGGED;
        username = null;
        return "Log Out Successful";
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
                - logout
                - help
                - quit
                """;
    }
}
