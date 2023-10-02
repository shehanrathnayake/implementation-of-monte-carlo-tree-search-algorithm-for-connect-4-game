package lk.ijse.dep.service;

import java.util.ArrayList;

public class AiPlayer extends Player{
    private static Board board;
    public AiPlayer(Board board){
        super(board);
        this.board = board;
    }

    @Override
    public void movePiece(int col) {

        /*===================================================================================================================================================*/

        /*              AiPlayer Class
         * Check whether there is only one column available for the next move
         * If so, set it to the next move. Else making an instance of MonteCarloTreeSeach class and get returned the best move
         *
         * */

        col = new MonteCarloTreeSearch().bestMove();

        /* Rest of the AiPlayer class*/

        board.updateMove(col, Piece.GREEN);
        board.getBoardUI().update(col, false);
        Winner winner = board.findWinner();
        if (winner.getWinningPiece() != Piece.EMPTY) {
            board.getBoardUI().notifyWinner(winner);

        } else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }
    }

    /* ====================Class Node representing nodes in the tree============================================== */
    private static class Node {
        private Board board;
        private int col;
        private Node parent = null;
        private int visits = 0;
        private int wins = 0;
        private double ucbValue = 0;
        private ArrayList<Node> childArray = new ArrayList<>();

        private Node(Board board) {
            this.board = board;
        }
        private Node(int col, Node parent) {
            this.col = col;
            this.parent = parent;
        }
    }

    /* ================Class that implemented MonteCarlo Tree Search Algorithm==================================== */
    private static class MonteCarloTreeSearch {
        Node rootNode;
        ArrayList<Node> maxUcbNodes;

        /*              Best move

         * Starting point
         * Check whether avoidDefeatMove() returns column number or -1
         * If it returns column number, returns it. Else continue Monte Carlo Tree search algo and returns the best move column number
         *
         * */

        private int bestMove() {
            int bestMove = avoidDefeatMove();
            if (bestMove == -1) {
                rootNode = new Node(board);
                for (int i = 0; i < 6; i++) {
                    select(rootNode);
                }
                int bestNode;
                do {
                    bestNode = (int) (Math.random() * maxUcbNodes.size());
                }while (bestMove == maxUcbNodes.size());
                bestMove = maxUcbNodes.get(bestNode).col;
            }
            return bestMove;
        }

        /*              Avoid defeat

         * Check whether human player has connected 3 consecutive pieces which he is going to win in the next move of him
         * If so, set the next move to avoid it. Return the required column. Else return -1
         *
         * */

        private int avoidDefeatMove() {
            return -1;
        }


        /*              Selection phase

         * If root comes for the first iteration, he has no child, so send him for expansion process. Here where tree is started to be built.
         * From the second iteration root has children array so checks the children one by one whether they have been visited at least one. if not send the first non visited child for rollout process
         * If all children have been visited, then select the child that have higher UCB value. if he has children, send him for select method as a parent node (Recursion). Else send him for expansion process
         *
         * */
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

        /*              Expansion phase

         * Add 5 child nodes to the child arraylist. (Only if it is a legal move)
         * Then send the first child for rollout process
         *
         * */
        private void expand(Node parentNode) {
            for (int i = 0; i < 6; i++) {
                parentNode.childArray.add(new Node(i, parentNode));
            }
            rollOut(parentNode.childArray.get(0));
        }

        /*              Rollout phase

         * Creating a logical board with the current status and play a simulation with random move until a terminal state coming (win, lose or tie)
         * Calculating the UCT value (win/visits)
         * Send the rollout node and UCT value for back propagation
         *
         * */

        private void rollOut(Node nonVisitedNode) {

            // Simulate a random game starting from the current state
            int wins = 0;
            for (int i = 0; i < 1; i++) {
//                Board logicalBoard = rootNode.board;
                BoardImpl logicalBoard = new BoardImpl(board.getBoardUI());
                Piece currentPlayer = Piece.GREEN;
                Piece winningPiece;

                boolean firstTime = true;
                while ((winningPiece = logicalBoard.findWinner().getWinningPiece()) == Piece.EMPTY  && logicalBoard.existLegalMoves()) {

                    int randomMove;
                    if (firstTime) {
                        randomMove = nonVisitedNode.col;
                        firstTime = false;
                    }
                    else {
                        do{
                            randomMove = (int) (Math.random() * 6);
                        } while(randomMove == 6 || !logicalBoard.isLegalMove(randomMove));
                    }
                    logicalBoard.updateMove(randomMove, currentPlayer);  // Update with the current player's piece
                    currentPlayer = (currentPlayer == Piece.GREEN) ? Piece.BLUE : Piece.GREEN;  // Switch to the other player
                }
                wins += ((winningPiece == Piece.GREEN) ? 1 : 0);
            }
            // Update the node's statistics based on the result of the simulated game
            backPropagate(nonVisitedNode, wins);
        }

        /*              Back propagation phase

         * Traversing to the root updating new UCT value, no of visits and new UCB values
         * Calling to update maxUsbNode which hold the highest UCB value among all the visited nodes in the entire tree.
         *
         * */

        private void backPropagate(Node rolledOutNode, int win) {
            Node traversingNode = rolledOutNode;
            traversingNode.wins += win;
            traversingNode.visits ++;

            while(traversingNode.parent != null) {
                Node parentTraversingNode = traversingNode.parent;
                parentTraversingNode.wins += win;
                parentTraversingNode.visits ++;
                traversingNode = traversingNode.parent;
            }
            maxUcbNodes = null;
            updateMaxUcbNode(rootNode);
        }

        /*              Updating maxUCBNode

         * Find the node which has the highest value of UCB value among nodes that have been visited at least one by traversing the entire tree.
         *
         * */

        private void updateMaxUcbNode(Node node) {
//            System.out.println("===================================");
            if (node == rootNode && node.childArray.size() == 0) return;
            for (Node child : node.childArray) {
                if (child.visits > 0) {
                    child.ucbValue = child.wins/(child.visits * 1.0) + Math.sqrt(2) * Math.sqrt(Math.log(child.parent.visits) /child.visits);

                    if (board.isLegalMove(child.col)) {
                        if (maxUcbNodes == null || child.ucbValue > maxUcbNodes.get(0).ucbValue) {
                            maxUcbNodes = new ArrayList<>();
                            maxUcbNodes.add(child);
                        } else if (child.ucbValue == maxUcbNodes.get(0).ucbValue) {
                            maxUcbNodes.add(child);
                        }
                    }
                    if (child.childArray.size() != 0) {
                        updateMaxUcbNode(child);
                    }
                }
            }
        }
    }
}
