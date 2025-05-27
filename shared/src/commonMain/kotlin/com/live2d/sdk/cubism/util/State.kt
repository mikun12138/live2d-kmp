package com.live2d.sdk.cubism.util

interface Stateful<T : State> {
    var state: T
        @Deprecated(message = "use Stateful.switchState(nextState)")
        set
    var lastState: T
        @Deprecated("damedame")
        set
}

infix fun <T : State> Stateful<T>.switchState(nextState: T) {
    state.onExit(nextState)
    nextState.onEnter(state)

    lastState = state
    state = nextState
}

interface State {
    val onEnter: (State) -> Unit
    val onExit: (State) -> Unit
}

