package lk.ijse.dep.service;

public abstract class Player {

    //implemented
    protected Board board;

    public Player(Board board) {
        this.board = board;
    }

    public abstract void movePiece(int col);
}
