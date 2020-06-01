package component

import model.GameObject
import model.GameState
import org.w3c.dom.CanvasRenderingContext2D
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
            gameState = GameState.Start
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
                is GameState.Start -> {
                    setState {
                        gameState = GameState.newGame()
                    }
                }
                is GameState.Playing.PlayerRunning -> {
                    setState {
                        gameState = currentState.jumpStart()
                    }
                }
                is GameState.End -> {
                    setState {
                        gameState = GameState.Start
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
        context.fillRect(0.0, 0.0, GameState.canvasWidth, GameState.canvasHeight)
        context.fillStyle = "#199861"
        context.font = "24px \"VT323\""
        context.fillText("score: ${gameState.score}", 50.0, 50.0)
        when (gameState) {
            is GameState.Start -> {
                val statusText = "Please Press Space Key!"
                val textWidth = context.measureText(statusText).width
                context.font = "32px \"VT323\""
                context.fillText(statusText, GameState.canvasWidth / 2 - textWidth / 2, GameState.canvasHeight / 2)
            }
            is GameState.Playing -> {
                gameState.enemyList.forEach {
                    drawGameObject(context, it)
                }
                drawGameObject(context, gameState.player)
            }
            is GameState.End.GameOver -> {
                drawGameObject(context, gameState.player)
                gameState.enemyList.forEach {
                    drawGameObject(context, it)
                }
                val statusText = "Game Over!"
                val textWidth = context.measureText(statusText).width
                context.font = "32px \"VT323\""
                context.fillText(statusText, GameState.canvasWidth / 2 - textWidth / 2, GameState.canvasHeight / 2)
            }
            is GameState.End.GameClear -> {
                drawGameObject(context, gameState.player)
                drawGameObject(context, GameState.chicken)
                val statusText = "Happy Birthday uzimaru!"
                val textWidth = context.measureText(statusText).width
                context.font = "32px \"VT323\""
                context.fillText(statusText, GameState.canvasWidth / 2 - textWidth / 2, GameState.canvasHeight / 2)

            }
        }
        drawGameObject(context, gameState.player)
    }

    override fun RBuilder.render() {
        canvasComponent(
                GameState.canvasWidth.toString(),
                GameState.canvasHeight.toString()
        ) { context ->
            drawGameState(context, state.gameState)
        }
    }

    interface State : RState {
        var gameState: GameState
    }
}
