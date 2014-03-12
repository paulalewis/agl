package com.castlefrog.agl.domains.hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.castlefrog.agl.AbstractSimulator;
import com.castlefrog.agl.IllegalActionException;
import com.castlefrog.agl.TurnType;

public final class HexSimulator extends AbstractSimulator<HexState, HexAction> {
    private static final int N_AGENTS = 2;

    private static final int[] REWARDS_BLACK_WINS = new int[] { 1, -1 };
    private static final int[] REWARDS_WHITE_WINS = new int[] { -1, 1 };
    private static final int[] REWARDS_NEUTRAL = new int[] { 0, 0 };

    private TurnType turnType_;

    private HexSimulator(HexState state) {
        legalActions_ = new ArrayList<>();
        legalActions_.add(new ArrayList<HexAction>());
        legalActions_.add(new ArrayList<HexAction>());
        setState(state);
    }

    private HexSimulator(HexSimulator simulator) {
        super(simulator);
        turnType_ = simulator.getTurnType();
    }

    public HexSimulator copy() {
        return new HexSimulator(this);
    }

    public static HexSimulator create(int boardSize, TurnType turnType) {
        return new HexSimulator(getInitialState(boardSize));
    }

    public void setState(HexState state) {
        state_ = state;
        computeRewards();
        computeLegalActions(null);
    }

    public void stateTransition(List<HexAction> actions) {
        HexAction action = actions.get(state_.getAgentTurn());
        if (!legalActions_.get(state_.getAgentTurn()).contains(action)) {
            throw new IllegalActionException(action, state_);
        }
        int x = action.getX();
        int y = action.getY();
        switch (state_.getBoardState()) {
            case EMPTY:
                state_.setBoardState(HexState.BoardState.FIRST_MOVE);
                break;
            case FIRST_MOVE:
                state_.setBoardState(HexState.BoardState.SECOND_MOVE);
                break;
            case SECOND_MOVE:
                state_.setBoardState(HexState.BoardState.OTHER);
                break;
        }
        if (state_.isLocationEmpty(x, y)) {
            state_.setLocation(x, y, state_.getAgentTurn() + 1);
            state_.setAgentTurn(getNextAgentTurn());
            computeRewards(action);
            computeLegalActions(action);
        } else {
            state_.setLocation(x, y, 0);
            state_.setLocation(y, x, state_.getAgentTurn() + 1);
            state_.setAgentTurn(getNextAgentTurn());
            computeRewards(action);
            computeLegalActions(null);
        }
    }

    private void computeLegalActions(HexAction prevAction) {
        if (rewards_ == REWARDS_NEUTRAL) {
            int agentTurn = state_.getAgentTurn();
            int otherTurn = (agentTurn + 1) % N_AGENTS;
            legalActions_.set(agentTurn, legalActions_.get(otherTurn));
            legalActions_.set(otherTurn, new ArrayList<HexAction>());
            List<HexAction> legalActions = legalActions_.get(agentTurn);
            if (prevAction != null && state_.getBoardState() == HexState.BoardState.OTHER) {
                legalActions.remove(prevAction);
            } else {
                legalActions.clear();
                for (int i = 0; i < state_.getBoardSize(); i += 1) {
                    for (int j = 0; j < state_.getBoardSize(); j += 1) {
                        if (state_.isLocationEmpty(i, j) ||
                                state_.getBoardState() == HexState.BoardState.FIRST_MOVE) {
                            legalActions.add(HexAction.valueOf(i, j));
                        }
                    }
                }
            }
        } else {
            clearLegalActions();
        }
    }

    private void computeRewards() {
        byte[][] locations = state_.getLocations();
        boolean[][] visited = new boolean[state_.getBoardSize()][state_.getBoardSize()];
        for (int i = 0; i < state_.getBoardSize(); i += 1) {
            if (locations[0][i] == 1 && !visited[0][i]) {
                if ((dfsSides(0, i, locations, visited) & 3) == 3) {
                    rewards_ = REWARDS_BLACK_WINS;
                    return;
                }
            }
            if (locations[i][0] == 2 && !visited[i][0]) {
                if ((dfsSides(i, 0, locations, visited) & 12) == 12) {
                    rewards_ = REWARDS_WHITE_WINS;
                    return;
                }
            }
        }
        rewards_ = REWARDS_NEUTRAL;
    }

    private void computeRewards(HexAction action) {
        byte[][] locations = state_.getLocations();
        boolean[][] visited = new boolean[state_.getBoardSize()][state_.getBoardSize()];
        int x = action.getX();
        int y = action.getY();
        int value = dfsSides(x, y, locations, visited);
        if (locations[x][y] == 1 && (value & 3) == 3) {
            rewards_ = REWARDS_BLACK_WINS;
        } else if (locations[x][y] == 2 && (value & 12) == 12) {
            rewards_ = REWARDS_WHITE_WINS;
        } else {
            rewards_ = REWARDS_NEUTRAL;
        }
    }

    private int dfsSides(int x0,
                         int y0,
                         byte[][] locations,
                         boolean[][] visited) {
        int value = 0;
        Stack<HexAction> stack = new Stack<>();
        stack.push(HexAction.valueOf(x0, y0));
        visited[x0][y0] = true;
        while (!stack.empty()) {
            HexAction v = stack.pop();
            int x = v.getX();
            int y = v.getY();
            value |= getLocationMask(x, y);
            for (int i = -1; i <= 1; i += 1) {
                for (int j = -1; j <= 1; j += 1) {
                    int xi = x + i;
                    int yi = y + j;
                    if (i + j != 0 && xi >= 0 && yi >= 0 &&
                            xi < state_.getBoardSize() && yi < state_.getBoardSize()) {
                        if (!visited[xi][yi] &&
                                locations[xi][yi] == locations[x][y]) {
                            stack.push(HexAction.valueOf(xi, yi));
                            visited[xi][yi] = true;
                        }
                    }
                }
            }
        }
        return value;
    }

    private int getLocationMask(int x, int y) {
        int side = 0;
        if (x == 0) {
            side |= 1;
        } else if (x == state_.getBoardSize() - 1) {
            side |= 2;
        }
        if (y == 0) {
            side |= 4;
        } else if (y == state_.getBoardSize() - 1) {
            side |= 8;
        }
        return side;
    }

    private int getNextAgentTurn() {
        return (state_.getAgentTurn() + 1) % N_AGENTS;
    }

    public static HexState getInitialState(int boardSize) {
        return new HexState(boardSize, new byte[N_AGENTS][(boardSize * boardSize + Byte.SIZE - 1) / Byte.SIZE], 0, HexState.BoardState.EMPTY);
    }

    public int getNAgents() {
        return N_AGENTS;
    }

    public TurnType getTurnType() {
        return turnType_;
    }
}
