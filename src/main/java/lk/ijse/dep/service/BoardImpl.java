package lk.ijse.dep.service;

public abstract class BoardImpl implements Board {
    private Piece piece[][];

    private BoardUI boardUI;

    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
    }
}
