package lk.ijse.dep.service;

public class AiPlayer extends Player{
    // implemented
    public AiPlayer(Board board){
        super(board);
    }

    @Override
    public void movePiece(int col) {
        int number;
        do{
            number = (int) (Math.random() * 10);
        } while(!(number > 0 && number < 6 && board.isLegalMove(number)));

        col = number;
        board.updateMove(col, Piece.GREEN);
        board.getBoardUI().update(col, false);
        Winner winner = board.findWinner();
        if (winner.getWinningPiece() != Piece.EMPTY) {
            board.getBoardUI().notifyWinner(winner);

        } else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }

    }
}
