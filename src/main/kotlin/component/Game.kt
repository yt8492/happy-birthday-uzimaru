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

class Game : RComponent<RProps, Game.State>() {

    private var keydown = false

    init {
        state.apply {
            gameState = GameState.newGame()
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
        if (keydown) {
            keydown = false
            when (currentState) {
                is GameState.Playing.PlayerRunning -> {
                    setState {
                        gameState = GameState.Playing.PlayerJumping(
                                currentState.player,
                                currentState.enemyList,
                                0,
                                currentState.frame,
                                currentState.score
                        )
                    }
                }
                is GameState.End -> {
                    setState {
                        gameState = GameState.newGame()
                    }
                }
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
        context.drawImage(gameObject.image, gameObject.x, gameObject.y, gameObject.width, gameObject.height)
    }

    private fun drawGameState(context: CanvasRenderingContext2D, gameState: GameState) {
        context.fillStyle = "#3C3C3C"
        context.fillRect(0.0, 0.0, gameState.canvasWidth, gameState.canvasHeight)
        context.fillStyle = "#199861"
        context.font = "24px \"VT323\""
        context.fillText("score: ${gameState.score}", 50.0, 50.0)
        gameState.enemyList.forEach {
            drawGameObject(context, it)
        }
        drawGameObject(context, gameState.player)
    }

    override fun RBuilder.render() {
        canvasComponent(
                state.gameState.canvasWidth.toString(),
                state.gameState.canvasHeight.toString()
        ) { context ->
            drawGameState(context, state.gameState)
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
            val image: Image
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
        abstract val enemyList: List<GameObject>
        abstract val score: Int
        val canvasWidth = 800.0
        val canvasHeight = 600.0
        val groundY = 400.0
        val evolutionScore = 1000
        val clearScore = 2000

        protected fun enemy(): GameObject {
            return GameObject(
                     0.0,
                    groundY,
                    100.0,
                    100.0,
                    v1
            )
        }
        sealed class Playing : GameState() {
            abstract fun calculateNextState(): GameState

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

            protected fun calculateGameEnd(): End? {
                if (enemyList.any { it.checkCollision(player) }) {
                    return End.GameOver(player, enemyList, frame, score)
                }
                if (score >= clearScore) {
                    return End.GameClear(player, enemyList, frame, score)
                }
                return null
            }



            data class PlayerJumping(
                    override val player: GameObject,
                    override val enemyList: List<GameObject>,
                    private val t: Int,
                    override val frame: Int,
                    override val score: Int
            ) : Playing() {

                override fun calculateNextState(): GameState {
                    calculateGameEnd()?.let {
                        return it
                    }
                    val gravity = 0.4
                    val vy = 13
                    val y = 0.5 * gravity * t * t - vy * t + (groundY)
                    val playerY = if (y <= groundY) {
                        y
                    } else {
                        groundY
                    }
                    val nextPlayer = if (score < evolutionScore) {
                        uzimaru1().copy(y = playerY)
                    } else {
                        uzimaru2().copy(y = playerY)
                    }
                    return if (y <= groundY) {
                        PlayerJumping(nextPlayer, calculateNextEnemies(), t + 1, frame + 1, score + 1)
                    } else {
                        PlayerRunning(nextPlayer, calculateNextEnemies(), frame + 1, score + 1)
                    }
                }
            }

            data class PlayerRunning(
                    override val player: GameObject,
                    override val enemyList: List<GameObject>,
                    override val frame: Int,
                    override val score: Int
            ) : Playing() {
                override fun calculateNextState(): GameState {
                    calculateGameEnd()?.let {
                        return it
                    }
                    val nextPlayer = if (score < evolutionScore) {
                        uzimaru1()
                    } else {
                        uzimaru2()
                    }
                    return this.copy(player = nextPlayer, enemyList = calculateNextEnemies(), frame = frame + 1, score = score + 1)
                }
            }
        }

        sealed class End : GameState() {
            data class GameClear(
                    override val player: GameObject,
                    override val enemyList: List<GameObject>,
                    override val frame: Int,
                    override val score: Int
            ) : End()

            data class GameOver(
                    override val player: GameObject,
                    override val enemyList: List<GameObject>,
                    override val frame: Int,
                    override val score: Int
            ) : End()
        }

        companion object {
            fun newGame(): GameState {
                return Playing.PlayerRunning(
                        GameObject(
                                600.0,
                                400.0,
                                100.0,
                                100.0,
                                v3
                        ),
                        emptyList(),
                        0,
                        0
                )
            }

            fun uzimaru1(): GameObject {
                return GameObject(
                        600.0,
                        400.0,
                        100.0,
                        100.0,
                        v2
                )
            }

            fun uzimaru2(): GameObject {
                return GameObject(
                        600.0,
                        400.0,
                        100.0,
                        100.0,
                        v3
                )
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
