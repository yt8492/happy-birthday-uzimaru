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

    private var keydown = false

    init {
        state.apply {
            gameState = GameState.Playing.PlayerRunning(
                    GameObject(
                            600.0,
                            400.0,
                            100.0,
                            100.0,
                            v3
                    )
            )
        }
        keydown = false
    }

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
        if (keydown && state.gameState !is GameState.Playing.PlayerJumping) {
            keydown = false
            setState {
                gameState = GameState.Playing.PlayerJumping(
                        gameState.player,
                        0
                )
            }
            return
        }
        when (val currentState = state.gameState) {
            is GameState.Playing -> {
                val nextState = currentState.calculateNextState()
                if (currentState != nextState) {
                    setState {
                        gameState = nextState
                    }
                }
            }
        }
    }

    private fun drawGameObject(context: CanvasRenderingContext2D, gameObject: GameObject) {
        if (gameObject.imageLoaded) {
            context.drawImage(gameObject.image, gameObject.x, gameObject.y, gameObject.width, gameObject.height)
        } else {
            gameObject.image.onload = {
                gameObject.imageLoaded = true
                context.drawImage(gameObject.image, gameObject.x, gameObject.y, gameObject.width, gameObject.height)
            }
        }
    }

    override fun RBuilder.render() {
        println("render")
        canvasComponent(
                state.gameState.canvasWidth.toString(),
                state.gameState.canvasHeight.toString()
        ) { context ->
            context.fillStyle = "#3C3C3C"
            context.fillRect(0.0, 0.0, state.gameState.canvasWidth, state.gameState.canvasHeight)
            drawGameObject(context, state.gameState.player)
        }
    }

    interface State : RState {
        var gameState: GameState
    }

    data class GameObject(
            val x: Double,
            val y: Double,
            val width: Double,
            val height: Double,
            val image: Image,
            var imageLoaded: Boolean = false
    )

    sealed class GameState {
        abstract val player: GameObject
        val canvasWidth = 800.0
        val canvasHeight = 600.0
        val groundY = 400.0

        sealed class Playing : GameState() {
            abstract fun calculateNextState(): GameState

            data class PlayerJumping(
                    override val player: GameObject,
                    private val t: Int
            ) : Playing() {
                override fun calculateNextState(): GameState {
                    val gravity = 0.4
                    val vy = 10
                    val y = 0.5 * gravity * t * t - vy * t + (groundY)
                    println("gravity: $gravity, t: $t, vy: $vy, playerY: ${player.y}")
                    return if (y <= groundY) {
                        PlayerJumping(player.copy(y = y), t + 1)
                    } else {
                        PlayerRunning(player.copy(y = groundY))
                    }
                }
            }

            data class PlayerRunning(
                    override val player: GameObject
            ) : Playing() {
                override fun calculateNextState(): GameState {
                    return this
                }
            }
        }
    }

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
