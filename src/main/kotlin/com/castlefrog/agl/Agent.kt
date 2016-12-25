package com.castlefrog.agl

import java.util.Optional

/**
 * An agent interacts in a domain by selecting
 * an action from a list of legal actions from
 * the current state.
 */
interface Agent {

    /**
     * An agent selects an action given a state and simulator.
     * @param playerId player ID that this agent is selecting the action for
     * @param state current domain state.
     * @param simulator simulator that determines action outcomes in domain.
     * @return selected action from the current state or no action
     *         if the agent had no legal actions to choose
     */
    fun <S : State<S>, A : Action<A>> selectAction(playerId: Int, state: S, simulator: Simulator<S, A>): Optional<A>
}
