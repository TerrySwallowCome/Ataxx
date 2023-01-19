/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;


import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Tianyu Liu
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    private ArrayList<Move> allmoves(Board board) {
        ArrayList<Move> result = new ArrayList();
        char[] C = {'a', 'b', 'c', 'd', 'e', 'f', 'g'};
        char[] R = {'1', '2', '3', '4', '5', '6', '7'};
        for (int fc0 = 0; fc0 < C.length; fc0++) {
            for (int fr0 = 0; fr0 < R.length; fr0++) {
                if (board.get(C[fc0], R[fr0]) == board.whoseMove()) {
                    int a = fc0 - 2;
                    int b = fc0 + 2;
                    for (int fc1 = a; fc1 <= b && fc1 < 7; fc1++) {
                        int c = fr0 - 2;
                        int d = fr0 + 2;
                        for (int fr1 = c; fr1 <= d && fr1 < 7; fr1++) {
                            if (fc1 >= 0 && fr1 >= 0) {
                                char c0 = C[fc0];
                                char r0 = R[fr0];
                                char c1 = C[fc1];
                                char r1 = R[fr1];
                                Move thisMove;
                                thisMove = Move.move(c0, r0, c1, r1);
                                if (board.legalMove(thisMove)) {
                                    result.add(thisMove);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (result.size() == 0) {
            result.add(Move.PASS);
        }
        return result;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best = null;
        int bestScore;
        if (sense == 1) {
            bestScore = -INFTY;
        } else {
            bestScore = INFTY;
        }
        ArrayList<Move> allm = allmoves(board);
        for (int i = 0; i < allm.size(); i++) {
            Move thisMove = allm.get(i);
            board.makeMove(thisMove);
            int response = minMax(board, depth - 1, false, -sense, alpha, beta);
            board.undo();
            boolean condition;
            if (sense == 1) {
                condition = (response > bestScore);
            } else {
                condition = (response < bestScore);
            }
            if (condition) {
                bestScore = response;
                if (sense == 1) {
                    alpha = max(alpha, bestScore);
                } else {
                    beta = min(beta, bestScore);
                }
                best = thisMove;
                if (alpha >= beta) {
                    if (saveMove) {
                        _lastFoundMove = best;
                    }
                    return bestScore;
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.numPieces(RED) - board.numPieces(BLUE);
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}
