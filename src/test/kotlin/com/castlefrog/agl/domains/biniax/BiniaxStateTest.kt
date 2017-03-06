package com.castlefrog.agl.domains.biniax

import com.google.common.truth.Truth
import org.junit.Test

class BiniaxStateTest {

    @Test(expected = IllegalArgumentException::class)
    fun testIllegalBiniaxStateSize() {
        BiniaxState(locations = byteArrayOf())
    }

    @Test
    fun testCopy() {
        val state = BiniaxSimulator().initialState
        Truth.assertThat(state).isEqualTo(state.copy())
    }

    @Test
    fun testCopyModification() {
        val state = BiniaxSimulator().initialState
        val copyState = state.copy()
        state.locations[0] = -8
        Truth.assertThat(copyState.locations[0]).isNotEqualTo(-8)
    }

    @Test
    fun testHashCode() {
        Truth.assertThat(BiniaxState().hashCode()).isEqualTo(BiniaxState().hashCode())
    }

    @Test
    fun testToString() {
        val locations = byteArrayOf(
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                28, 28, 28, 0, 28,
                18, 18, 18, 18, 18,
                0, 0, 0, 0, 0,
                0, 0, 4, 0, 0)
        val state = BiniaxState(locations = locations)
        Truth.assertThat(state.toString()).isEqualTo("""
        |Turns: 0
        |Free Moves: 2
        |---------------------
        |:                   :
        |:                   :
        |:                   :
        |:B-H B-H B-H     B-H:
        |:A-H A-H A-H A-H A-H:
        |:                   :
        |:        [D]        :
        |---------------------
        """.trimMargin())
    }

}