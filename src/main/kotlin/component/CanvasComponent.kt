package component

import kotlinx.html.tabIndex
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.canvas

fun RBuilder.canvasComponent(width: String, height: String, updateCanvas: (CanvasRenderingContext2D) -> Unit) {
    child(CanvasComponent::class) {
        attrs.width = width
        attrs.height = height
        attrs.updateCanvas = updateCanvas
    }
}

class CanvasComponent : RComponent<CanvasComponent.Props, CanvasComponent.State>() {

    override fun componentDidMount() {
        updateCanvas()
    }

    override fun componentDidUpdate(prevProps: Props, prevState: State, snapshot: Any) {
        updateCanvas()
    }

    private fun updateCanvas() {
        val context = state.canvas.getContext("2d") as CanvasRenderingContext2D
        props.updateCanvas(context)
    }

    override fun RBuilder.render() {
        canvas {
            ref {
                state.canvas = it
            }
            attrs.tabIndex = "canvas"
            attrs.width = props.width
            attrs.height = props.height
        }
    }

    interface Props : RProps {
        var width: String
        var height: String
        var updateCanvas: (CanvasRenderingContext2D) -> Unit
    }

    interface State: RState {
        var canvas: HTMLCanvasElement
    }
}
