package tablut;

import java.util.ArrayList;
import java.util.List;

import static tablut.Square.sq;

/**
 * A Player that automatically generates moves.
 *
 * @author Jianing Yu
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;
    /**
     * the direction.
     */
    private Direct[] directs = {new Direct(1, 0),
        new Direct(-1, 0),
        new Direct(0, 1),
        new Direct(0, -1)};

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {

        Move move = findMove();

        System.out.println("* " + move.toString());
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int score = findMove(board(), maxDepth(b), true, 0, 0, 0);

        if (_lastFoundMove == null) {
            List<Move> mvs = _controller.board().legalMoves(_myPiece);
            if (mvs == null || mvs.isEmpty()) {
                System.out.println("Error no avaliable steps!");
                return null;
            }

            _lastFoundMove = mvs.get(0);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {


        if (_myPiece == Piece.WHITE) {
            return buildWhitePath(board, depth, saveMove, sense, alpha, beta);
        } else {
            return buildBlackPath(board, depth, saveMove, sense, alpha, beta);
        }


    }

    /**
     * build black piece.
     *
     * @param board    the board of the game
     * @param depth    the depth
     * @param saveMove the move
     * @param sense    the sense
     * @param alpha    the alpha
     * @param beta     the beta
     * @return 0
     */
    private int buildBlackPath(Board board, int depth, boolean saveMove,
                               int sense, int alpha, int beta) {
        Move mv = suroundMove(Piece.KING);
        if (mv == null) {
            mv = suroundMove(Piece.WHITE);
        }
        _lastFoundMove = mv;
        return 0;
    }

    /**
     * build white piece's path.
     *
     * @param board    the board
     * @param depth    the depth
     * @param saveMove the saved move
     * @param sense    the sense
     * @param alpha    the alpha
     * @param beta     the beta
     * @return 0
     */
    private int buildWhitePath(Board board, int depth,
                               boolean saveMove, int sense,
                               int alpha, int beta) {
        Square kingSq = findKing(); List<Path> paths = new ArrayList<>();
        Path basPath = new Path();
        basPath.nowSq = kingSq; paths.add(basPath);
        while (true) {
            goPath(paths); boolean flag = true;
            for (Path pa : paths) {
                if (!pa.end) {
                    flag = false;
                }
            }
            if (flag) {
                break;
            }
        }
        if (paths.isEmpty()) {
            _lastFoundMove = suroundMove(Piece.BLACK); return 0;
        }
        List<Path> result = filterMinScorePath(paths);
        int radam = _controller.randInt(result.size());
        Path path = result.get(radam); Move mv = path.path.get(0);
        if (_controller.board().get(mv.to().col(), mv.to().row()) == Piece.EMPTY
                && _controller.board().isUnblockedMove(mv.from(), mv.to())) {
            _lastFoundMove = mv;
            return path.score;
        }
        if (mv.from().row() == mv.to().row()) {
            int fromCol = Math.min(mv.from().col(), mv.to().col());
            int toCol = Math.max(mv.from().col(), mv.to().col());
            int mid = fromCol == mv.from().col() ? 1 : 0;
            for (int col = fromCol + mid; col <= toCol - (1 - mid); col++) {
                Piece piece = board().get(col, mv.from().row());
                if (piece == Piece.WHITE) {
                    Move mv1 = findEscape(col, mv.from().row());
                    if (mv1 == null) {
                        continue;
                    }
                    _lastFoundMove = mv1;
                    return path.score;
                }
            }
        }
        if (mv.from().col() == mv.to().col()) {
            int fromRow = Math.min(mv.from().row(), mv.to().row());
            int toRow = Math.max(mv.from().row(), mv.to().row());
            int mid = fromRow == mv.from().row() ? 1 : 0;
            for (int row = fromRow + mid; row <= toRow - (1 - mid); row++) {
                Piece piece = board().get(mv.from().col(), row);
                if (piece == Piece.WHITE) {
                    Move mv1 = findEscape(mv.from().col(), row);
                    if (mv1 == null) {
                        continue;
                    }
                    _lastFoundMove = mv1;
                    return path.score;
                }
            }
        }
        return 0;
    }

    /** find the minimized score path.
     *
     * @param paths the paths
     * @return the list of paths
     */
    private List<Path> filterMinScorePath(List<Path> paths) {
        List<Path> result = new ArrayList<>();
        int minSco = Integer.MAX_VALUE;
        for (Path pa : paths) {
            if (minSco > pa.score) {
                minSco = pa.score;
                result.clear();
                result.add(pa);
            } else if (minSco == pa.score) {
                result.add(pa);
            }
        }

        return result;

    }

    /**
     * capture the piece.
     *
     * @param piece the piece that we should capture
     * @return the move
     */
    private Move suroundMove(Piece piece) {

        Move move = null;
        if (piece == Piece.KING) {
            move = computeKillKingMove();
            if (move != null) {
                return move;
            }
        }

        List<Square> pieces = listAllPieces(piece);


        while (true) {
            List<Square> subPieces = getMaxScorePieces(pieces, piece);

            move = findAvaliableMove(pieces, piece);
            if (move != null) {
                return move;
            } else {
                pieces.removeAll(subPieces);
                if (pieces.isEmpty()) {
                    break;
                }
            }
        }

        if (piece == Piece.KING) {
            return null;
        }


        Piece enemyPiece = piece == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
        List<Move> mvs = _controller.board().legalMoves(enemyPiece);
        if (mvs == null || mvs.isEmpty()) {
            System.out.println("Error no avaliable steps!");
            return null;
        }

        move = mvs.get(0);

        return move;
    }

    /** compute the move that can capture king.
     *
     * @return the move
     */
    private Move computeKillKingMove() {
        Move mv = null;
        List<Square> pieces = listAllPieces(Piece.KING);

        if (pieces == null || pieces.isEmpty() || pieces.size() > 1) {
            return null;
        }
        Square sq = pieces.get(0);

        if (_controller.board().get(sq.col() - 1, sq.row()) == Piece.BLACK) {

            mv = findBlackMove(sq.col() + 1, sq.row());
            if (mv != null) {
                return mv;
            }

        }
        if (_controller.board().get(sq.col() + 1, sq.row()) == Piece.BLACK) {
            mv = findBlackMove(sq.col() - 1, sq.row());
            if (mv != null) {
                return mv;
            }
        }
        if (_controller.board().get(sq.col(), sq.row() - 1) == Piece.BLACK) {
            mv = findBlackMove(sq.col(), sq.row() + 1);
            if (mv != null) {
                return mv;
            }
        }
        if (_controller.board().get(sq.col(), sq.row() + 1) == Piece.BLACK) {
            mv = findBlackMove(sq.col(), sq.row() - 1);
            if (mv != null) {
                return mv;
            }
        }

        return mv;

    }

    /**
     * find the move for black pieces.
     * @param col the column
     * @param row the row
     * @return
     */
    private Move findBlackMove(int col, int row) {
        if (_controller.board().get(col, row) == Piece.BLACK) {
            return null;
        }

        for (Direct d : directs) {
            int count = 1;

            while (true) {
                int bCol = col + count * d.col;
                int bRow = row + count * d.row;

                if (bCol < 0 || bCol > 8 || bRow < 0 || bCol > 8) {
                    break;
                }
                if (_controller.board().get(bCol, bRow) == Piece.WHITE
                        || _controller.board().get(bCol, bRow) == Piece.KING) {
                    break;
                }
                if (_controller.board().get(bCol, bRow) == Piece.BLACK) {
                    return Move.mv(sq(bCol, bRow), sq(col, row));
                }
                count++;
            }
        }
        return null;

    }

    /**
     * find all the possible moves.
     *
     * @param pieces the pieces
     * @param piece  the piece
     * @return the move
     */
    private Move findAvaliableMove(List<Square> pieces, Piece piece) {

        Piece friendPiece = piece == Piece.WHITE ? Piece.KING : Piece.BLACK;
        Piece enemyPiece = piece == Piece.WHITE ? Piece.BLACK : Piece.WHITE;

        if (piece == Piece.KING) {
            friendPiece = Piece.WHITE;
            enemyPiece = Piece.BLACK;
        }

        int newCol = 0;
        int newRow = 0;

        for (Square s : pieces) {
            for (Direct d : directs) {

                for (int count = 1; true; count++) {
                    newCol = s.col() + d.col * count;
                    newRow = s.row() + d.row * count;
                    if (newCol < 0 || newCol > 8 || newRow < 0 || newRow > 8
                            || _controller.board().get(newCol, newRow) == piece
                            || _controller.board().get(newCol, newRow)
                            == friendPiece) {
                        break;
                    } else if (count == 1
                            && _controller.board().get(newCol, newRow)
                            == enemyPiece) {
                        break;
                    } else if (_controller.board().get(newCol, newRow)
                            == enemyPiece) {
                        if ((s.row() + d.row) == 4
                                && (s.col() + d.col) == 4) {
                            break;
                        }
                        return Move.mv(sq(newCol, newRow),
                                sq(s.col() + d.col,
                                        s.row() + d.row));
                    }
                }
            }
        }

        return null;

    }

    /**
     * find the optimal move.
     *
     * @param pieces the pieces
     * @param piece  the piece
     * @return the result
     */
    private List<Square> getMaxScorePieces(List<Square> pieces, Piece piece) {
        List<Square> result = new ArrayList<>();

        int maxScore = Integer.MIN_VALUE;
        Piece enemyPiece = piece == Piece.WHITE ? Piece.BLACK : Piece.WHITE;
        Piece friendPiece = piece == Piece.WHITE ? Piece.KING : Piece.BLACK;

        for (Square p : pieces) {

            int newCol = 0;
            int newRow = 0;
            int pieceScore = 0;
            for (Direct d : directs) {
                newCol = p.col() + d.row;
                newRow = p.row() + d.row;
                if (newCol == 4 && newCol == 4) {
                    pieceScore = Integer.MIN_VALUE;
                }

                if (newCol < 0 || newCol > 8 || newRow < 0 || newRow > 8
                        || _controller.board().get(newCol, newRow)
                        == enemyPiece) {
                    pieceScore++;
                } else if (_controller.board().get(newCol, newRow) == piece
                        || _controller.board().get(newCol, newRow)
                        == friendPiece) {
                    pieceScore--;
                }
            }
            if (pieceScore > maxScore) {
                result.clear();
                maxScore = pieceScore;
                result.add(p);
            } else if (pieceScore == maxScore) {
                result.add(p);
            }
        }

        return result;
    }

    /**
     * list all the pieces.
     *
     * @param piece the piece
     * @return the list of the pieces
     */
    private List<Square> listAllPieces(Piece piece) {
        List<Square> result = new ArrayList<>();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (_controller.board().get(col, row) == piece) {
                    result.add(sq(col, row));
                }
            }
        }

        return result;

    }

    /**
     * find the optimal escape move.
     *
     * @param col the column
     * @param row the row
     * @return the move
     */
    private Move findEscape(int col, int row) {

        Direct direct = null;
        int step = 0;
        for (Direct d : directs) {
            boolean flag = true;
            for (int count = 1; flag; count++) {
                int c = col + count * d.col;
                int r = row + count * d.row;
                if (c >= 0 && c < 9 && r >= 0 && r < 9
                        && _controller.board().get(c, r) == Piece.EMPTY) {
                    continue;
                } else {
                    if (count - 1 > step) {
                        step = count - 1;
                        direct = d;
                    }
                    flag = false;

                }
            }

        }

        if (step == 0) {
            System.out.println("White on Road can not move!");
            return null;
        }

        int toCol = col + step * direct.col;
        int toRoe = row + step * direct.row;

        return Move.mv(sq(col, row), sq(toCol, toRoe));

    }

    /**
     * find of the possible move to the edge.
     *
     * @param paths the paths
     */
    private void goPath(List<Path> paths) {

        List<Path> result = new ArrayList<>();
        for (Path path : paths) {
            if (path.end) {
                result.add(path);
            }

            Square kingSq = path.nowSq;

            for (Direct sq : directs) {
                int count = 1;
                int baseScore = 0;
                while (true) {
                    int col = kingSq.col() + sq.col * count;
                    int row = kingSq.row() + sq.row * count;
                    if (row < 0 || row > 8 || col < 0 || col > 8
                            || board().get(col, row) == Piece.BLACK) {
                        break;
                    } else if (board().get(col, row) == Piece.WHITE) {
                        baseScore++;
                    }

                    Path basPath = clonePath(path);
                    result.add(basPath);

                    basPath.addMove(Move.mv(kingSq, sq(col, row)));
                    basPath.score = basPath.score + baseScore + 1;

                    basPath.nowSq = sq(col, row);
                    if (basPath.score > 4) {
                        result.remove(basPath);
                    } else {
                        if (col == 0 || col == 8 || row == 0 || row == 8) {
                            basPath.end = true;
                        }
                    }

                    count++;

                }
            }
        }

        paths.clear();
        paths.addAll(result);
    }

    /**
     * copy the path.
     *
     * @param path the path
     * @return the path copied
     */
    private Path clonePath(Path path) {
        Path result = new Path();

        result.nowSq = path.nowSq;
        result.score = path.score;
        result.end = path.end;
        for (Move mv : path.path) {
            result.addMove(mv);
        }

        return result;
    }

    /**
     * find the king.
     *
     * @return the square where the king is
     */
    private Square findKing() {

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board().get(col, row) == Piece.KING) {
                    return sq(col, row);
                }
            }
        }

        System.out.println("Find King error!");
        return null;
    }

    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        return 4;
    }


    /**
     * the path.
     */
    class Path {
        /**
         * the score for the board.
         */
        private int score;
        /**
         * the path.
         */
        private List<Move> path = new ArrayList<>();
        /**
         * the square.
         */
        private Square nowSq;
        /**
         * whether the game ends.
         */
        private boolean end;

        /**
         * add move.
         *
         * @param sq the square
         */
        void addMove(Move sq) {
            path.add(sq);

        }
    }

    /**
     * the direction.
     */
    class Direct {
        /**
         * the col of the direction.
         */
        private int row;
        /**
         * the row of the direction.
         */
        private int col;

        /**
         * the direction.
         *
         * @param col1 the col of the direction
         * @param row1 the row of the direction
         */
        Direct(int col1, int row1) {
            this.row = row1;
            this.col = col1;
        }

    }

}
