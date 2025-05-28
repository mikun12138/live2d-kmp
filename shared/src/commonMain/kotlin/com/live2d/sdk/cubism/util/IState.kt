package com.live2d.sdk.cubism.util

interface Stateful<T : IState> {
    var state: T
        @Deprecated(message = "use Stateful.switchState(nextState)")
        set
    var lastState: T
        @Deprecated("damedame")
        set
}

infix fun <T : IState> Stateful<T>.switchStateTo(nextState: T) {
    state.onExit(nextState)
    nextState.onEnter(state)

    lastState = state
    state = nextState
}

interface IState {
    val onEnter: (lastState: IState) -> Unit
    val onExit: (nextState: IState) -> Unit
}

