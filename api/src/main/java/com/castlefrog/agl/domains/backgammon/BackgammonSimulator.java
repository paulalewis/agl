package com.castlefrog.agl.domains.backgammon;

import com.castlefrog.agl.AdversarialSimulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Backgammon has about 100 actions per game.
 */
public final class BackgammonSimulator extends AdversarialSimulator<BackgammonState, BackgammonAction> {
    private BackgammonSimulator(BackgammonState state) {
        legalActions_ = new ArrayList<>();
        legalActions_.add(new ArrayList<BackgammonAction>());
        legalActions_.add(new ArrayList<BackgammonAction>());
        setState(state);
    }

    private BackgammonSimulator(BackgammonSimulator simulator) {
        super(simulator);
    }

    public BackgammonSimulator copy() {
        return new BackgammonSimulator(this);
    }

    public static BackgammonSimulator create(BackgammonState state) {
        return new BackgammonSimulator(state);
    }

    public void setState(BackgammonState state) {
        state_ = state;
        computeRewards();
        computeLegalActions();
    }

    public void stateTransition(List<BackgammonAction> actions) {
        BackgammonAction action = actions.get(state_.getAgentTurn());
        if (!legalActions_.get(state_.getAgentTurn()).contains(action)) {
            throw new IllegalArgumentException("Illegal action, " + action + ", from state, " + state_);
        }

        byte[] locations = state_.getLocations();

        for (int i = 0; i < action.size(); i++) {
            int from = action.getMove(i).getFrom();
            int distance = action.getMove(i).getDistance();
            byte piece;

            if (locations[from] > 0) {
                piece = 1;
            } else {
                piece = -1;
            }
            int to = from + distance * piece;
            if (to > 0 && to < BackgammonState.N_LOCATIONS - 1) {
                if (locations[to] * piece < 0) {
                    locations[to] = piece;
                    if (piece > 0) {
                        locations[25] -= piece;
                    } else {
                        locations[0] -= piece;
                    }
                } else {
                    locations[to] += piece;
                }
            }
            locations[from] -= piece;
        }
        byte[] dice = new byte[] {
                (byte) (Math.random() * BackgammonState.N_DIE_FACES + 1),
                (byte) (Math.random() * BackgammonState.N_DIE_FACES + 1) };
        state_ = new BackgammonState(locations, dice, (state_.getAgentTurn() + 1) % 2);
        computeRewards();
        computeLegalActions();
    }

    private void computeLegalActions() {
        clearLegalActions();
        byte[] locations = state_.getLocations();
        byte[] dice = state_.getDice();
        int piece;
        byte[] values;
        int depth;

        if (getRewards()[0] == 0) {
            if (state_.getAgentTurn() == 0) {
                piece = 1;
            } else {
                piece = -1;
            }

            if (dice[0] == dice[1]) {
                values = new byte[] {dice[0]};
            } else {
                values = dice;
            }

            if (dice[0] == dice[1]) {
                depth = 4;
            } else {
                depth = 2;
            }

            // Simplify the board
            for (int i = 0; i < BackgammonState.N_LOCATIONS; i++) {
                if (locations[i] * piece == -1) {
                    locations[i] = 0;
                }
            }

            List<BackgammonAction> legalActions = dfs(locations, new LinkedList<BackgammonMove>(), values, piece, depth);

            // Prune moves that are too small
            int max = 0;
            for (BackgammonAction legalAction : legalActions) {
                if (legalAction.size() > max) {
                    max = legalAction.size();
                }
            }
            for (int i = 0; i < legalActions.size(); i++) {
                if (legalActions.get(i).size() != max) {
                    legalActions.remove(i--);
                }
            }
        }
    }

    private List<BackgammonAction> dfs(byte[] locations,
            LinkedList<BackgammonMove> moves, byte[] values, int piece,
            int depth) {
        List<BackgammonAction> legalActions = new ArrayList<>();
        int limit = BackgammonState.N_LOCATIONS;
        int start = 0;

        if (piece > 0 && locations[0] > 0) {
            limit = 1;
        } else if (piece < 0 && locations[25] < 0) {
            start = 25;
        }

        boolean moveOff = canMoveOff(locations, piece);
        for (int i = start; i < limit; i++) {
            if (locations[i] * piece >= 1) {
                for (int j = 0; j < values.length; j++) {
                    if (canMove(i, values[j], moveOff)) {
                        BackgammonMove move = BackgammonMove.valueOf(i,
                                values[j]);
                        if (moves.isEmpty() || move.compareTo(moves.getLast()) * piece >= 0) {
                            moves.addLast(move);
                            if (depth > 1) {
                                locations[i] -= piece;
                                int next = i + values[j] * piece;
                                if (next > 0 && next < BackgammonState.N_LOCATIONS - 1) {
                                    locations[next] += piece;
                                }
                                int k = 0;
                                if (values.length == 2) {
                                    if (j == 0) {
                                        k = 1;
                                    } else {
                                        k = 0;
                                    }
                                }
                                legalActions.addAll(dfs(locations, moves, new byte[] {values[k]}, piece, depth - 1));
                                if (next > 0 && next < BackgammonState.N_LOCATIONS - 1) {
                                    locations[next] -= piece;
                                }
                                locations[i] += piece;
                            } else {
                                legalActions.add(new BackgammonAction(moves));
                            }
                            moves.removeLast();
                        }
                    }
                }
            }
        }
        if (legalActions.size() == 0) {
            legalActions.add(new BackgammonAction(moves));
        }
        return legalActions;
    }

    private boolean canMove(int location, int distance, boolean moveOff) {
        if (state_.getAgentTurn() == 0) {
            int next = location + distance;
            return (next < BackgammonState.N_LOCATIONS - 1 && state_.getLocation(next) >= -1) ||
                   (moveOff && next >= BackgammonState.N_LOCATIONS - 1);
        } else {
            int next = location - distance;
            return (next > 0 && state_.getLocation(next) <= 1) || (moveOff && next <= 0);
        }
    }

    /**
     * Checks if a player can start moving pieces off of the board.
     * @return true if legal to move off board.
     */
    private boolean canMoveOff(byte[] locations, int piece) {
        if (piece > 0) {
            for (int i = 0; i < 19; i += 1) {
                if (locations[i] > 0) {
                    return false;
                }
            }
        } else {
            for (int i = 7; i < BackgammonState.N_LOCATIONS; i += 1) {
                if (locations[i] < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void computeRewards() {
        boolean pos = false, neg = false;
        for (int i = 0; i < BackgammonState.N_LOCATIONS; i += 1) {
            if (state_.getLocation(i) > 0) {
                pos = true;
            } else if (state_.getLocation(i) < 0) {
                neg = true;
            }
        }
        if (!pos) {
            rewards_ = REWARDS_BLACK_WINS;
        } else if (!neg) {
            rewards_ = REWARDS_WHITE_WINS;
        } else {
            rewards_ = REWARDS_NEUTRAL;
        }
    }
}
