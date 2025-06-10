package com.live2d.sdk.cubism.framework.utils

internal interface StateContext<C : StateContext<C, S>, S : IState<C, S>> {
    var state: S
        @Deprecated(message = "use Stateful.switchState(nextState)")
        set
}

internal infix fun <C : StateContext<C, S>, S : IState<C, S>> C.switchStateTo(nextState: S) {
    state.onExit(this, nextState)
    nextState.onEnter(this, state)

    state = nextState
}

internal interface IState<C : StateContext<C, S>, S : IState<C, S>> {

    val onEnter: (context: C, lastState: S) -> Unit
    val onExit: (context: C, nextState: S) -> Unit
}

