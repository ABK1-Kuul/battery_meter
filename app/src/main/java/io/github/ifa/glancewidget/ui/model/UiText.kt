package io.github.ifa.glancewidget.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Resource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText
    data class Compound(
        val prefix: UiText,
        val separator: String = " ",
        val suffix: UiText
    ) : UiText
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.Plain -> value
        is UiText.Resource -> if (args.isEmpty()) {
            stringResource(id = id)
        } else {
            stringResource(id = id, *args.toTypedArray())
        }
        is UiText.Compound -> "${prefix.asString()}$separator${suffix.asString()}"
    }
}
