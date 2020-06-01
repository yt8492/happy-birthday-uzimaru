package model

import component.Game
import org.w3c.dom.Image

sealed class GameState {
    abstract val player: GameObject
    abstract val frame: Int
    abstract val enemyList: List<GameObject>
    abstract val score: Int

    object Start : GameState() {
        override val player: GameObject = uzimaru1
        override val frame: Int = 0
        override val enemyList: List<GameObject> = emptyList()
        override val score: Int = 0
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
                        }.plus(enemy)
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
                return End.GameClear(player.copy(y = groundY), enemyList, frame, score)
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
                    uzimaru1.copy(y = playerY)
                } else {
                    uzimaru2.copy(y = playerY)
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
            fun jumpStart(): PlayerJumping {
                return PlayerJumping(
                        player,
                        enemyList,
                        0,
                        frame,
                        score
                )
            }

            override fun calculateNextState(): GameState {
                calculateGameEnd()?.let {
                    return it
                }
                val nextPlayer = if (score < evolutionScore) {
                    uzimaru1
                } else {
                    uzimaru2
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
        const val canvasWidth = 800.0
        const val canvasHeight = 600.0
        const val groundY = 400.0
        const val evolutionScore = 1000
        const val clearScore = 2000
        const val playerX = 600.0
        const val playerWidth = 100.0
        const val playerHeight = 100.0
        const val enemyWidth = 100.0
        const val enemyHeight = 100.0
        const val chickenWidth = 200.0
        const val chickenHeight = 200.0

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

        val uzimaru1 = GameObject(
                playerX,
                groundY,
                playerWidth,
                playerHeight,
                v2
        )
        val uzimaru2 = GameObject(
                playerX,
                groundY,
                playerWidth,
                playerHeight,
                v3
        )
        val enemy = GameObject(
                0.0,
                groundY,
                enemyWidth,
                enemyHeight,
                v1
        )
        val chicken = GameObject(
                canvasWidth / 2 - chickenWidth / 2,
                canvasHeight / 2,
                chickenWidth,
                chickenHeight,
                v4
        )

        fun newGame(): GameState {
            return Playing.PlayerRunning(
                    uzimaru1,
                    emptyList(),
                    0,
                    0
            )
        }

    }
}