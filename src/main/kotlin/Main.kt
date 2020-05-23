import component.Game
import react.dom.render
import kotlin.browser.document

fun main() {
    render(document.getElementById("root")) {
        child(Game::class) {
        }
    }
}
