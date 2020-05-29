package component

import org.w3c.dom.CanvasRenderingContext2D
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
            player = GameObject(
                    600.0,
                    400.0,
                    100.0,
                    100.0,
                    v3
            )
            keydown = false
        }
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
        if (keydown && state.player.y > 200) {
            setState {
                val y = player.y
                player = player.copy(y = y - 3)
            }
        } else if (!keydown && state.player.y < 400) {
            setState {
                val y = player.y
                player = player.copy(y = y + 3)
            }
        }
    }

    private fun drawGameObject(context: CanvasRenderingContext2D, gameObject: GameObject) {
        gameObject.image.onload = {
            context.drawImage(gameObject.image, gameObject.x, gameObject.y, gameObject.width, gameObject.height)
        }
        context.drawImage(gameObject.image, gameObject.x, gameObject.y, gameObject.width, gameObject.height)
    }

    override fun RBuilder.render() {
        println("render")
        canvasComponent("800", "600") { context ->
            context.fillStyle = "#3C3C3C"
            context.fillRect(0.0, 0.0, 800.0, 600.0)
            drawGameObject(context, state.player)
        }
    }

    interface State : RState {
        var player: GameObject
    }

    data class GameObject(
            val x: Double,
            val y: Double,
            val width: Double,
            val height: Double,
            val image: Image
    )

    companion object {
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
    }
}
