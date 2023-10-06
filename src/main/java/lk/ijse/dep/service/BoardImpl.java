package lk.ijse.dep.service;

public class BoardImpl implements Board {
    private final Piece[][] piece;

    private final BoardUI boardUI;

    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
        this.piece = new Piece[NUM_OF_COLS][NUM_OF_ROWS];

        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j < NUM_OF_ROWS; j++) {
                piece[i][j] = Piece.EMPTY;
            }
        }
    }

    @Override
    public BoardUI getBoardUI() {
        return boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col) {
        for (int i = 0; i < NUM_OF_ROWS; i++) {
            if (piece[col][i] == Piece.EMPTY) return i;
        }
        return -1;
    }

    @Override
    public boolean isLegalMove(int col) {
        if (findNextAvailableSpot(col) != -1) return true;
        else return false;
    }

    @Override
    public boolean existLegalMoves() {
        for (int i = 0; i < NUM_OF_COLS; i++) {
            if (isLegalMove(i)) return true;
        }
        return false;
    }

    @Override
    public void updateMove(int col, Piece move) {
        piece[col][findNextAvailableSpot(col)] = move;
    }

    @Override
    public void updateMove(int col, int row, Piece move) {
        piece[col][row] = move;
    }

    @Override
    public Piece[][] getPiece() {
        return piece;
    }

    @Override
    public Winner findWinner() {
        Winner winner = null;

        for (int i = 0; i < NUM_OF_COLS; i++) {
            for (int j = 0; j+3 < NUM_OF_ROWS; j++) {
                boolean equal = true;
                for (int k = 0; k < 3; k++) {
                    if (piece[i][j+k] == Piece.EMPTY || piece[i][j+k+1] == Piece.EMPTY || piece[i][j+k] != piece[i][j+k+1]) {
                        equal = false;
                        break;
                    }
                }
                if (equal) {
                    winner = new Winner(piece[i][j], i, j, i, j + 3);
                    return winner;
                }
            }
        }

        for (int i = 0; i < NUM_OF_ROWS; i++) {
            for (int j = 0; j+3 < NUM_OF_COLS; j++) {
                boolean equal = true;
                for (int k = 0; k < 3; k++) {
                    if (piece[j+k][i] == Piece.EMPTY || piece[j+k+1][i] == Piece.EMPTY || piece[j+k][i] != piece[j+k+1][i]) {
                        equal = false;
                        break;
                    }
                }
                if (equal) {
                    winner = new Winner(piece[j][i], j, i, j+3, i);
                    return winner;
                }
            }
        }

        if (winner == null) winner = new Winner(Piece.EMPTY);

        return winner;
    }
}
