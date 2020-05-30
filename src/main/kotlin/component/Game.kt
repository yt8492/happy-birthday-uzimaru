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
import kotlin.math.abs
import kotlin.random.Random

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
                    ),
                    emptyList(),
                    0
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
        val currentState = state.gameState
        if (keydown && currentState is GameState.Playing.PlayerRunning) {
            keydown = false
            setState {
                gameState = GameState.Playing.PlayerJumping(
                        currentState.player,
                        currentState.enemyList,
                        0,
                        currentState.frame
                )
            }
            return
        }
        when (currentState) {
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
        canvasComponent(
                state.gameState.canvasWidth.toString(),
                state.gameState.canvasHeight.toString()
        ) { context ->
            context.fillStyle = "#3C3C3C"
            context.fillRect(0.0, 0.0, state.gameState.canvasWidth, state.gameState.canvasHeight)
            when (val gameState = state.gameState) {
                is GameState.Playing -> {
                    gameState.enemyList.forEach {
                        if (it.checkCollision(gameState.player)) {
                            println("collision")
                        }
                        drawGameObject(context, it)
                    }
                }
            }
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
    ) {
        fun checkCollision(other: GameObject): Boolean {
            val centerX1 = this.x + this.width / 2
            val centerX2 = other.x + other.width / 2
            val centerY1 = this.y + this.height / 2
            val centerY2 = other.y + other.height / 2
            return abs(centerX1 - centerX2) < this.width / 2 + other.width / 2 &&
                    abs(centerY1 - centerY2) < this.height / 2 + other.width / 2
        }
    }

    sealed class GameState {
        abstract val player: GameObject
        abstract val frame: Int
        val canvasWidth = 800.0
        val canvasHeight = 600.0
        val groundY = 400.0

        protected fun enemy(): GameObject {
            return GameObject(
                     0.0,
                    groundY,
                    100.0,
                    100.0,
                    v1,
                    true
            )
        }

        sealed class Playing : GameState() {
            abstract fun calculateNextState(): GameState
            abstract val enemyList: List<GameObject>

            protected fun calculateNextEnemies(): List<GameObject> {
                if (frame % listOf(100, 200).random() == 0) {
                    return enemyList.asSequence()
                            .map {
                                it.copy(x = it.x + 8)
                            }.filter {
                                it.x < canvasWidth
                            }.plus(enemy())
                            .toList()
                } else {
                    return enemyList.asSequence()
                            .map {
                                it.copy(x = it.x + 8)
                            }.filter {
                                it.x < canvasWidth
                            }.toList()
                }
            }

            data class PlayerJumping(
                    override val player: GameObject,
                    override val enemyList: List<GameObject>,
                    private val t: Int,
                    override val frame: Int
            ) : Playing() {
                override fun calculateNextState(): GameState {
                    val gravity = 0.4
                    val vy = 13
                    val y = 0.5 * gravity * t * t - vy * t + (groundY)
                    return if (y <= groundY) {
                        PlayerJumping(player.copy(y = y), calculateNextEnemies(), t + 1, frame + 1)
                    } else {
                        PlayerRunning(player.copy(y = groundY), calculateNextEnemies(), frame + 1)
                    }
                }
            }

            data class PlayerRunning(
                    override val player: GameObject,
                    override val enemyList: List<GameObject>,
                    override val frame: Int
            ) : Playing() {
                override fun calculateNextState(): GameState {
                    return this.copy(enemyList = calculateNextEnemies(), frame = frame + 1)
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
