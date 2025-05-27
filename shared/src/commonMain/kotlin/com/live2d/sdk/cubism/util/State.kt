package com.live2d.sdk.cubism.util

interface Stateful<T : State> {
    var state: T
        @Deprecated(message = "use Stateful.switchState(nextState)",
            replaceWith = ReplaceWith(
                expression = "this.switchState(newState)",
                imports = ["com.live2d.sdk.cubism.util.Stateful"]
            ),)
        set
    var lastState: T
        @Deprecated("damedame")
        set
}

infix fun <T : State> Stateful<T>.switchStateTo(nextState: T) {
    state.onExit(nextState)
    nextState.onEnter(state)

    lastState = state
    state = nextState
}

interface State {
    val onEnter: (lastState: State) -> Unit
    val onExit: (nextState: State) -> Unit
}

