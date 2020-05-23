package component

import org.w3c.dom.Image
import org.w3c.dom.events.KeyboardEvent
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import kotlin.browser.window

class Game : RComponent<RProps, Game.State>() {

    init {
        state.apply {
            characterPosition = Position(600.0, 400.0)
            keydown = false
        }
    }

    private val v1 = Image().apply {
        src = "./images/v1.svg"
    }
    private val v2 = Image().apply {
        src = "./images/v2.svg"
    }
    private val v3 = Image().apply {
        src = "./images/v3.svg"
    }
    private val v4 = Image().apply {
        src = "./images/v4.svg"
    }

    private var keydown = false

    override fun componentDidMount() {
        window.addEventListener("keydown", {
            it as KeyboardEvent
            if (it.key == " ") {
                keydown = true
            }
        })
        window.addEventListener("keyup", {
            it as KeyboardEvent
            if (it.key == " ") {
                keydown = false
            }
        })
        updateStatus()
    }

    private fun updateStatus() {
        window.requestAnimationFrame {
            updateStatus()
        }
        if (keydown && state.characterPosition.y > 200) {
            setState {
                val y = state.characterPosition.y
                characterPosition = state.characterPosition.copy(y = y - 1)
            }
        } else if (!keydown && state.characterPosition.y < 400) {
            setState {
                val y = state.characterPosition.y
                characterPosition = state.characterPosition.copy(y = y + 1)
            }
        }
    }

    override fun RBuilder.render() {
        println("render")
        canvasComponent("800", "600") { context ->
            context.fillStyle = "#3C3C3C"
            context.fillRect(0.0, 0.0, 800.0, 600.0)
            v3.onload = {
                context.drawImage(v3, state.characterPosition.x, state.characterPosition.y, 100.0, 100.0)
            }
            context.drawImage(v3, state.characterPosition.x, state.characterPosition.y, 100.0, 100.0)
        }
    }

    interface State : RState {
        var characterPosition: Position
    }

    data class Position(val x: Double, val y: Double)
}
