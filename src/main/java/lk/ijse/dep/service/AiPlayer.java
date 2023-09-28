package lk.ijse.dep.service;

import java.util.ArrayList;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

public class AiPlayer extends Player{
    // implemented
    public AiPlayer(Board board){
        super(board);
    }

    @Override
    public void movePiece(int col) {
        class Node {
            private int col;
            private Node parent = null;
            private int wins = 0;
            private int visits = 0;
            private double uctValue;
            private ArrayList<Node> childArray = new ArrayList<>();

            private Node() {

            }
            private Node(int col, Node parent) {
                this.col = col;
                this.parent = parent;
            }
        }

        class MonteCarloTreeSearch {
            Node maxUctNode = null;
            private void select(Node parentNode) {
                ArrayList<Node> childList = parentNode.childArray;

                for (Node child : childList) {
                    if (child.visits == 0) {
                        rollOut(child);
                        maxUctNode = child;
                        return;
                    }
                    child.uctValue = (child.wins/(child.visits * 1.0)) * 2 * sqrt(log(child.parent.visits) / child.visits);
                    if (maxUctNode == null || child.uctValue > maxUctNode.uctValue) maxUctNode = child;

                }
                if (maxUctNode.childArray.size() != 0) select(maxUctNode);
                else expand(maxUctNode);
            }

            private void expand(Node parentNode) {
                for (int i = 0; i < 5; i++) {
                    if (board.isLegalMove(i)){
                        parentNode.childArray.add(new Node(i, parentNode));
                    }
                }
                rollOut(parentNode.childArray.get(0));
            }

            private void rollOut(Node nonVisitedNode) {

                Node rolledoutNode = null;
                backPropagate(nonVisitedNode, rolledoutNode);
            }

            private void backPropagate(Node nonVisitedNode, Node rolledoutNode) {
                Node traversingNode = nonVisitedNode;
                do {
                    traversingNode.wins += rolledoutNode.wins;
                    traversingNode.visits ++;
                    traversingNode = traversingNode.parent;
                } while (traversingNode.parent != null);
            }

            private int bestMove() {
                Node rootNode = new Node();
                for (int i = 0; i < 5; i++) {
                    select(rootNode);
                }
                return maxUctNode.col;
            }
        }

        col = new MonteCarloTreeSearch().bestMove();

//        int number;
//        do{
//            number = (int) (Math.random() * 10);
//        } while(!(number > 0 && number < 6 && board.isLegalMove(number)));

//        col = number;

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
