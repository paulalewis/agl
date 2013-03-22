package com.castlefrog.agl.domains.backgammon;

import com.castlefrog.agl.SimulatorProvider;
import com.castlefrog.agl.Simulator;
import com.castlefrog.agl.Simulators;

public class BackgammonSimulatorProvider implements SimulatorProvider {
    static {
        Simulators.registerProvider("backgammon", BackgammonSimulatorProvider.getInstance());
    }

    private BackgammonSimulatorProvider() {}

    public static BackgammonSimulatorProvider getInstance() {
        return new BackgammonSimulatorProvider();
    }

    public Simulator newSimulator(String[] args) {
        return new BackgammonSimulator();
    }
}