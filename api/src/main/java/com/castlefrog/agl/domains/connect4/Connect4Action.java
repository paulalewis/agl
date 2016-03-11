package com.castlefrog.agl.domains.connect4;

import java.util.ArrayList;
import java.util.List;

import com.castlefrog.agl.Action;

public final class Connect4Action implements Action {
    /** Holds list of all possible actions. */
    private static final List<Connect4Action> actions_ = generateActions();

    /** Slot location to place piece. */
    private final int location_;

    private Connect4Action(int location) {
        location_ = location;
    }

    public Connect4Action copy() {
        return this;
    }

    private static List<Connect4Action> generateActions() {
        List<Connect4Action> actions = new ArrayList<>();
        for (int i = 0; i < Connect4State.WIDTH; i += 1) {
            actions.add(new Connect4Action(i));
        }
        return actions;
    }

    /**
     * Returns the Connect 4 action representation of the slot location.
     * @param location
     *      slot location to place piece.
     *      value range from 0 to Connect4State.WIDTH - 1.
     * @return a Connect 4 action.
     */
    public static Connect4Action valueOf(int location) {
        return actions_.get(location);
    }

    public int getLocation() {
        return location_;
    }

    @Override
    public int hashCode() {
        return location_;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Connect4Action)) {
            return false;
        }
        Connect4Action connect4Action = (Connect4Action) object;
        return location_ == connect4Action.getLocation();
    }

    @Override
    public String toString() {
        return String.valueOf(location_ + 1);
    }
}