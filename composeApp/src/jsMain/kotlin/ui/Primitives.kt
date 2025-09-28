package ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun AppScaffold(topBar: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Div({
        style {
            property("min-height","100vh")
            property("display","flex"); property("flex-direction","column")
            property("background", Theme.bg); property("color", Theme.onSurface)
        }
    }) {
        topBar?.invoke()
        Div({ style { property("flex","1"); padding(Theme.pad) } }) { content() }
    }
}

@Composable
fun AppTopBar(
    onBack: (() -> Unit)? = null,
    titleLeft: String = "Respiro",
    titleRight: String = "Desk",
    actions: (@Composable () -> Unit)? = null,
    menu: (@Composable () -> Unit)? = null
) {
    Header({
        style {
            property("display","flex"); property("align-items","center")
            property("gap","8px"); property("padding","12px 16px")
            property("background", Theme.primary); property("color", Theme.onPrimary)
            property("position","sticky"); property("top","0"); property("z-index","5")
        }
    }) {
        if (onBack != null) {
            Button(attrs = {
                onClick { onBack() }
                style {
                    property("background","transparent"); property("border","none")
                    property("color", Theme.onPrimary); property("font-size","18px"); property("cursor","pointer")
                }
            }) { Text("←") }
        }
        H3({ style { property("margin","0") } }) {
            Text(titleLeft)
            Span({ style { property("color", Theme.primaryVariant); property("margin-left","6px") } }) { Text(titleRight) }
        }
        Div({ attr("style","margin-left:auto; display:flex; gap:8px;") }) {
            menu?.invoke()
            actions?.invoke()
        }
    }
}

@Composable
fun AppCard(content: @Composable () -> Unit) {
    Div({
        style {
            property("background", Theme.surface); property("border-radius", Theme.radius)
            property("box-shadow", Theme.shadow); property("padding", Theme.pad)
            property("margin-bottom","12px"); property("border","1px solid ${Theme.border}")
        }
    }) { content() }
}

@Composable
fun AppButton(onClick: () -> Unit, text: String) {
    Button(attrs = {
        onClick { onClick() }
        style {
            property("background", Theme.primary); property("color", Theme.onPrimary)
            property("border","none"); property("border-radius","10px")
            property("padding","10px 14px"); property("cursor","pointer")
        }
    }) { Text(text) }
}

@Composable
fun AppTextField(value: String, onChange: (String)->Unit, placeholder: String = "") {
    Input(type = InputType.Text, attrs = {
        this.value(value); this.placeholder(placeholder)
        onInput { e -> onChange(e.value) }
        style {
            property("width","100%"); property("padding","10px 12px")
            property("background","#0b1220"); property("color", Theme.onSurface)
            property("border","1px solid ${Theme.border}"); property("border-radius","10px")
        }
    })
}
