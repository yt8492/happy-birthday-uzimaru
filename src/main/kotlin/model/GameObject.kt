package model

import org.w3c.dom.Image
import kotlin.math.abs

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