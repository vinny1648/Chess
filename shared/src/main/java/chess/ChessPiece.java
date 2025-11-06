package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && piece == that.piece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, piece);
    }

    private final ChessGame.TeamColor color;
    private final PieceType piece;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.piece = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return piece;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    //checks if the move is on the board
    private boolean onBoard(int[] move) {
        return move[0] >= 1 && move[0] <= 8 && move[1] >= 1 && move[1] <= 8;
    }
    //checks if the move is on an enemy piece
    private boolean onEnemyPiece(ChessBoard board, int[] move) {
        ChessPosition pos = new ChessPosition(move[0], move[1]);
        if (board.getPiece(pos) == null) {
            return false;
        } else {return board.getPiece(pos).getTeamColor() != getTeamColor();}
    }
    //checks if the move is on a team piece
    private boolean onTeamPiece(ChessBoard board, int[] move) {
        ChessPosition pos = new ChessPosition(move[0], move[1]);
        if (board.getPiece(pos) == null) {
            return false;
        } else {return board.getPiece(pos).getTeamColor() == getTeamColor();}
    }
    //calculates moves for pieces that move continuously
    private Collection<ChessMove> moveMultiplier(ChessBoard board, ChessPosition myPosition, int[][]directions) {
        Collection<ChessMove> moves = new ArrayList<>();
        //for every direction
        for (int[] direction : directions) {
            boolean valid = true;
            int multiplier = 1;
            //extend direction until piece hit or off board
            while (valid) {
                int[] potentialMove = new int[]{(direction[0] * multiplier) + myPosition.getRow(),
                        (direction[1] * multiplier) + myPosition.getColumn()};
                if (onBoard(potentialMove)) {
                    if (onEnemyPiece(board, potentialMove)) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(potentialMove[0], potentialMove[1]), null));
                        valid = false;
                    } else if (onTeamPiece(board, potentialMove)) {
                        valid = false;
                    } else {
                        moves.add(new ChessMove(myPosition, new ChessPosition(potentialMove[0], potentialMove[1]), null));
                        multiplier++;
                    }
                } else {
                    valid = false;
                }
            }
        }
        return moves;
    }
    //calculates moves for pieces that don't move continuously
    private Collection<ChessMove> moveFinder(ChessBoard board, ChessPosition myPosition, int[][]directions) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int[] direction : directions) {
            int[] potentialMove = new int[]{direction[0] + myPosition.getRow(), direction[1] + myPosition.getColumn()};
            if (onBoard(potentialMove) && !onTeamPiece(board, potentialMove)) {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMove[0], potentialMove[1]), null));
            }
        }
        return moves;
    }
    //calculates moves for pawns (needs work)
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int[][] potentialMoves = switch (getTeamColor()) {
            case WHITE -> new int[][]{{1, 1}, {1, -1}, {1, 0}, {2, 0}};
            case BLACK -> new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {-2, 0}};
        };
        for (int i = 0; i < 4; i++) {
            potentialMoves[i] = new int[]{myPosition.getRow() + potentialMoves[i][0],
                    myPosition.getColumn() + potentialMoves[i][1]};
        }
        if (onBoard(potentialMoves[0]) && onEnemyPiece(board, potentialMoves[0])) {
            if (potentialMoves[0][0] == 1 || potentialMoves[0][0] == 8) {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[0][0], potentialMoves[0][1]), PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[0][0], potentialMoves[0][1]), PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[0][0], potentialMoves[0][1]), PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[0][0], potentialMoves[0][1]), PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[0][0], potentialMoves[0][1]), null));
            }
        }
        if (onBoard(potentialMoves[1]) && onEnemyPiece(board, potentialMoves[1])) {
            if (potentialMoves[1][0] == 1 || potentialMoves[1][0] == 8) {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[1][0], potentialMoves[1][1]), PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[1][0], potentialMoves[1][1]), PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[1][0], potentialMoves[1][1]), PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[1][0], potentialMoves[1][1]), PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[1][0], potentialMoves[1][1]), null));
            }
        }
        if (onBoard(potentialMoves[2]) && !onEnemyPiece(board, potentialMoves[2]) && !onTeamPiece(board, potentialMoves[2])) {
            if (potentialMoves[2][0] == 1 || potentialMoves[2][0] == 8) {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[2][0], potentialMoves[2][1]), PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[2][0], potentialMoves[2][1]), PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[2][0], potentialMoves[2][1]), PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[2][0], potentialMoves[2][1]), PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[2][0], potentialMoves[2][1]), null));
            }
        }
        if ((getTeamColor() == ChessGame.TeamColor.WHITE
                && myPosition.getRow() == 2)
                || (getTeamColor() == ChessGame.TeamColor.BLACK
                && myPosition.getRow() == 7)) {
            if (!onEnemyPiece(board, potentialMoves[2])
                    && !onTeamPiece(board, potentialMoves[2])
                    && !onEnemyPiece(board, potentialMoves[3])
                    && !onTeamPiece(board, potentialMoves[3])) {
                moves.add(new ChessMove(myPosition, new ChessPosition(potentialMoves[3][0], potentialMoves[3][1]), null));
            }
        }
        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //sorts piece types based on the directions they can move in
        int[][] directions = switch (getPieceType()) {
            case KING, QUEEN -> new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
            case BISHOP -> new int[][]{{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
            case KNIGHT -> new int[][]{{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
            case ROOK -> new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            case PAWN -> null;
        };
        Collection<ChessMove> moves;
        //finds moves for continuously moving or non-continuously moving pieces
        moves = switch (getPieceType()) {
            case QUEEN, BISHOP, ROOK -> moveMultiplier(board, myPosition, directions);
            case KING, KNIGHT -> moveFinder(board, myPosition, directions);
            case PAWN -> pawnMoves(board, myPosition);
        };
        return moves;
    }
}
