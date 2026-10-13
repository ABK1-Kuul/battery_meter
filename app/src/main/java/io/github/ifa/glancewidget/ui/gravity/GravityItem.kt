package io.github.ifa.glancewidget.ui.gravity

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.gravityItem(id: Any? = null): Modifier = composed {
    val controller = LocalGravityController.current ?: return@composed this
    val itemId = remember(id) { id ?: Any() }
    val itemState = remember(itemId) { GravityItemState() }

    DisposableEffect(itemId, controller) {
        controller.register(itemId, itemState)
        onDispose {
            controller.unregister(itemId)
        }
    }

    this
        .onGloballyPositioned { coordinates ->
            if (coordinates.isAttached) {
                itemState.center = coordinates.boundsInRoot().center
            }
        }
        .graphicsLayer {
            translationX = itemState.translationX.value
            translationY = itemState.translationY.value
            scaleX = itemState.scale.value
            scaleY = itemState.scale.value
            alpha = itemState.alpha.value
        }
}
