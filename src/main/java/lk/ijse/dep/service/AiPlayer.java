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
         * Getting the current state of the game.
         * Check whether the AI player is going to win in the next move, so the result of the monte carlo algorithm assign to col
         * Check if the human player is going to win in the next move, so the defending column assign to col
         * If not satisfy above two, assign the result of the monte carlo algorithm assign to col
         *
         * */

        Board currentState = getCurrentState(new BoardImpl(board.getBoardUI()));
        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch();
        int bestMoveToWin = mcts.bestMove();
        int bestMoveToDefend = avoidDefeatMove(currentState);

        if (mcts.finalMove == true) col = bestMoveToWin;
        else {
            if (bestMoveToDefend == -1) col = bestMoveToWin;
            else col = bestMoveToDefend;
        }

        /* Updating the actual board and check if there is a winner */

        board.updateMove(col, Piece.GREEN);
        board.getBoardUI().update(col, false);
        Winner winner = board.findWinner();
        if (winner.getWinningPiece() != Piece.EMPTY) {
            board.getBoardUI().notifyWinner(winner);

        } else if (!board.existLegalMoves()){
            board.getBoardUI().notifyWinner(new Winner(Piece.EMPTY));
        }
    }

    /* Check whether human player has put 3 blue balls together and going to win in the next turn. If, check the position and avoid the defeat returning the column value */
    private int avoidDefeatMove(Board currentState) {
        // Check vertical combinations
        for (int i = 0; i < Board.NUM_OF_COLS; i++) {
            int row = board.findNextAvailableSpot(i);
            if (row < 3 || row == -1) continue;
            int bluePieceCount = 0;
            int j = row-1;
            while(j > row-4 && currentState.getPiece()[i][j] == Piece.BLUE) {
                bluePieceCount++;
                j--;
            }
            if (bluePieceCount == 3) return i;
        }

        // Check horizontal combination
        for (int i = 0; i < Board.NUM_OF_COLS; i++) {
            int row = board.findNextAvailableSpot(i);
            if (row == -1) continue;
            for (int j = i-3; j <= i; j++) {
                if (j < 0) continue;
                int bluePieceCount = 0;
                for (int k = j; k < j+4; k++) {
                    if (k == i || k >= Board.NUM_OF_COLS) continue;
                    if (currentState.getPiece()[k][row] == Piece.BLUE) bluePieceCount++;
                    else break;
                }
                if (bluePieceCount == 3) return i;
            }
        }
        return -1;
    }

    /*          Get Current updated Piece array of the game */
    public static Piece[][] getCurrentStatePiece() {

        Piece[][] currentStatePiece = new Piece[Board.NUM_OF_COLS][Board.NUM_OF_ROWS];
        Piece[][] boardPiece = board.getPiece();

        for (int i = 0; i < Board.NUM_OF_COLS; i++) {
            for (int j = 0; j < Board.NUM_OF_ROWS; j++) {
                currentStatePiece[i][j] = boardPiece[i][j];
            }
        }
        return currentStatePiece;
    }

    /*          Get Current State of the game */
    public static BoardImpl getCurrentState(BoardImpl logicalBoard) {
        Piece[][] currentStatePiece = getCurrentStatePiece();
        for (int i = 0; i < Board.NUM_OF_ROWS; i++) {
            for (int j = 0; j < Board.NUM_OF_COLS; j++) {
                logicalBoard.updateMove(j, i, currentStatePiece[j][i]);
            }
        }
        return logicalBoard;
    }

    /* ====================Class Node representing nodes in the tree============================================== */
    private static class Node {
        private int col;
        private Node parent = null;
        private int visits = 0;
        private int wins = 0;
        private int moves = 0;
        private double ucbValue = 0;
        private ArrayList<Node> childArray = new ArrayList<>();

        private Node() {}
        private Node(int col, Node parent) {
            this.col = col;
            this.parent = parent;
        }
    }

    /* ================Class that implemented MonteCarlo Tree Search Algorithm==================================== */
    private static class MonteCarloTreeSearch {
        Node rootNode;
        ArrayList<Node> maxUcbNodes;
        boolean finalMove = false;

        /*              Best move

         * Starting point
         * Call select method iterating all possible move nodes
         * After all, select the column with highest possible chance of winning and return it
         *
         * */

        MonteCarloTreeSearch() {}

        private int bestMove() {
            rootNode = new Node();
            for (int i = 0; i < 6; i++) {
                select(rootNode);
            }
            if (maxUcbNodes == null || maxUcbNodes.size() == 0) bestMove();
            int bestMove;
            do {
                bestMove = (int) (Math.random() * maxUcbNodes.size());
            }while (bestMove == maxUcbNodes.size());
            return maxUcbNodes.get(bestMove).col;
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

         * Add 5 child nodes to the child arraylist.
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

         * Creating a logical board with the current state and play a simulation with random move until a terminal state coming (win, lose or tie)
         * Send the rollout node wins and moves values for back propagation
         *
         * */

        private void rollOut(Node nonVisitedNode) {

            // Simulate a random game starting from the current state
            Board logicalBoard = AiPlayer.getCurrentState(new BoardImpl(board.getBoardUI()));

            int wins = 0;
            int moves = 0;
            Piece currentPlayer = Piece.BLUE;
            Piece winningPiece;

            boolean firstTime = true;
            while ((winningPiece = logicalBoard.findWinner().getWinningPiece()) == Piece.EMPTY  && logicalBoard.existLegalMoves()) {
                currentPlayer = (currentPlayer == Piece.GREEN) ? Piece.BLUE : Piece.GREEN;  // Switch to the other player
                int randomMove;
                do{
                    if (firstTime) {
                        randomMove = nonVisitedNode.col;
                        firstTime = false;
                    } else {
                        randomMove = (int) (Math.random() * 6);
                    }
                } while(randomMove == 6 || !logicalBoard.isLegalMove(randomMove));

                logicalBoard.updateMove(randomMove, currentPlayer);  // Update with the current player's piece
                if (currentPlayer == Piece.GREEN) moves++;

            }
            if (winningPiece == Piece.GREEN) wins = 2;
            else if (winningPiece == Piece.BLUE) {
                wins = 0;
                moves = 0;
            }
            else wins = 1;

            if (winningPiece == Piece.GREEN && moves == 1) this.finalMove = true;

            // Update the node's statistics based on the result of the simulated game
            backPropagate(nonVisitedNode, wins, moves);
        }

        /*              Back propagation phase

         * Traversing to the root updating new heuristic values
         * Calling to update maxUsbNode which hold the highest UCB value among all the visited nodes in the entire tree.
         *
         * */

        private void backPropagate(Node rolledOutNode, int win, int moves) {
            Node traversingNode = rolledOutNode;
            traversingNode.wins += win;
            traversingNode.moves += moves;
            traversingNode.visits ++;

            while(traversingNode.parent != null) {
                Node parentTraversingNode = traversingNode.parent;
                parentTraversingNode.wins += win;
                parentTraversingNode.moves += moves;
                parentTraversingNode.visits ++;
                traversingNode = traversingNode.parent;
            }
            maxUcbNodes = null;
            updateMaxUcbNode(rootNode);
        }

        /*              Updating maxUCBNode

         * Find the node which has the highest heuristic values among nodes that have been visited at least ones by traversing the entire tree.
         * Checks the no of wins and no of moves during the rollout process
         *
         * */

        private void updateMaxUcbNode(Node node) {
            if (node == rootNode && node.childArray.size() == 0) return;
            for (Node child : node.childArray) {
                if (child.visits > 0) {
                    child.ucbValue = child.wins/(child.visits * 1.0) + Math.sqrt(2) * Math.sqrt(Math.log(child.parent.visits) /child.visits);

                    if (board.isLegalMove(child.col) && child.ucbValue > 0) {
                        if (maxUcbNodes == null || child.ucbValue > maxUcbNodes.get(0).ucbValue || (child.ucbValue == maxUcbNodes.get(0).ucbValue && child.moves < maxUcbNodes.get(0).moves)) {
                            maxUcbNodes = new ArrayList<>();
                            maxUcbNodes.add(child);
                        }
                        else if (child.ucbValue == maxUcbNodes.get(0).ucbValue && child.moves == maxUcbNodes.get(0).moves) {
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
