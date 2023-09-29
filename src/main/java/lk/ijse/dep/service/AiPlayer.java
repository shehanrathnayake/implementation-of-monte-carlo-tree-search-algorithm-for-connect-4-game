package lk.ijse.dep.service;

import java.util.ArrayList;
import java.util.Arrays;

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
            private int visits = 0;
            private double uctValue = 0;
            private double ucbValue = 0;
            private ArrayList<Node> childArray = new ArrayList<>();

            private Node() {
//                this.col = col;
            }
            private Node(int col, Node parent) {
                this.col = col;
                this.parent = parent;
            }
        }

        class MonteCarloTreeSearch {
            Node rootNode;
            Node maxUcbNode = null;
            private void select(Node parentNode) {
                ArrayList<Node> childList = parentNode.childArray;
                if (childList.size() > 0) {
                    for (Node child : childList) {
                        if (child.visits == 0) {
                            rollOut(child);
                            return;
                        }
                    }
                    Node tempMaxUcbNode = childList.get(0);
                    for (Node child : childList) {
                        if (child.ucbValue > tempMaxUcbNode.ucbValue) {
                            tempMaxUcbNode = child;
                        }
                    }
                    if (tempMaxUcbNode.childArray.size() == 0) expand(tempMaxUcbNode);
                    else select(tempMaxUcbNode);

                } else expand(parentNode);
            }

            private void expand(Node parentNode) {
                for (int i = 0; i < 6; i++) {
                    System.out.println(i + "th branch");
                    if (board.isLegalMove(i)){
                        parentNode.childArray.add(new Node(i, parentNode));
                    }
                }
//                System.out.println("Child array size: " + parentNode.childArray.size());
                rollOut(parentNode.childArray.get(0));
            }

            private void rollOut(Node nonVisitedNode) {

                BoardImpl logicalBoard = new BoardImpl(board.getBoardUI());
                Piece currentPlayer = Piece.GREEN;

                // Simulate a random game starting from the current state
                int visits = 0;
                while (logicalBoard.findWinner().getWinningPiece() == Piece.EMPTY  && logicalBoard.existLegalMoves()) {
                    int move = 0;
                    int randomMove;
                    do{
                        randomMove = (int) (Math.random() * 10);
                    } while(!(randomMove >= 0 && randomMove < 6 && logicalBoard.isLegalMove(randomMove)));

                    logicalBoard.updateMove(randomMove, currentPlayer);  // Update with the current player's piece
//                    logicalBoard.getBoardUI().update(randomMove, (currentPlayer == Piece.GREEN) ? false : true);
                    currentPlayer = (currentPlayer == Piece.GREEN) ? Piece.BLUE : Piece.GREEN;  // Switch to the other player
                    visits++;
                }

                // Update the node's statistics based on the result of the simulated game
                double uctValue = ((logicalBoard.findWinner().getWinningPiece() == Piece.GREEN) ? 1 : 0)/(visits * 1.0);
                backPropagate(nonVisitedNode, uctValue);
            }

            private void backPropagate(Node rolledOutNode, double uctValue) {
                Node traversingNode = rolledOutNode;
                traversingNode.uctValue += uctValue;
                traversingNode.visits ++;
                Node parentTraversingNode;
                while(traversingNode.parent != null) {
                    parentTraversingNode = traversingNode.parent;
                    parentTraversingNode.uctValue += uctValue;
                    parentTraversingNode.visits ++;
                    traversingNode.ucbValue = traversingNode.uctValue + 2 * sqrt(Math.log(traversingNode.parent.visits) / traversingNode.visits);

//                    System.out.println("updated uct value: " + traversingNode.uctValue);
//                    System.out.println("updated math value: " + 2 * sqrt(Math.log(traversingNode.parent.visits) / traversingNode.visits));
//                    System.out.println("updated ucb value" + traversingNode.ucbValue);
//                    System.out.println("updated parent visits: " + traversingNode.parent.visits);
//                    System.out.println();

                    traversingNode = traversingNode.parent;
                }
                updateMaxUcbNode(rootNode);
            }

            private void updateMaxUcbNode(Node node) {
                if (node == rootNode && node.childArray.size() == 0) return;
                for (Node child : node.childArray) {
                    if (child.visits > 0) {
                        if (maxUcbNode == null || child.ucbValue > maxUcbNode.ucbValue) {
                            maxUcbNode = child;
                        }
                        if (child.childArray.size() != 0) {
                            updateMaxUcbNode(child);
                        }
                    }
                }
            }

            private int avoidDefeatMove() {
//                System.out.println();
                return -1;
            }

            private int bestMove() {
                int bestMove = avoidDefeatMove();
                if (bestMove == -1) {
                    rootNode = new Node();
                    for (int i = 0; i < 1000; i++) {
                        select(rootNode);
                    }
                    bestMove = maxUcbNode.col;
                }

//                System.out.println("maxucb node: " + maxUcbNode);
//                System.out.println("BestMove ucb value: " + maxUcbNode.ucbValue);
                return bestMove;
            }
        }

        int count = 0;
        int availableCol = 0;
        for (int i = 0; i < 6; i++) {
            if (board.findNextAvailableSpot(i) == -1) {
                count++;
            } else availableCol = i;
        }
        if (count == 5) {
            col = availableCol;
//            System.out.println("count: " + col);
        } else col = new MonteCarloTreeSearch().bestMove();


//        int number;
//        do{
//            number = (int) (Math.random() * 10);
//        } while(!(number >= 0 && number < 6 && board.isLegalMove(number)));
//
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
