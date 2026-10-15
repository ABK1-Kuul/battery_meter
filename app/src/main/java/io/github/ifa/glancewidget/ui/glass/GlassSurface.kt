package io.github.ifa.glancewidget.ui.glass

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

val LocalHazeState = compositionLocalOf<HazeState?> { null }

object GlassBlurTracker {
    private val activeCount = AtomicInteger(0)

    fun tryAcquire(): Boolean {
        while (true) {
            val current = activeCount.get()
            if (current >= 4) return false
            if (activeCount.compareAndSet(current, current + 1)) return true
        }
    }

    fun release() {
        activeCount.updateAndGet { if (it > 0) it - 1 else 0 }
    }
}

object GlassNoiseCache {
    val bitmap: Bitmap by lazy {
        val size = 128
        val bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(42L)
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            val v = random.nextInt(256)
            pixels[i] = android.graphics.Color.argb(255, v, v, v)
        }
        bm.setPixels(pixels, 0, size, 0, 0, size, size)
        bm
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable () -> Unit
) {
    val isGlassTheme = LocalIsGlassTheme.current
    if (!isGlassTheme) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            content = content
        )
        return
    }

    val context = LocalContext.current
    val powerManager = remember(context) {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }
    val isPowerSaveMode = powerManager?.isPowerSaveMode == true
    val frostValue = LocalGlassFrost.current
    val frostDp = frostValue.coerceIn(0, 32).dp
    val isApiSupported = Build.VERSION.SDK_INT >= 31

    val canBlur = isApiSupported && !isPowerSaveMode && frostValue > 0

    var hasBlurSlot by remember { mutableStateOf(false) }
    DisposableEffect(canBlur) {
        if (canBlur) {
            hasBlurSlot = GlassBlurTracker.tryAcquire()
        }
        onDispose {
            if (hasBlurSlot) {
                GlassBlurTracker.release()
                hasBlurSlot = false
            }
        }
    }

    val useBlur = canBlur && hasBlurSlot && hazeState != null

    val baseModifier = modifier.clip(shape)

    val backdropModifier = if (useBlur) {
        baseModifier.hazeEffect(
            state = hazeState,
            style = HazeDefaults.style(
                backgroundColor = GlassBlack.copy(alpha = 0.50f),
                blurRadius = frostDp,
                noiseFactor = 0.16f
            )
        )
    } else {
        baseModifier.background(GlassSurfaceContainerHigh)
    }

    val noiseImage = remember { GlassNoiseCache.bitmap.asImageBitmap() }

    Box(
        modifier = backdropModifier.drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val tintBrush = Brush.linearGradient(
                colors = listOf(GlassTintStart, GlassTintEnd),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
            val radialHighlight = Brush.radialGradient(
                colors = listOf(GlassHighlight, Color.Transparent),
                center = Offset.Zero,
                radius = size.minDimension * 0.75f
            )
            val stroke1dp = 1.dp.toPx()

            onDrawWithContent {
                drawOutline(outline = outline, brush = tintBrush)
                drawOutline(outline = outline, brush = radialHighlight)
                drawOutline(
                    outline = outline,
                    color = GlassBorder,
                    style = Stroke(width = stroke1dp)
                )

                drawLine(
                    color = GlassSpecular,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = stroke1dp
                )

                val noiseCols = (size.width / 128f).toInt() + 1
                val noiseRows = (size.height / 128f).toInt() + 1
                for (x in 0 until noiseCols) {
                    for (y in 0 until noiseRows) {
                        drawImage(
                            image = noiseImage,
                            dstOffset = IntOffset(x * 128, y * 128),
                            dstSize = IntSize(128, 128),
                            alpha = 0.16f,
                            blendMode = BlendMode.Overlay
                        )
                    }
                }

                drawContent()
            }
        }
    ) {
        content()
    }
}

@ThemePreviews
@Composable
fun GlassSurfacePreview() {
    MaterialTheme {
        GlassSurface(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Glass Surface Preview",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
