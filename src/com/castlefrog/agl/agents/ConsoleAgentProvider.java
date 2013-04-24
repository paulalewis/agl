package com.castlefrog.agl.agents;

import com.castlefrog.agl.Agent;
import com.castlefrog.agl.AgentProvider;
import com.castlefrog.agl.Agents;

public class ConsoleAgentProvider implements AgentProvider {
    private static ConsoleAgent instance = null;

    static {
        Agents.registerProvider("console", ConsoleAgentProvider.getInstance());
    }

    private ConsoleAgentProvider() {}

    public static ConsoleAgentProvider getInstance() {
        return new ConsoleAgentProvider();
    }

    public Agent newAgent(String[] args) {
        if (instance == null)
            instance = new ConsoleAgent();
        return instance;
    }
}
