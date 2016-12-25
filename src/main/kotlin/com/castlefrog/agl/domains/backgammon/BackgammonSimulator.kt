package com.castlefrog.agl.domains.backgammon

import com.castlefrog.agl.AdversarialSimulator

import java.util.ArrayList
import java.util.LinkedList

/**
 * Classic game of Backgammon
 */
class BackgammonSimulator(state: BackgammonState,
                          legalActions: List<MutableList<BackgammonAction>>? = null,
                          rewards: IntArray? = null) :
        AdversarialSimulator<BackgammonState, BackgammonAction>() {

    override var state: BackgammonState = state
        set(value) {
            field = value
            _rewards = null
            _legalActions = null
        }

    private var _rewards: IntArray? = null
    override val rewards: IntArray
        get() {
            if (_rewards == null) {
                _rewards = computeRewards(state)
            }
            return _rewards ?: computeRewards(state)
        }

    private var _legalActions: List<MutableList<BackgammonAction>>? = null
    override val legalActions: List<MutableList<BackgammonAction>>
        get() {
            if (_legalActions == null) {
                _legalActions = computeLegalActions(state, rewards)
            }
            return _legalActions ?: computeLegalActions(state, rewards)
        }

    init {
        _rewards = rewards
        _legalActions = legalActions
    }

    override fun copy(): BackgammonSimulator {
        return BackgammonSimulator(state.copy(), _legalActions?.copy(), _rewards?.copyOf())
    }

    override fun stateTransition(actions: Map<Int, BackgammonAction>) {
        val action = actions[state.agentTurn]
        if (action === null || !legalActions[state.agentTurn].contains(action)) {
            throw IllegalArgumentException("Illegal action, $action, from state, $state")
        }

        val locations = state.locations

        for ((from, distance) in action.moves) {
            val piece: Byte = if (locations[from] > 0) 1 else -1
            val to = from + distance * piece
            if (to > 0 && to < BackgammonState.N_LOCATIONS - 1) {
                if (locations[to] * piece < 0) {
                    locations[to] = piece
                    if (piece > 0) {
                        locations[25] = (locations[25] - piece).toByte()
                    } else {
                        locations[0] = (locations[0] - piece).toByte()
                    }
                } else {
                    locations[to] = (locations[to] + piece).toByte()
                }
            }
            locations[from] = (locations[from] - piece).toByte()
        }
        val dice = byteArrayOf((Math.random() * BackgammonState.N_DIE_FACES + 1).toByte(),
                (Math.random() * BackgammonState.N_DIE_FACES + 1).toByte())
        state = BackgammonState(locations, dice, (state.agentTurn + 1) % 2)
    }

    companion object {

        fun create(): BackgammonSimulator {
            return BackgammonSimulator(getInitialState())
        }

        fun getInitialState(): BackgammonState {
            val dice = ByteArray(BackgammonState.N_DICE)
            val agentTurn: Int
            do {
                dice[0] = (Math.random() * BackgammonState.N_DIE_FACES + 1).toByte()
                dice[1] = (Math.random() * BackgammonState.N_DIE_FACES + 1).toByte()
            } while (dice[0] == dice[1])
            if (dice[0] > dice[1]) {
                dice[1] = (Math.random() * BackgammonState.N_DIE_FACES + 1).toByte()
                agentTurn = BackgammonState.TURN_BLACK
            } else {
                dice[0] = (Math.random() * BackgammonState.N_DIE_FACES + 1).toByte()
                agentTurn = BackgammonState.TURN_WHITE
            }
            return BackgammonState(dice = dice, agentTurn = agentTurn)
        }

        private fun computeRewards(state: BackgammonState): IntArray {
            var pos = false
            var neg = false
            for (i in 0..BackgammonState.N_LOCATIONS - 1) {
                if (state.locations[i] > 0) {
                    pos = true
                } else if (state.locations[i] < 0) {
                    neg = true
                }
            }
            if (!pos) {
                return AdversarialSimulator.REWARDS_BLACK_WINS
            } else if (!neg) {
                return AdversarialSimulator.REWARDS_WHITE_WINS
            } else {
                return AdversarialSimulator.REWARDS_NEUTRAL
            }
        }

        private fun computeLegalActions(state: BackgammonState,
                                        rewards: IntArray): List<MutableList<BackgammonAction>> {
            val legalActions = ArrayList<MutableList<BackgammonAction>>()
            legalActions.add(ArrayList<BackgammonAction>())
            legalActions.add(ArrayList<BackgammonAction>())
            val locations = state.locations
            val dice = state.dice
            val piece: Int
            val values: ByteArray
            val depth: Int

            if (rewards[0] == 0) {
                if (state.agentTurn == 0) {
                    piece = 1
                } else {
                    piece = -1
                }

                if (dice[0] == dice[1]) {
                    values = byteArrayOf(dice[0])
                } else {
                    values = dice
                }

                if (dice[0] == dice[1]) {
                    depth = 4
                } else {
                    depth = 2
                }

                // Simplify the board
                for (i in 0..BackgammonState.N_LOCATIONS - 1) {
                    if (locations[i] * piece == -1) {
                        locations[i] = 0
                    }
                }

                legalActions[state.agentTurn].addAll(dfs(locations, LinkedList<BackgammonMove>(),
                        values, piece, depth, state.agentTurn))

                // Prune moves that are too small
                var max = 0
                for (legalAction in legalActions) {
                    if (legalAction[state.agentTurn].moves.size > max) {
                        max = legalAction[state.agentTurn].moves.size
                    }
                }
                var i = 0
                while (i < legalActions.size) {
                    if (legalActions[state.agentTurn][i].moves.size != max) {
                        legalActions.removeAt(i--)
                    }
                    i++
                }
            }
            return legalActions
        }

        private fun dfs(locations: ByteArray,
                        moves: LinkedList<BackgammonMove>,
                        values: ByteArray,
                        piece: Int,
                        depth: Int,
                        agentTurn: Int): MutableList<BackgammonAction> {
            val legalActions = ArrayList<BackgammonAction>()
            var limit = BackgammonState.N_LOCATIONS
            var start = 0

            if (piece > 0 && locations[0] > 0) {
                limit = 1
            } else if (piece < 0 && locations[25] < 0) {
                start = 25
            }

            val moveOff = canMoveOff(locations, piece)
            for (i in start..limit - 1) {
                if (locations[i] * piece >= 1) {
                    for (j in values.indices) {
                        if (canMove(i, values[j].toInt(), moveOff, agentTurn, locations)) {
                            val move = BackgammonMove.valueOf(i,
                                    values[j].toInt())
                            if (moves.isEmpty() || move.compareTo(moves.last) * piece >= 0) {
                                moves.addLast(move)
                                if (depth > 1) {
                                    locations[i] = (locations[i] - piece).toByte()
                                    val next = i + values[j] * piece
                                    if (next > 0 && next < BackgammonState.N_LOCATIONS - 1) {
                                        locations[next] = (locations[next] + piece).toByte()
                                    }
                                    var k = 0
                                    if (values.size == 2) {
                                        if (j == 0) {
                                            k = 1
                                        } else {
                                            k = 0
                                        }
                                    }
                                    legalActions.addAll(dfs(locations, moves, byteArrayOf(values[k]),
                                            piece, depth - 1, agentTurn))
                                    if (next > 0 && next < BackgammonState.N_LOCATIONS - 1) {
                                        locations[next] = (locations[next] - piece).toByte()
                                    }
                                    locations[i] = (locations[i] + piece).toByte()
                                } else {
                                    legalActions.add(BackgammonAction(moves.toHashSet()))
                                }
                                moves.removeLast()
                            }
                        }
                    }
                }
            }
            if (legalActions.size == 0) {
                legalActions.add(BackgammonAction(moves.toHashSet()))
            }
            return legalActions
        }

        private fun canMove(location: Int,
                            distance: Int,
                            moveOff: Boolean,
                            agentTurn: Int,
                            locations: ByteArray): Boolean {
            if (agentTurn == BackgammonState.TURN_BLACK) {
                val next = location + distance
                return next < BackgammonState.N_LOCATIONS - 1 && locations[next] >= -1 ||
                        moveOff && next >= BackgammonState.N_LOCATIONS - 1
            } else {
                val next = location - distance
                return next > 0 && locations[next] <= 1 || moveOff && next <= 0
            }
        }

        /**
         * Checks if a player can start moving pieces off of the board.
         * @return true if legal to move off board.
         */
        private fun canMoveOff(locations: ByteArray, piece: Int): Boolean {
            if (piece > 0) {
                for (i in 0..18) {
                    if (locations[i] > 0) {
                        return false
                    }
                }
            } else {
                for (i in 7..BackgammonState.N_LOCATIONS - 1) {
                    if (locations[i] < 0) {
                        return false
                    }
                }
            }
            return true
        }
    }
}
