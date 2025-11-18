package ui;

import exception.ResponseException;
import server.ServerFacade;

import static ui.ChessClient.PlayerState.*;
import static ui.EscapeSequences.*;

import java.util.Scanner;

public class ChessClient {

    private final ServerFacade server;
    private PlayerState playerState = UNLOGGED;
    private String username;

    private enum PlayerState {
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
    private void evalInput() {
        if (playerState == UNLOGGED) {
            System.out.print("\n" + ERASE_SCREEN);
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
