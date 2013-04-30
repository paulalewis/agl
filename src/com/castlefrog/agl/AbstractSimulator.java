package com.castlefrog.agl;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractSimulator implements common functionality for
 * many simulators.
 */
public abstract class AbstractSimulator<S extends State, A extends Action> implements Simulator<S, A> {
    protected S state_;
    protected List<List<A>> legalActions_;
    protected int[] rewards_;
    
    protected AbstractSimulator() {
    	//state_ = getInitialState();
    }
    
    protected AbstractSimulator(AbstractSimulator<S, A> simulator) {
    	state_ = simulator.getState();
        legalActions_ = simulator.getLegalActions();
        rewards_ = simulator.getRewards();
    }

    /**
     * Rewards for each agent may be indexed by that agent's id.
     * @return array of rewards for each agent.
     */
    public int[] getRewards() {
        int[] rewards = new int[rewards_.length];
        for (int i = 0; i < rewards_.length; i += 1)
            rewards[i] = rewards_[i];
        return rewards;
    }

    /**
     * Get reward for the specified agent.
     * @param agentId the agent to get reward value.
     * @return the reward in current state of single agent.
     */
    public int getReward(int agentId) {
        return rewards_[agentId];
    }

    /**
     * A state is terminal if no agent has any
     * legal actions from the current state.
     * @return true if current state is terminal.
     */
    public boolean isTerminalState() {
        for (int i = 0; i < getNAgents(); i += 1)
            if (hasLegalActions(i))
                return false;
        return true;
    }

    /**
     * Current state of the simulator.
     * @return current state.
     */
    public S getState() {
        return (S) state_.copy();
    }
    
    /**
     * This method returns a list of legal actions
     * for each agent.
     *
     * @return
     *      a list of legal actions for each agent
     *      from the current state.
     */
    public List<List<A>> getLegalActions() {
        List<List<A>> allLegalActions = new ArrayList<List<A>>();
        for (int i = 0; i < getNAgents(); i += 1)
            allLegalActions.add(getLegalActions(i));
        return allLegalActions;
    }

    /**
     * This method returns a list of legal actions
     * for the given agent.
     *
     * @param agentId
     *      agent ID associated with list of legal actions.
     * @return
     *      a list of legal actions from current state.
     */
    public List<A> getLegalActions(int agentId) {
        List<A> legalActions = new ArrayList<A>();
        for (A action: legalActions_.get(agentId))
            legalActions.add(action);
        return legalActions;
    }
    
    /**
     * Returns true if the given agent has 1
     * or more legal actions.
     *
     * @param agentId
     *      agent ID associated with list of legal actions.
     * @return
     *      true if agent has 1 or more legal actions
     *      from current state, otherwise false.
     */
    public boolean hasLegalActions(int agentId) {
        return legalActions_.get(agentId).size() != 0;
    }
}
