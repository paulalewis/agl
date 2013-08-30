package com.castlefrog.agl.agents;

import java.util.List;

import com.castlefrog.agl.Agent;
import com.castlefrog.agl.AgentProvider;
import com.castlefrog.agl.Agents;

public final class ExternalAgentProvider implements AgentProvider {
    private static ExternalAgent instance_ = null;

    static {
        Agents.registerProvider("external", ExternalAgentProvider.getInstance());
    }

    private ExternalAgentProvider() {
    }

    public static ExternalAgentProvider getInstance() {
        return new ExternalAgentProvider();
    }

    public Agent newAgent(List<String> params) {
        if (instance_ == null) {
            instance_ = new ExternalAgent();
        }
        return instance_;
    }
}
