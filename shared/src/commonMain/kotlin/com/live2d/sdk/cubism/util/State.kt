package com.live2d.sdk.cubism.util

interface Stateful<T : State> {
    var state: T
        @Deprecated("不知道有没有用但还是这么写的是屑")
        set
    var lastState: T
        @Deprecated("不知道有没有用但还是这么写的是屑")
        set
}

infix fun <T : State> Stateful<T>.switchState(nextState: T) {
    state.onExit(nextState)
    nextState.onEnter(state)

    lastState = state
    state = nextState
}

abstract class State(
    val onEnter: (State) -> Unit,
    val onExit: (State) -> Unit,
)

