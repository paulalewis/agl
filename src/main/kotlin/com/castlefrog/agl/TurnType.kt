package com.castlefrog.agl

/**
 * Indicates how agent turns are decided.
 */
enum class TurnType {
    /** next player to move decided at random  */
    RANDOM,
    /** players move in order  */
    SEQUENTIAL,
    /** player move order randomized  */
    RANDOM_ORDER,
    /** players move at same time  */
    SIMULTANEOUS
}
