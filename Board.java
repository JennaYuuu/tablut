package tablut;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;

import static tablut.Piece.*;
import static tablut.Square.SQUARE_LIST;
import static tablut.Square.sq;


/**
 * The state of a Tablut Game.
 *
 * @author Jianing Yu
 */
class Board {

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 9;

    /**
     * The throne (or castle) square and its four surrounding squares..
     */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /**
     * Initial positions of attackers.
     */
    static final Square[] INITIAL_ATTACKERS = {
            sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
            sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
            sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
            sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /**
     * Initial positions of defenders of the king.
     */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
            sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        init();
    }

    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }

    /**
     * Copies MODEL into me.
     */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
    }

    /**
     * Clears the board to the initial position.
     */
    void init() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                bPiece[i][j] = Piece.EMPTY;
            }
        }
        bPiece[4][4] = Piece.KING;
        for (Square p : INITIAL_DEFENDERS) {
            bPiece[p.row()][p.col()] = Piece.WHITE;
        }
        for (Square s : INITIAL_ATTACKERS) {
            bPiece[s.row()][s.col()] = Piece.BLACK;
        }
        stack.clear();
        _turn = BLACK;
        _winner = null;
    }

    /**
     * Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     *
     * @param n the limit
     */
    void setMoveLimit(int n) {

        if (2 * n > moveCount()) {
            lim = n;
        }
    }

    /**
     * Return a Piece representing whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the winner in the current position, or null if there is no winner
     * yet.
     */
    Piece winner() {
        return _winner;
    }

    /**
     * Returns true iff this is a win due to a repeated position.
     */
    boolean repeatedPosition() {
        return _repeated;
    }

    /**
     * Record current position and set winner() next mover if the current
     * position is a repeat.
     */
    private void checkRepeated() {
        for (int index = stack.size() - 1; index >= 0; index = index - 1) {
            Piece[][] pieces = stack.get(index);
            if (compare(pieces)) {
                _repeated = true;
                _winner = _turn;
                break;
            }
        }
    }

    /**
     * compare the pieces.
     *
     * @param pieces the pieces
     * @return true or false
     */
    private boolean compare(Piece[][] pieces) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (bPiece[row][col] != pieces[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Return the number of moves since the initial position that have not been
     * undone.
     */
    int moveCount() {
        return _moveCount;
    }

    /**
     * Return location of the king.
     */
    Square kingPosition() {
        return THRONE;
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     */
    final Piece get(int col, int row) {
        return bPiece[row][col];
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     */
    final void put(Piece p, Square s) {
        bPiece[s.row()][s.col()] = p;
    }

    /**
     * Set square S to P and record for undoing.
     */
    final void revPut(Piece p, Square s) {

    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /**
     * Return true iff FROM - TO is an unblocked rook move on the current
     * board.  For this to be true, FROM-TO must be a rook move and the
     * squares along it, other than FROM, must be empty.
     */
    boolean isUnblockedMove(Square from, Square to) {
        if (from.row() != to.row() && from.col() != to.col()) {
            return false;
        }
        if (from.row() == to.row() && from.col() == to.col()) {
            return false;
        }
        if (bPiece[from.row()][from.col()] != KING
                && kingPosition().row() == to.row()
                && kingPosition().col() == to.col()) {
            return false;
        }
        if (from.row() == to.row()) {
            int fromCol = Math.min(from.col(), to.col());
            int toCol = Math.max(from.col(), to.col());

            for (int col = fromCol + 1; col < toCol; col++) {
                Piece piece = bPiece[from.row()][col];
                if (piece != Piece.EMPTY) {
                    return false;
                }
            }
        }
        if (from.col() == to.col()) {
            int fromRow = Math.min(from.row(), to.row());
            int toRow = Math.max(from.row(), to.row());

            for (int row = fromRow + 1; row < toRow; row++) {
                if (bPiece[row][from.col()] != Piece.EMPTY) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /**
     * Return true iff FROM-TO is a valid move.
     */
    boolean isLegal(Square from, Square to) {
        if (!isLegal(from)) {
            return false;
        }

        if (bPiece[to.row()][to.col()] != Piece.EMPTY) {
            return false;
        }

        return isUnblockedMove(from, to);
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        Piece[][] record = new Piece[SIZE][SIZE];
        fillRecord(record);
        put(bPiece[from.row()][from.col()], to);
        put(Piece.EMPTY, from);
        _moveCount++;
        stack.add(record);
        _turn = _turn == WHITE ? BLACK : WHITE;
        setWiner(from, to);

    }

    /**
     * initialize the board.
     *
     * @param record the piece
     */
    private void fillRecord(Piece[][] record) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                record[row][col] = bPiece[row][col];
            }
        }
    }

    /**
     * determine the victory condition.
     *
     * @param from the square from
     * @param to   the square to
     */
    private void setWiner(Square from, Square to) {
        if (bPiece[to.row()][to.col()] == Piece.KING) {
            if ((to.row() == 0 || to.row() == 8)
                    || (to.col() == 0 || to.col() == 8)) {
                _winner = WHITE;
                return;
            }
        }
        Piece toPiece = bPiece[to.row()][to.col()];
        Piece enemyPiece = (toPiece == Piece.WHITE
                || toPiece == Piece.KING) ? Piece.BLACK : Piece.WHITE;
        Piece target = Piece.KING;
        if (toPiece == Piece.WHITE) {
            target = Piece.BLACK;
        }
        if (!hasMove(enemyPiece) && !hasMove(target)) {
            _winner = toPiece;
            return;
        }
        if (toPiece == Piece.BLACK && lim > 0) {
            if ((_moveCount + 1) / 2 > lim) {
                _winner = WHITE;
                return;
            }
        }
        if ((toPiece == Piece.WHITE || toPiece == Piece.KING) && lim > 0) {
            if (_moveCount / 2 + 1 > lim) {
                _winner = BLACK;
                return;
            }
        }
        if (to.col() - 1 >= 0) {
            if (bPiece[to.row()][to.col() - 1] == enemyPiece
                    || bPiece[to.row()][to.col() - 1] == target) {
                checkAndRemovePiece(to.row(), to.col() - 1,
                        toPiece, enemyPiece);
            }
        }
        if (to.col() + 1 <= 8) {
            if (bPiece[to.row()][to.col() + 1] == enemyPiece
                    || bPiece[to.row()][to.col() + 1] == target) {
                checkAndRemovePiece(to.row(), to.col() + 1,
                        toPiece, enemyPiece);
            }
        }
        if (to.row() - 1 >= 0) {
            if (bPiece[to.row() - 1][to.col()] == enemyPiece
                    || bPiece[to.row() - 1][to.col()] == target) {
                checkAndRemovePiece(to.row() - 1,
                        to.col(), toPiece, enemyPiece);
            }
        }
        if (to.row() + 1 <= 8) {
            if (bPiece[to.row() + 1][to.col()] == enemyPiece
                    || bPiece[to.row() + 1][to.col()] == target) {
                checkAndRemovePiece(to.row() + 1,
                        to.col(), toPiece, enemyPiece);
            }
        }
    }

    /**
     * remove the captured piece.
     *
     * @param row        row of the piece
     * @param col        col of the piece
     * @param toPiece    the piece
     * @param enemyPiece the enemy piece
     */
    private void checkAndRemovePiece(int row, int col, Piece toPiece,
                                     Piece enemyPiece) {

        Piece target = buildTarget(col, row, toPiece);

        if (checkSurround(row, col, toPiece)) {
            return;
        }

        if (row == 4 && col == 4 && bPiece[row][col] == Piece.KING) {
            checkKingWin();
        }
        checkCapture(row, col, toPiece, enemyPiece, target);

    }

    /**
     * check captured condition.
     * @param row the row
     * @param col the col
     * @param toPiece the toPiece
     * @param enemyPiece to enemyPiece
     * @param target the target
     */
    private void checkCapture(int row, int col, Piece toPiece,
                              Piece enemyPiece, Piece target) {
        int enemyCount = 0;
        int index = row - 1;
        if (index < 0) {
            enemyCount = 0;
        } else if (bPiece[index][col] == toPiece || bPiece[index][col] == target
                || (index == 4 && col == 4 && bPiece[4][4] == Piece.EMPTY)) {
            enemyCount++;
        }
        index = row + 1;
        if (index > 8) {
            enemyCount = 0;
        } else if (bPiece[index][col] == toPiece || bPiece[index][col] == target
                || (index == 4 && col == 4
                && bPiece[4][4] == Piece.EMPTY)) {
            enemyCount++;
        }
        if (enemyCount == 2) {
            if (bPiece[row][col] == Piece.KING && (row == 4 && col == 4)) {
                return;
            }
            if (bPiece[row][col] == Piece.KING && !(row == 4 && col == 4)) {
                capture(sq(row - 1, col), sq(row + 1, col));
                _winner = BLACK;
            } else {
                capture(sq(row - 1, col), sq(row + 1, col));
            }
            return;
        }
        enemyCount = 0;
        index = col - 1;
        if (index < 0) {
            enemyCount = 0;
        } else if (bPiece[row][index] == toPiece
                || bPiece[row][index] == target || (row == 4 && index == 4
                && bPiece[4][4] == Piece.EMPTY)) {
            enemyCount++;
        }
        index = col + 1;
        if (index > 8) {
            enemyCount = 0;
        } else if (bPiece[row][index] == toPiece || bPiece[row][index] == target
                || (row == 4 && index == 4 && bPiece[4][4] == Piece.EMPTY)) {
            enemyCount++;
        }
        if (enemyCount == 2) {
            if (bPiece[row][col] == Piece.KING && (row == 4 && col == 4)) {
                return;
            }
            if (bPiece[row][col] == Piece.KING && !(row == 4 && col == 4)) {
                capture(sq(row, col - 1), sq(row, col + 1));
                _winner = BLACK;
            } else {
                capture(sq(row, col - 1), sq(row, col + 1));
            }
        }

    }

    /**
     * check surround capture condition.
     * @param row the row
     * @param col the col
     * @param toPiece the toPiece
     * @return true or false
     */
    private boolean checkSurround(int row, int col, Piece toPiece) {
        if (bPiece[row][col] == KING) {
            if (sq(col, row) == NTHRONE) {
                if (bPiece[NTHRONE.row()][NTHRONE.col() - 1] == BLACK
                        && bPiece[NTHRONE.row()][NTHRONE.col() + 1] == BLACK
                        && bPiece[NTHRONE.row() + 1][NTHRONE.col()] == BLACK
                ) {
                    _winner = BLACK;
                    bPiece[row][col] = EMPTY;

                }
                return true;
            }
            if (sq(col, row) == STHRONE) {
                if (bPiece[STHRONE.row()][STHRONE.col() + 1] == BLACK
                        && bPiece[STHRONE.row()][STHRONE.col() - 1] == BLACK
                        && bPiece[STHRONE.row() - 1][STHRONE.col()] == BLACK
                ) {
                    _winner = BLACK;
                    bPiece[row][col] = EMPTY;
                }
                return true;
            }
            if (sq(col, row) == WTHRONE) {
                if (bPiece[WTHRONE.row()][WTHRONE.col() - 1] == BLACK
                        && bPiece[WTHRONE.row() + 1][WTHRONE.col()] == BLACK
                        && bPiece[WTHRONE.row() - 1][WTHRONE.col()] == BLACK
                ) {
                    _winner = BLACK;
                    bPiece[row][col] = EMPTY;
                }
                return true;
            }
            if (sq(col, row) == ETHRONE) {
                if (bPiece[ETHRONE.row()][ETHRONE.col() + 1] == BLACK
                        && bPiece[ETHRONE.row() + 1][ETHRONE.col()] == BLACK
                        && bPiece[ETHRONE.row() - 1][ETHRONE.col()] == BLACK
                ) {
                    _winner = BLACK;
                    bPiece[row][col] = EMPTY;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * build the target.
     * @param col the col
     * @param row the row
     * @param toPiece the toPiece
     * @return the target piece
     */
    private Piece buildTarget(int col, int row, Piece toPiece) {

        Piece target = Piece.KING;

        if (toPiece == Piece.BLACK) {
            target = Piece.BLACK;
        }
        if (toPiece == Piece.KING) {
            return Piece.WHITE;
        }
        if (bPiece[4][4] == Piece.KING && toPiece == Piece.BLACK) {
            int blackCount = 0;
            if (bPiece[3][4] == Piece.BLACK) {
                blackCount++;
            }
            if (bPiece[5][4] == Piece.BLACK) {
                blackCount++;
            }
            if (bPiece[4][3] == Piece.BLACK) {
                blackCount++;
            }
            if (bPiece[4][5] == Piece.BLACK) {
                blackCount++;
            }
            if (blackCount == 3) {
                target = Piece.KING;
            }
        }

        return target;
    }

    /**
     * check if the king wins.
     */
    private void checkKingWin() {
        if (bPiece[3][4] == Piece.BLACK
                && bPiece[5][4] == Piece.BLACK
                && bPiece[4][3] == Piece.BLACK
                && bPiece[4][5] == Piece.BLACK) {
            bPiece[4][4] = Piece.EMPTY;
            _winner = BLACK;
        }
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
        checkRepeated();
    }

    /**
     * Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     * SQ0 and the necessary conditions are satisfied.
     */
    private void capture(Square sq0, Square sq2) {
        bPiece[(sq0.col() + sq2.col()) / 2]
                [(sq0.row() + sq2.row()) / 2] = Piece.EMPTY;
    }

    /**
     * Undo one move.  Has no effect on the initial board.
     */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            revPut(null, null);
            _moveCount--;
        }
    }

    /**
     * Remove record of current position in the set of positions encountered,
     * unless it is a repeated position or we are at the first move.
     */
    private void undoPosition() {
        Piece[][] pre = stack.remove(stack.size() - 1);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                bPiece[row][col] = pre[row][col];
            }
        }
        _repeated = false;
    }

    /**
     * Clear the undo stack and board-position counts. Does not modify the
     * current position or win status.
     */
    void clearUndo() {
        stack.clear();
    }

    /**
     * Return a new mutable list of all legal moves on the current board for
     * SIDE (ignoring whose turn it is at the moment).
     */
    List<Move> legalMoves(Piece side) {
        List<Move> moves = new ArrayList<>();
        List<Square> squares = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (get(col, row) == side) {
                    squares.add(sq(col, row));
                }
            }
        }
        for (Square sq : squares) {
            moves.addAll(legalMoves(sq));
        }
        return moves;
    }

    /**
     * @param sq the square
     * @return the list of the move
     */
    private List<Move> legalMoves(Square sq) {
        List<Move> moves = new ArrayList<>();
        int sqRow = sq.row();
        int sqCol = sq.col();
        for (int row = sqRow + 1; row < SIZE; row++) {
            if (get(sqCol, row) == Piece.EMPTY) {
                if (sq(row, sqCol) == THRONE) {
                    continue;
                }
                Move mv = Move.mv(sq, sq(sqCol, row));
                if (mv != null) {
                    moves.add(mv);
                }
            } else {
                break;
            }
        }
        for (int row = sqRow - 1; row >= 0; row--) {
            if (get(sqCol, row) == Piece.EMPTY) {
                if (sq(row, sqCol) == THRONE) {
                    continue;
                }
                Move mv = Move.mv(sq, sq(sqCol, row));
                if (mv != null) {
                    moves.add(mv);
                }
            } else {
                break;
            }
        }
        for (int col = sqCol - 1; col >= 0; col--) {
            if (get(col, sqRow) == Piece.EMPTY) {
                if (sq(sqRow, col) == THRONE) {
                    continue;
                }
                Move mv = Move.mv(sq, sq(col, sqRow));
                if (mv != null) {
                    moves.add(mv);
                }
            } else {
                break;
            }
        }
        for (int col = sqCol + 1; col < SIZE; col++) {
            if (get(col, sqRow) == Piece.EMPTY) {
                if (sq(sqRow, col) == THRONE) {
                    continue;
                }
                Move mv = Move.mv(sq, sq(col, sqRow));
                if (mv != null) {
                    moves.add(mv);
                }
            } else {
                break;
            }
        }
        return moves;
    }

    /**
     * Return true iff SIDE has a legal move.
     */
    boolean hasMove(Piece side) {
        if (legalMoves(side).isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Return a text representation of this Board.  If COORDINATES, then row
     * and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /**
     * Return the locations of all pieces on SIDE.
     */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> locations = new HashSet<>();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (get(row, col) == side) {
                    locations.add(sq(col, row));
                }
            }
        }

        return locations;
    }

    /**
     * Return the contents of _board in the order of SQUARE_LIST as a sequence
     * of characters: the toString values of the current turn and Pieces.
     */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /**
     * Piece whose turn it is (WHITE or BLACK).
     */
    private Piece _turn;
    /**
     * Cached value of winner on this board, or EMPTY if it has not been
     * computed.
     */
    private Piece _winner;
    /**
     * Number of (still undone) moves since initial position.
     */
    private int _moveCount = 0;
    /**
     * True when current board is a repeated position (ending the game).
     */
    private boolean _repeated;
    /**
     * the board piece.
     */
    private Piece[][] bPiece = new Piece[SIZE][SIZE];
    /**
     * the limit.
     */
    private int lim = 0;
    /**
     * save the piece.
     */
    private List<Piece[][]> stack = new ArrayList<>();

}
