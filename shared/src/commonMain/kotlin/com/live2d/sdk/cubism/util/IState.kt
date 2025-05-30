package com.live2d.sdk.cubism.util

// TODO:: change to abs class ?
interface StateContext<C : StateContext<C, S>, S : IState<C, S>> {
    var state: S
        @Deprecated(message = "use Stateful.switchState(nextState)")
        set
}

infix fun <C : StateContext<C, S>, S : IState<C, S>> C.switchStateTo(nextState: S) {
    state.onExit(this, nextState)
    nextState.onEnter(this, state)

    state = nextState
}

interface IState<C : StateContext<C, S>, S : IState<C, S>> {

    val onEnter: (context: C, lastState: S) -> Unit
    val onExit: (context: C, nextState: S) -> Unit
}

