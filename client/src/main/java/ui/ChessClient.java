package ui;

import chess.*;
import exception.ResponseException;
import model.*;
import Facades.*;
import websocket.messages.ServerMessage;

import static ui.ChessClient.PlayerState.*;
import static ui.EscapeSequences.*;

import java.util.*;

public class ChessClient implements NotificationHandler {

    private final ServerFacade server;
    private final WebSocketFacade ws;

    private PlayerState playerState;
    private String username;
    private String auth;
    private ChessGame currentGame;
    private Integer currentGameID;
    private HashMap<Integer, Integer> glist;

    @Override
    public void notify(ServerMessage notification) {
        System.out.print("\r" + ERASE_LINE);

        if (notification.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            currentGame = notification.getGame();
            try {
                gameView();
            } catch (ResponseException e) {
                System.out.println("game view error");
            }
        }

        if (notification.getMessage() != null) {
            System.out.println(SET_TEXT_COLOR_WHITE + notification.getMessage());
        }
        if (notification.getErrorMessage() != null) {
            System.out.println(SET_TEXT_COLOR_RED + notification.getErrorMessage());
        }
        if (username != null) {
            System.out.print(SET_TEXT_COLOR_MAGENTA + username + ">>> " + RESET_TEXT_COLOR);
        } else {
            System.out.print(SET_TEXT_COLOR_MAGENTA + ">>> " + RESET_TEXT_COLOR);
        }
    }


    enum PlayerState {
        WHITETEAM,
        BLACKTEAM,
        OBSERVER,
        MENU,
        UNLOGGED
    }

    public ChessClient(String serverUrl) throws ResponseException{
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
        playerState = UNLOGGED;
        glist = new HashMap<>();
    }

    public void run() {
        System.out.println("Welcome to 240 Chess. Type Help to get started");
        try {
            System.out.print(menu());
        } catch (Throwable e) {
            System.out.print("Error: menu error");
        }

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            if (playerState == UNLOGGED) {
                System.out.print("\n" + SET_TEXT_COLOR_MAGENTA + ">>> " + RESET_TEXT_COLOR);
            } else {
                System.out.print("\n" + SET_TEXT_COLOR_MAGENTA + username + ">>> " + RESET_TEXT_COLOR);
            }
            String line = scanner.nextLine();

            try {
                result = evalInput(line);
                if (!Objects.equals(result, "")) {
                    System.out.print(SET_TEXT_COLOR_MAGENTA + result);
                }
            } catch (Throwable e) {
                String msg = e.getMessage();
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
        else {
            gameView();
            return switch (cmd) {
                case "concede" -> concedeGame();
                case "leave" -> leaveGame();
                case "board" -> gameView();
                case "move" -> move(params);
                case "validmoves" -> gameView(params);
                case "help" -> menu();
                default -> menu();
            };
        }
    }
    private String delete() throws ResponseException {
        server.delete();
        return "deleted database";
    }
    private String gameView(String... params) throws ResponseException {
        if (params.length > 1) {
            throw new ResponseException("Expected: <position of piece>");
        }
        System.out.println(ERASE_LINE);
        ArrayList<String> highlightPositions = new ArrayList<>();
        String piecePosition = "";
        if (params.length == 1 && params[0] != null) {
            String position = params[0].toLowerCase();
            Map<Character, Integer> vtranslation = Map.of(
                    'a', 1, 'b', 2, 'c', 3, 'd', 4, 'e', 5, 'f', 6, 'g', 7, 'h', 8
            );
            char colPositionLetter = position.charAt(0);
            int rowPosition = Character.getNumericValue(position.charAt(1));
            if (position.length() != 2 || !vtranslation.containsKey(colPositionLetter) || rowPosition < 1 || rowPosition > 8) {
                throw new ResponseException("Position must be expressed by a letter (a-h) and a number (1-8)");
            }
            int colPosition = vtranslation.get(colPositionLetter);
            piecePosition = "" + rowPosition + colPosition;
            ChessPosition chessPosition = new ChessPosition(rowPosition, colPosition);
            Collection<ChessMove> vMoves = currentGame.validMoves(chessPosition);
            String hPosition;
            for (ChessMove move: vMoves) {
                hPosition = "" + move.getEndPosition().getRow() + move.getEndPosition().getColumn();
                highlightPositions.add(hPosition);
            }
        }
        String boardView = getEdges();
        ChessBoard board = currentGame.getBoard();
        for (int i = 8; i >= 1; i--) {
            for (int j = 0; j <= 9; j++) {
                int row = i;
                int col = j;
                if (playerState == BLACKTEAM) {
                    row = Math.abs(i - 9);
                    col = Math.abs(j - 9);
                }
                if (j == 0 || j == 9) {
                    boardView += SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + " " + row + " ";
                    boardView += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK;
                } else {
                    ChessPosition pos = new ChessPosition(row, col);
                    String piece = buildPiece(board.getPiece(pos));
                    String pot = "" + row + col;
                    if (pot.equals(piecePosition)) {
                        boardView += SET_BG_COLOR_DARK_GREEN;
                    } else if (i % 2 != j % 2 && highlightPositions.contains(pot)) {
                        boardView += SET_BG_COLOR_YELLOW;
                    } else if (i % 2 != j % 2) {
                        boardView += SET_BG_COLOR_WHITE;
                    } else if (highlightPositions.contains(pot)) {
                        boardView += "\u001b[48;5;184m";
                    } else {
                        boardView += SET_BG_COLOR_BLACK;
                    }
                    boardView += piece;
                }
            }
            boardView += "\n";
        }
        boardView += getEdges() + SET_TEXT_COLOR_MAGENTA + SET_BG_COLOR_BLACK;
        System.out.print(boardView);
        return "";
    }
    private String getEdges() {
        String edges;
        if (playerState == BLACKTEAM) {
            edges = "    h   g  f   e   d  c   b  a     ";
        } else {
            edges = "    a   b  c   d   e  f   g  h     ";
        }
        String edge = SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_BOLD +
                edges +
                RESET_TEXT_BOLD_FAINT;
        edge += SET_TEXT_COLOR_WHITE + SET_BG_COLOR_BLACK + "\n";
        return edge;
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
    private String move(String... params) throws ResponseException {
        if (params.length < 2 || params.length > 3) {
            throw new ResponseException(
                    "Expected: <position of piece> <position to move to> (<piece to promote to if applicable>)");
        }

        ChessPosition startPosition;
        ChessPosition endPosition;
        ChessPiece.PieceType promo = null;

        if (params.length == 3 && params[2] != null) {
            String promoStr = params[2].toUpperCase();
            promo = switch (promoStr) {
                case "QUEEN" -> ChessPiece.PieceType.QUEEN;
                case "BISHOP" -> ChessPiece.PieceType.BISHOP;
                case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
                case "ROOK" -> ChessPiece.PieceType.ROOK;
                default -> throw new ResponseException(
                        "Promotion piece must be one of: QUEEN, BISHOP, KNIGHT, ROOK");
            };
        }

        String start = params[0].toLowerCase();
        String end = params[1].toLowerCase();

        Map<Character, Integer> vtranslation = Map.of(
                'a', 1, 'b', 2, 'c', 3, 'd', 4,
                'e', 5, 'f', 6, 'g', 7, 'h', 8
        );

        if (start.length() != 2) {
            throw new ResponseException("Position must be expressed by a letter (a-h) and a number (1-8)");
        }
        char startColPositionLetter = start.charAt(0);
        int startRowPosition = Character.getNumericValue(start.charAt(1));

        if (end.length() != 2) {
            throw new ResponseException("Position must be expressed by a letter (a-h) and a number (1-8)");
        }
        char endColPositionLetter = end.charAt(0);
        int endRowPosition = Character.getNumericValue(end.charAt(1));

        if (!vtranslation.containsKey(startColPositionLetter) || startRowPosition < 1 || startRowPosition > 8 ||
                !vtranslation.containsKey(endColPositionLetter)   || endRowPosition   < 1 || endRowPosition   > 8) {
            throw new ResponseException("Position must be expressed by a letter (a-h) and a number (1-8)");
        }

        int startColPosition = vtranslation.get(startColPositionLetter);
        int endColPosition = vtranslation.get(endColPositionLetter);

        startPosition = new ChessPosition(startRowPosition, startColPosition);
        endPosition   = new ChessPosition(endRowPosition, endColPosition);

        ws.makeMove(auth, currentGameID, new ChessMove(startPosition, endPosition, promo));
        return "";
    }
    private String concedeGame() throws ResponseException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure? <Y/N>");
        String line = scanner.nextLine();
        if (line.toLowerCase().equals("y")) {
            ws.concede(auth, currentGameID);
        }
        return "";

    }
    private String leaveGame() {
        try {
            ws.leaveGame(auth, currentGameID);

            playerState = MENU;
            currentGame = null;
            currentGameID = null;
            return "Game Left";
        } catch (Throwable e) {
            return "(leaveGame) Game Error: " + e.getMessage();
        }
    }
    private String register(String... params) throws ResponseException{
        if (params.length >= 3) {
            username = params[0];
            String password = params[1];
            String email = params[2];
            auth = server.register(new UserData(username, password, email));
            playerState = MENU;
            return "Registration Successful";
        }
        throw new ResponseException("Expected: <username> <password> <email>");
    }
    private String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            username = params[0];
            String password = params[1];
            auth = server.login(new LoginUser(username, password));
            playerState = MENU;
            return "Log In Successful";
        }
        throw new ResponseException("Expected: <username> <password>");
    }
    private String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            String gameName = params[0];
            server.createGame(new GameData(0, null, null, gameName, null));
            listGames();
            return "Game Created";
        }
        throw new ResponseException("Expected: <gamename>");
    }
    private String joinGame(String... params) throws ResponseException {
        if (params.length >= 2) {
            try {
                currentGameID = glist.get(Integer.parseInt(params[0]));
                String color = params[1].toUpperCase();
                GameData gameData = server.joinGame(new JoinRequest(color, currentGameID));
                ws.joinGame(auth, currentGameID);
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
                gameView();
                return "Game Joined";
            } catch (NumberFormatException e) {
                throw new ResponseException("gameID must be numerical");
            } catch (NullPointerException e) {
                throw new ResponseException("gameID is invalid");
            }
        }
        throw new ResponseException("Expected: <gameID> [WHITE|BLACK]");
    }
    private String observe(String... params) throws ResponseException {
        if (params.length >= 1) {
            try {
                listGames();
                currentGameID = glist.get(Integer.parseInt(params[0]));
                currentGame = server.joinGame(new JoinRequest(null, currentGameID)).game();
                ws.joinGame(auth, currentGameID);
                playerState = OBSERVER;
                gameView();
                return "Observing Game";
            } catch (NumberFormatException e) {
                throw new ResponseException("gameID must be numerical");
            }

        }
        throw new ResponseException("Expected: <gameID>");
    }
    private String listGames() throws ResponseException {
        GameList games = server.listGames();
        glist.clear();
        String gamesList = "ID : Name : Players\n";
        for (int i = 0; i < games.games().size(); i++) {
            GameData game = games.games().get(i);
            String whitePlayer = "";
            String blackPlayer = "";
            if (game.whiteUsername() != null) {
                whitePlayer = game.whiteUsername();
            }
            if (game.blackUsername() != null) {
                blackPlayer = game.blackUsername();
            }
            String gameItem = (i + 1) + " : " + game.gameName() + " : w= " + whitePlayer + " b= " + blackPlayer + "\n";
            glist.put(i+1, game.gameID());
            gamesList = gamesList + gameItem;
        }
        return gamesList;
    }
    private String logout() throws ResponseException {
        server.logout();
        playerState = UNLOGGED;
        username = null;
        auth = null;
        return "Log Out Successful";
    }

    private String menu() throws ResponseException {
        if (playerState == UNLOGGED) {
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - help
                    - quit
                    """;
        } else if (playerState == MENU) {
            return """
                - creategame <gamename>
                - joingame <gameID> [WHITE|BLACK]
                - observe <gameID>
                - listgames
                - logout
                - help
                - quit
                """;
        } else {
            gameView();
            return """
                - board
                - move <from> <to>
                - vaildmoves <position of piece>
                - leave
                - concede
                - help
                """;
        }
    }
}
