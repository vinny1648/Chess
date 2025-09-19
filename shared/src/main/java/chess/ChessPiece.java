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
    private Collection<ChessMove> moveMultiplier(ChessBoard board, ChessPosition myPosition, int[][]directions) {
        throw new RuntimeException("Not implemented");
    }
    private Collection<ChessMove> moveFinder(ChessBoard board, ChessPosition myPosition, int[][]directions) {
        throw new RuntimeException("Not implemented");
    }
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not implemented");
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
        Collection<ChessMove> moves = new ArrayList<>();
        //finds moves for continuously moving or non-continuously moving pieces
        moves = switch (getPieceType()) {
            case QUEEN, BISHOP, ROOK -> moveMultiplier(board, myPosition, directions);
            case KING, KNIGHT -> moveFinder(board, myPosition, directions);
            case PAWN -> pawnMoves(board, myPosition);
        };
        return moves;
    }
}
