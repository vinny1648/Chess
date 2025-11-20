package chess;

import java.util.Collection;
import java.util.Objects;
/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn
                && Objects.equals(board, chessGame.board)
                && Objects.equals(perpitrator, chessGame.perpitrator)
                && Objects.equals(wKing, chessGame.wKing)
                && Objects.equals(bKing, chessGame.bKing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    private TeamColor turn;
    private ChessBoard board;
    private ChessPosition perpitrator;
    private ChessPosition wKing;
    private boolean wKingHasMoved;
    private boolean swRookHasMoved;
    private boolean seRookHasMoved;
    private ChessPosition bKing;
    private boolean bKingHasMoved;
    private boolean nwRookHasMoved;
    private boolean neRookHasMoved;
    private ChessPosition enPassantablePawn;
    private ChessPosition enPassantPosition;


    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        wKing = new ChessPosition(1, 5);
        wKingHasMoved = false;
        bKing = new ChessPosition(8, 5);
        bKingHasMoved = false;
        board.resetBoard();

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }
    public boolean testEP (ChessMove move, ChessPiece piece) {
        boolean success = true;
        ChessPosition enemyLocation;
        if (piece.getTeamColor() == TeamColor.WHITE) {
            enemyLocation = new ChessPosition(move.getEndPosition().getRow() - 1, move.getEndPosition().getColumn());
        } else {
            enemyLocation = new ChessPosition(move.getEndPosition().getRow() + 1, move.getEndPosition().getColumn());
        }
        ChessPiece enemy = board.getPiece(enemyLocation);
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(enemyLocation, null);
        if (isInCheck(piece.getTeamColor())) {
            success = false;
        }
        board.addPiece(move.getStartPosition(), piece);
        board.addPiece(move.getEndPosition(), null);
        board.addPiece(enemyLocation, enemy);
        return success;
    }
    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> newMoves = piece.pieceMoves(board, startPosition);
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && enPassantablePawn != null) {
            if (board.getPiece(startPosition).getTeamColor() == TeamColor.WHITE) {
                if (startPosition.getRow() == enPassantablePawn.getRow()
                        && enPassantablePawn.getColumn() + 1 == startPosition.getColumn()) {
                ChessMove enPMove = new ChessMove(startPosition, new ChessPosition(6, startPosition.getColumn() - 1), null);
                if (testEP(enPMove, piece)) {
                    newMoves.add(enPMove);
                    enPassantPosition = enPMove.getEndPosition();
                }
            } else if (startPosition.getRow() == enPassantablePawn.getRow()
                        && enPassantablePawn.getColumn() - 1 == startPosition.getColumn()) {
                ChessMove enPMove = new ChessMove(startPosition, new ChessPosition(6, startPosition.getColumn() + 1), null);
                if (testEP(enPMove, piece)) {
                    newMoves.add(enPMove);
                    enPassantPosition = enPMove.getEndPosition();
                }
            }
            }
            if (board.getPiece(startPosition).getTeamColor() == TeamColor.BLACK) {
                if (startPosition.getRow() == enPassantablePawn.getRow() && enPassantablePawn.getColumn() - 1 == startPosition.getColumn()) {
                    ChessMove enPMove = new ChessMove(startPosition, new ChessPosition(3, startPosition.getColumn() + 1), null);
                    if (testEP(enPMove, piece)) {
                        newMoves.add(enPMove);
                        enPassantPosition = enPMove.getEndPosition();
                    }
                } else if (startPosition.getRow() == enPassantablePawn.getRow() && enPassantablePawn.getColumn() + 1 == startPosition.getColumn()) {
                    ChessMove enPMove = new ChessMove(startPosition, new ChessPosition(3, startPosition.getColumn() - 1), null);
                    if (testEP(enPMove, piece)) {
                        newMoves.add(enPMove);
                        enPassantPosition = enPMove.getEndPosition();
                    }
                }
            }
        }
        for (ChessMove move: moves) {
            ChessPiece enemyPiece = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            if (isInCheck(piece.getTeamColor())) {
                newMoves.remove(move);
            }
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), enemyPiece);
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING && !isInCheck(piece.getTeamColor())) {
            if (board.getPiece(startPosition).getTeamColor() == TeamColor.WHITE && !wKingHasMoved) {
                if (!swRookHasMoved) {
                    ChessPosition path1 = new ChessPosition(1, 2);
                    ChessPosition path2 = new ChessPosition(1, 3);
                    ChessPosition path3 = new ChessPosition(1, 4);
                    if (board.getPiece(path1) == null && board.getPiece(path2) == null
                            && board.getPiece(path3) == null && !posInCheck(path1, piece.getTeamColor())
                            && !posInCheck(path2, piece.getTeamColor()) && !posInCheck(path3, piece.getTeamColor())) {
                        ChessMove qsCastle = new ChessMove(startPosition, path2, null);
                        newMoves.add(qsCastle);
                    }
                }
                if (!seRookHasMoved) {
                    ChessPosition path4 = new ChessPosition(1, 6);
                    ChessPosition path5 = new ChessPosition(1, 7);
                    if (board.getPiece(path4) == null && board.getPiece(path5) == null
                            && !posInCheck(path4, piece.getTeamColor()) && !posInCheck(path5, piece.getTeamColor())) {
                        ChessMove castle = new ChessMove(startPosition, path5, null);
                        newMoves.add(castle);
                    }
                }
            }
            if (board.getPiece(startPosition).getTeamColor() == TeamColor.BLACK && !bKingHasMoved) {
                if (!nwRookHasMoved) {
                    ChessPosition path1 = new ChessPosition(8, 2);
                    ChessPosition path2 = new ChessPosition(8, 3);
                    ChessPosition path3 = new ChessPosition(8, 4);
                    if (board.getPiece(path1) == null && board.getPiece(path2) == null
                            && board.getPiece(path3) == null && !posInCheck(path1, piece.getTeamColor())
                            && !posInCheck(path2, piece.getTeamColor()) && !posInCheck(path3, piece.getTeamColor())) {
                        ChessMove qsCastle = new ChessMove(startPosition, path2, null);
                        newMoves.add(qsCastle);
                    }
                }
                if (!neRookHasMoved) {
                    ChessPosition path4 = new ChessPosition(8, 6);
                    ChessPosition path5 = new ChessPosition(8, 7);
                    if (board.getPiece(path4) == null && board.getPiece(path5) == null
                            && !posInCheck(path4, piece.getTeamColor()) && !posInCheck(path5, piece.getTeamColor())) {
                        ChessMove castle = new ChessMove(startPosition, path5, null);
                        newMoves.add(castle);
                    }
                }
            }
        }
        return newMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException("No piece at start position");
        }
        if (board.getPiece(move.getStartPosition()).getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Not your turn");
        }
        Collection<ChessMove> valid = validMoves(move.getStartPosition());
        if (!valid.contains(move)) {
            throw new InvalidMoveException("Move not valid");
        }
        //perform move
        ChessPiece piece = board.getPiece(move.getStartPosition());
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        //special move for en passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getEndPosition().equals(enPassantPosition)) {
            board.addPiece(enPassantablePawn, null);
        }
        //special move for castle and access whether a king has moved
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                wKing = move.getEndPosition();
                if (!wKingHasMoved && Objects.equals(move.getEndPosition(), new ChessPosition(1, 3))) {
                    board.addPiece(new ChessPosition(1, 4), board.getPiece(new ChessPosition(1, 1)));
                    board.addPiece(new ChessPosition(1, 1), null);
                } else if (!wKingHasMoved && Objects.equals(move.getEndPosition(), new ChessPosition(1, 7))) {
                    board.addPiece(new ChessPosition(1, 6), board.getPiece(new ChessPosition(1, 8)));
                    board.addPiece(new ChessPosition(1, 8), null);
                }
                wKingHasMoved = true;
            }
            if (piece.getTeamColor() == TeamColor.BLACK) {
                bKing = move.getEndPosition();
                if (!bKingHasMoved && Objects.equals(move.getEndPosition(), new ChessPosition(8, 3))) {
                    board.addPiece(new ChessPosition(8, 4), board.getPiece(new ChessPosition(8, 1)));
                    board.addPiece(new ChessPosition(8, 1), null);
                } else if (!bKingHasMoved && Objects.equals(move.getEndPosition(), new ChessPosition(8, 7))) {
                    board.addPiece(new ChessPosition(8, 6), board.getPiece(new ChessPosition(8, 8)));
                    board.addPiece(new ChessPosition(8, 8), null);
                }
                bKingHasMoved = true;
            }
        }
        //access whether the rooks have moved
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (Objects.equals(move.getStartPosition(), new ChessPosition(1, 1))) {
                swRookHasMoved = true;
            } else if (Objects.equals(move.getStartPosition(), new ChessPosition(1, 8))) {
                seRookHasMoved = true;
            } else if (Objects.equals(move.getStartPosition(), new ChessPosition(8, 1))) {
                nwRookHasMoved = true;
            } else if (Objects.equals(move.getStartPosition(), new ChessPosition(8, 8))) {
                neRookHasMoved = true;
            }
        }
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
        }
        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
        //declare a pawn that can be victim of an en passant
        enPassantablePawn = null;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (piece.getTeamColor() == TeamColor.WHITE
                    && move.getStartPosition().getRow() == 2 && move.getEndPosition().getRow() == 4) {
                enPassantablePawn = move.getEndPosition();
            }
            if (piece.getTeamColor() == TeamColor.BLACK
                    && move.getStartPosition().getRow() == 7 && move.getEndPosition().getRow() == 5) {
                enPassantablePawn = move.getEndPosition();
            }
        }
    }

    public boolean posInCheck(ChessPosition position, TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    if (posDanger(piece, pos, position)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean posDanger(ChessPiece piece, ChessPosition enemyPos, ChessPosition position) {
        for (ChessMove move: piece.pieceMoves(board, enemyPos)) {
            if (move.getEndPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    if (checkDanger(piece, pos, teamColor)) {
                        perpitrator = pos;
                        return true;
                    }
                }
            }
        }
        perpitrator = null;
        return false;
    }
    private boolean checkDanger(ChessPiece piece, ChessPosition pos, TeamColor teamColor) {
        for (ChessMove move : piece.pieceMoves(board, pos)) {
            ChessPiece target = board.getPiece(move.getEndPosition());
            if (target != null &&
                    target.getPieceType() == ChessPiece.PieceType.KING &&
                    target.getTeamColor() == teamColor) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean kingCanMove(TeamColor teamColor) {
        ChessPosition king = switch (teamColor) {
            case TeamColor.WHITE -> wKing;
            case TeamColor.BLACK -> bKing;
        };
        Collection<ChessMove> moves = validMoves(king);
        for (ChessMove move: moves) {
            if (!posInCheck(move.getEndPosition(), teamColor)) {
                return true;
            }
        }
        return false;
    }
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        if (kingCanMove(teamColor)) {
            return false;
        }
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!mateDanger(pos, piece, teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean mateDanger(ChessPosition pos, ChessPiece piece, TeamColor teamColor) {
        for (ChessMove move : validMoves(pos)) {
            ChessPiece captured = board.getPiece(move.getEndPosition());
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), piece);
            boolean stillInCheck = isInCheck(teamColor);
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), captured);
            if (!stillInCheck) {
                return false;
            }
        }
        return true;
    }
    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        } else {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition pos = new ChessPosition(i, j);
                    ChessPiece piece = board.getPiece(pos);
                    if (piece == null || piece.getTeamColor() != teamColor) {
                        continue;
                    }
                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param boardSet the new board to use
     */
    public void setBoard(ChessBoard boardSet) {
        board = boardSet;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING) {
                    continue;
                }
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    if (!pos.equals(new ChessPosition(1, 5))) {
                        wKingHasMoved = true;
                    }
                    wKing = pos;
                } else {
                    if (!pos.equals(new ChessPosition(8, 5))) {
                        bKingHasMoved = true;
                    }
                    bKing = pos;
                }
            }
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
