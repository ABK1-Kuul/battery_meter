package io.github.ifa.glancewidget.glance.glass

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.WidgetAccent
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.ui.glass.DotGlyphs
import io.github.ifa.glancewidget.ui.glass.DotRingGeometry
import io.github.ifa.glancewidget.ui.glass.GlassAccentMono
import io.github.ifa.glancewidget.ui.glass.GlassAccentRed
import io.github.ifa.glancewidget.ui.glass.GlassBlack
import io.github.ifa.glancewidget.ui.glass.GlassBorder
import io.github.ifa.glancewidget.ui.glass.GlassGhostDot
import io.github.ifa.glancewidget.ui.glass.GlassHighlight
import io.github.ifa.glancewidget.ui.glass.GlassMeniscus
import io.github.ifa.glancewidget.ui.glass.GlassSpecular
import io.github.ifa.glancewidget.ui.glass.GlassTextPrimary
import io.github.ifa.glancewidget.ui.glass.GlassTextSecondary
import io.github.ifa.glancewidget.ui.glass.GlassTintEnd
import io.github.ifa.glancewidget.ui.glass.SparklineScale
import io.github.ifa.glancewidget.ui.model.BatteryTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object GlassBitmapRenderer {
    private const val MAX_CACHE_SIZE_BYTES = 2 * 1024 * 1024
    private val cache = WidgetLruCache<WidgetBitmapCacheKey, Bitmap>(MAX_CACHE_SIZE_BYTES) { _, bitmap ->
        bitmap.byteCount
    }

    suspend fun renderWidgetBitmap(
        key: WidgetBitmapCacheKey,
        chartRecord: ChartRecord? = null,
        statusText: String = "",
        unitText: String = ""
    ): Bitmap = withContext(Dispatchers.Default) {
        cache.get(key)?.let { return@withContext it }
        val rendered = renderOffscreen(key, chartRecord, statusText, unitText)
        cache.put(key, rendered)
        rendered
    }

    fun renderOffscreen(
        key: WidgetBitmapCacheKey,
        chartRecord: ChartRecord? = null,
        statusText: String = "",
        unitText: String = ""
    ): Bitmap {
        val rawWidth = if (key.width <= 0) 300 else key.width
        val rawHeight = if (key.height <= 0) 300 else key.height

        val maxDim = 512
        val scale = if (rawWidth > maxDim || rawHeight > maxDim) {
            min(maxDim.toFloat() / rawWidth, maxDim.toFloat() / rawHeight)
        } else {
            1f
        }

        val width = (rawWidth * scale).toInt().coerceAtLeast(64)
        val height = (rawHeight * scale).toInt().coerceAtLeast(64)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isCapsule = key.style == WidgetSetting.Style.LiquidTube || key.style == WidgetSetting.Style.StatPill
        val cornerRadius = if (isCapsule) {
            min(width, height) / 2f
        } else {
            min(width, height) * 0.18f
        }

        drawGlassBackground(canvas, width.toFloat(), height.toFloat(), cornerRadius, key.opacity)

        val accentColor = when (key.accent) {
            WidgetAccent.Red -> GlassAccentRed.toArgb()
            WidgetAccent.Mono -> GlassAccentMono.toArgb()
            WidgetAccent.FollowTone -> when (key.tone) {
                BatteryTone.Hot, BatteryTone.Low -> GlassAccentRed.toArgb()
                BatteryTone.Charging, BatteryTone.Full -> GlassAccentMono.toArgb()
                else -> GlassAccentMono.toArgb()
            }
        }

        val textPrimary = GlassTextPrimary.toArgb()
        val textSecondary = GlassTextSecondary.toArgb()
        val ghostDot = GlassGhostDot.toArgb()

        when (key.style) {
            WidgetSetting.Style.DotRing -> drawDotRing(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot, statusText)
            WidgetSetting.Style.MatrixTile -> drawMatrixTile(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot, chartRecord, statusText, unitText)
            WidgetSetting.Style.GlyphBar -> drawGlyphBar(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot, statusText)
            WidgetSetting.Style.LiquidTube -> drawLiquidTube(canvas, width, height, key, accentColor, textPrimary, ghostDot)
            WidgetSetting.Style.Orbit -> drawOrbit(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot)
            WidgetSetting.Style.Dot -> drawDot(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot)
            WidgetSetting.Style.StatPill -> drawStatPill(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot)
            else -> drawDotRing(canvas, width, height, key, accentColor, textPrimary, textSecondary, ghostDot, statusText)
        }

        return bitmap
    }

    fun getReadableSummary(level: Int, isCharging: Boolean): String {
        return buildString {
            append("Battery ")
            append(level)
            append(" percent")
            if (isCharging) {
                append(", charging")
            }
        }
    }

    private fun drawGlassBackground(
        canvas: Canvas,
        width: Float,
        height: Float,
        cornerRadius: Float,
        opacity: Float
    ) {
        val rect = RectF(1.5f, 1.5f, width - 1.5f, height - 1.5f)
        val clampedOpacity = opacity.coerceIn(0.15f, 1f)

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = GlassBlack.toArgb()
            alpha = (255 * clampedOpacity).toInt()
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, basePaint)

        val tintShader = LinearGradient(
            0f, 0f, width, height,
            GlassBorder.toArgb(),
            GlassTintEnd.toArgb(),
            Shader.TileMode.CLAMP
        )
        val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = tintShader
            alpha = (255 * clampedOpacity).toInt()
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, tintPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = GlassBorder.toArgb()
            alpha = (255 * clampedOpacity).toInt()
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        val specularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = GlassSpecular.toArgb()
        }
        val specularPath = Path().apply {
            val inset = cornerRadius * 0.7f
            moveTo(inset, 2f)
            lineTo(width - inset, 2f)
        }
        canvas.drawPath(specularPath, specularPaint)
    }

    private fun drawDotRing(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        textSecondary: Int,
        ghostDot: Int,
        statusText: String
    ) {
        val cx = width / 2f
        val cy = height * 0.44f
        val minDim = min(width, height)
        val orbitRadius = minDim * 0.34f
        val dotRadius = minDim * 0.024f
        val totalDots = 44

        val litCount = DotRingGeometry.calculateLitCount(key.level / 100f, totalDots)
        val headIndex = DotRingGeometry.calculateHeadIndex(key.level / 100f, totalDots)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (i in 0 until totalDots) {
            val angle = (i.toFloat() / totalDots) * (2f * PI.toFloat()) - (PI.toFloat() / 2f)
            val x = cx + orbitRadius * cos(angle)
            val y = cy + orbitRadius * sin(angle)
            val color = when {
                i == headIndex -> accentColor
                i < litCount -> textPrimary
                else -> ghostDot
            }
            paint.color = color
            canvas.drawCircle(x, y, dotRadius, paint)
        }

        val levelStr = "${key.level}%"
        val textDotRadius = minDim * 0.015f
        val textDotGap = textDotRadius * 0.7f
        val charGap = textDotRadius * 1.8f
        val textWidth = measureDotTextWidth(levelStr, textDotRadius, textDotGap, charGap)
        val textHeight = measureDotTextHeight(textDotRadius, textDotGap)

        drawDotText(
            canvas = canvas,
            text = levelStr,
            startX = cx - textWidth / 2f,
            startY = cy - textHeight / 2f,
            dotRadius = textDotRadius,
            dotGap = textDotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val statusLabel = if (statusText.isNotEmpty()) {
            statusText.uppercase(Locale.US)
        } else if (key.isCharging) {
            "CHARGING"
        } else {
            "BATTERY"
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = minDim * 0.065f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText(statusLabel, cx, height * 0.90f, textPaint)
    }

    private fun drawMatrixTile(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        textSecondary: Int,
        ghostDot: Int,
        chartRecord: ChartRecord?,
        statusText: String,
        unitText: String
    ) {
        val minDim = min(width, height)
        val padding = minDim * 0.10f

        val label = if (statusText.isNotEmpty()) statusText.uppercase(Locale.US) else "BATTERY"
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = minDim * 0.075f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.08f
        }
        canvas.drawText(label, padding, padding + labelPaint.textSize, labelPaint)

        val valueStr = "${key.level}"
        val dotRadius = minDim * 0.028f
        val dotGap = dotRadius * 0.7f
        val charGap = dotRadius * 1.8f
        val valStartX = padding
        val valStartY = height * 0.30f
        val drawnWidth = drawDotText(
            canvas = canvas,
            text = valueStr,
            startX = valStartX,
            startY = valStartY,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val unit = if (unitText.isNotEmpty()) unitText else "%"
        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = minDim * 0.09f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText(unit, valStartX + drawnWidth + dotRadius * 2f, valStartY + measureDotTextHeight(dotRadius, dotGap), unitPaint)

        val sparkValues = chartRecord?.voltages?.takeIf { it.isNotEmpty() }
            ?: chartRecord?.temperatures
            ?: listOf(key.level.toFloat())
        val columnCount = 16
        val rowCount = 7
        val sparkRows = SparklineScale.scaleToRows(sparkValues, columnCount, rowCount)

        val sparkDotRadius = (width - padding * 2f) / (columnCount * 2.8f)
        val sparkDotGap = sparkDotRadius * 0.8f
        val sparkBottom = height - padding

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (c in 0 until columnCount) {
            val colX = padding + c * (sparkDotRadius * 2f + sparkDotGap) + sparkDotRadius
            val litRow = sparkRows[c]
            for (r in 0 until rowCount) {
                val rowY = sparkBottom - r * (sparkDotRadius * 2f + sparkDotGap) - sparkDotRadius
                val isLit = r <= litRow
                val color = when {
                    isLit && c == columnCount - 1 -> accentColor
                    isLit -> textPrimary
                    else -> ghostDot
                }
                dotPaint.color = color
                canvas.drawCircle(colX, rowY, sparkDotRadius, dotPaint)
            }
        }
    }

    private fun drawGlyphBar(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        textSecondary: Int,
        ghostDot: Int,
        statusText: String
    ) {
        val padding = height * 0.16f
        val levelStr = "${key.level}%"
        val dotRadius = height * 0.040f
        val dotGap = dotRadius * 0.7f
        val charGap = dotRadius * 1.6f
        val valWidth = measureDotTextWidth(levelStr, dotRadius, dotGap, charGap)
        val valHeight = measureDotTextHeight(dotRadius, dotGap)

        drawDotText(
            canvas = canvas,
            text = levelStr,
            startX = padding,
            startY = (height - valHeight) / 2f,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val rightStartX = padding + valWidth + padding * 1.2f
        val label = if (statusText.isNotEmpty()) {
            statusText.uppercase(Locale.US)
        } else if (key.isCharging) {
            "CHARGING"
        } else {
            "BATTERY"
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = height * 0.18f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.08f
        }
        canvas.drawText(label, rightStartX, height * 0.38f, labelPaint)

        val barDots = 20
        val availableWidth = (width - rightStartX - padding)
        val barDotRadius = (availableWidth / (barDots * 2.6f)).coerceAtMost(height * 0.07f)
        val barDotGap = barDotRadius * 0.6f
        val barY = height * 0.68f

        val litCount = ((key.level / 100f) * barDots).roundToInt().coerceIn(0, barDots)
        val headIndex = litCount - 1

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (i in 0 until barDots) {
            val cx = rightStartX + i * (barDotRadius * 2f + barDotGap) + barDotRadius
            val color = when {
                i == headIndex -> accentColor
                i < litCount -> textPrimary
                else -> ghostDot
            }
            dotPaint.color = color
            canvas.drawCircle(cx, barY, barDotRadius, dotPaint)
        }
    }

    private fun drawLiquidTube(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        ghostDot: Int
    ) {
        val radius = width / 2f
        val liquidAvailHeight = height - width * 0.9f
        val liquidHeight = (key.level / 100f) * liquidAvailHeight
        val liquidTopY = height - radius - liquidHeight

        val liquidPath = Path().apply {
            moveTo(2f, liquidTopY)
            lineTo(width - 2f, liquidTopY)
            lineTo(width - 2f, height - radius)
            arcTo(RectF(2f, height.toFloat() - width.toFloat(), width - 2f, height - 2f), 0f, 180f, false)
            lineTo(2f, liquidTopY)
            close()
        }

        val liquidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = accentColor
            alpha = 180
        }
        canvas.drawPath(liquidPath, liquidPaint)

        val meniscusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = GlassMeniscus.toArgb()
        }
        canvas.drawLine(4f, liquidTopY, width - 4f, liquidTopY, meniscusPaint)

        val levelStr = "${key.level}%"
        val dotRadius = width * 0.032f
        val dotGap = dotRadius * 0.7f
        val charGap = dotRadius * 1.6f
        val textWidth = measureDotTextWidth(levelStr, dotRadius, dotGap, charGap)
        val textHeight = measureDotTextHeight(dotRadius, dotGap)

        drawDotText(
            canvas = canvas,
            text = levelStr,
            startX = (width - textWidth) / 2f,
            startY = width * 0.40f - textHeight / 2f,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )
    }

    private fun drawOrbit(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        textSecondary: Int,
        ghostDot: Int
    ) {
        val cx = width / 2f
        val cy = height * 0.45f
        val minDim = min(width, height)

        val outerRadius = minDim * 0.38f
        val outerDots = 48
        val outerDotRadius = minDim * 0.020f
        val outerLit = DotRingGeometry.calculateLitCount(key.level / 100f, outerDots)
        val outerHead = DotRingGeometry.calculateHeadIndex(key.level / 100f, outerDots)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (i in 0 until outerDots) {
            val angle = (i.toFloat() / outerDots) * (2f * PI.toFloat()) - (PI.toFloat() / 2f)
            val x = cx + outerRadius * cos(angle)
            val y = cy + outerRadius * sin(angle)
            val color = when {
                i == outerHead -> accentColor
                i < outerLit -> textPrimary
                else -> ghostDot
            }
            paint.color = color
            canvas.drawCircle(x, y, outerDotRadius, paint)
        }

        val innerRadius = minDim * 0.25f
        val innerDots = 36
        val innerDotRadius = minDim * 0.018f
        val tempProgress = (key.tempRounded / 60f).coerceIn(0f, 1f)
        val innerLit = DotRingGeometry.calculateLitCount(tempProgress, innerDots)
        val innerHead = DotRingGeometry.calculateHeadIndex(tempProgress, innerDots)

        for (i in 0 until innerDots) {
            val angle = (i.toFloat() / innerDots) * (2f * PI.toFloat()) - (PI.toFloat() / 2f)
            val x = cx + innerRadius * cos(angle)
            val y = cy + innerRadius * sin(angle)
            val color = when {
                i == innerHead -> accentColor
                i < innerLit -> textPrimary
                else -> ghostDot
            }
            paint.color = color
            canvas.drawCircle(x, y, innerDotRadius, paint)
        }

        val levelStr = "${key.level}"
        val textDotRadius = minDim * 0.015f
        val textDotGap = textDotRadius * 0.7f
        val charGap = textDotRadius * 1.6f
        val textWidth = measureDotTextWidth(levelStr, textDotRadius, textDotGap, charGap)
        val textHeight = measureDotTextHeight(textDotRadius, textDotGap)

        drawDotText(
            canvas = canvas,
            text = levelStr,
            startX = cx - textWidth / 2f,
            startY = cy - textHeight / 2f,
            dotRadius = textDotRadius,
            dotGap = textDotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = false
        )

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = minDim * 0.065f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("ORBIT", cx, height * 0.92f, textPaint)
    }

    private fun drawDot(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        textSecondary: Int,
        ghostDot: Int
    ) {
        val cx = width / 2f
        val cy = height * 0.44f
        val minDim = min(width, height)

        val levelStr = "${key.level}"
        val dotRadius = minDim * 0.038f
        val dotGap = dotRadius * 0.7f
        val charGap = dotRadius * 1.6f
        val textWidth = measureDotTextWidth(levelStr, dotRadius, dotGap, charGap)
        val textHeight = measureDotTextHeight(dotRadius, dotGap)

        drawDotText(
            canvas = canvas,
            text = levelStr,
            startX = cx - textWidth / 2f,
            startY = cy - textHeight / 2f,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val accentDotRadius = minDim * 0.055f
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = accentColor
        }
        canvas.drawCircle(cx, height * 0.76f, accentDotRadius, dotPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = minDim * 0.070f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("%", cx, height * 0.90f, textPaint)
    }

    private fun drawStatPill(
        canvas: Canvas,
        width: Int,
        height: Int,
        key: WidgetBitmapCacheKey,
        accentColor: Int,
        textPrimary: Int,
        textSecondary: Int,
        ghostDot: Int
    ) {
        val colWidth = width / 3f

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = GlassBorder.toArgb()
        }
        canvas.drawLine(colWidth, height * 0.20f, colWidth, height * 0.80f, dividerPaint)
        canvas.drawLine(colWidth * 2f, height * 0.20f, colWidth * 2f, height * 0.80f, dividerPaint)

        val dotRadius = height * 0.036f
        val dotGap = dotRadius * 0.7f
        val charGap = dotRadius * 1.6f

        val tempStr = "${key.tempRounded.toInt()}°"
        val tempWidth = measureDotTextWidth(tempStr, dotRadius, dotGap, charGap)
        val textHeight = measureDotTextHeight(dotRadius, dotGap)

        drawDotText(
            canvas = canvas,
            text = tempStr,
            startX = colWidth * 0.5f - tempWidth / 2f,
            startY = height * 0.38f - textHeight / 2f,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val voltStr = String.format(Locale.US, "%.1f", key.voltRounded) + "V"
        val voltWidth = measureDotTextWidth(voltStr, dotRadius, dotGap, charGap)
        drawDotText(
            canvas = canvas,
            text = voltStr,
            startX = colWidth * 1.5f - voltWidth / 2f,
            startY = height * 0.38f - textHeight / 2f,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val batStr = "${key.level}%"
        val batWidth = measureDotTextWidth(batStr, dotRadius, dotGap, charGap)
        drawDotText(
            canvas = canvas,
            text = batStr,
            startX = colWidth * 2.5f - batWidth / 2f,
            startY = height * 0.38f - textHeight / 2f,
            dotRadius = dotRadius,
            dotGap = dotGap,
            charGap = charGap,
            litColor = textPrimary,
            ghostColor = ghostDot,
            drawGhost = true
        )

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = height * 0.16f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }

        canvas.drawText("TEMP", colWidth * 0.5f, height * 0.78f, labelPaint)
        canvas.drawText("VOLT", colWidth * 1.5f, height * 0.78f, labelPaint)
        canvas.drawText("BAT", colWidth * 2.5f, height * 0.78f, labelPaint)

        val accentDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = accentColor
        }
        canvas.drawCircle(colWidth * 2.5f + height * 0.22f, height * 0.73f, height * 0.035f, accentDotPaint)
    }

    private fun drawDotText(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        dotRadius: Float,
        dotGap: Float,
        charGap: Float,
        litColor: Int,
        ghostColor: Int,
        drawGhost: Boolean
    ): Float {
        var currentX = startX
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (char in text) {
            for (r in 0 until DotGlyphs.ROW_COUNT) {
                for (c in 0 until DotGlyphs.COL_COUNT) {
                    val cx = currentX + c * (dotRadius * 2f + dotGap) + dotRadius
                    val cy = startY + r * (dotRadius * 2f + dotGap) + dotRadius
                    val isLit = DotGlyphs.isLit(char, r, c)
                    if (isLit) {
                        paint.color = litColor
                        canvas.drawCircle(cx, cy, dotRadius, paint)
                    } else if (drawGhost) {
                        paint.color = ghostColor
                        canvas.drawCircle(cx, cy, dotRadius, paint)
                    }
                }
            }
            currentX += DotGlyphs.COL_COUNT * (dotRadius * 2f + dotGap) + charGap
        }
        return currentX - startX
    }

    private fun measureDotTextWidth(
        text: String,
        dotRadius: Float,
        dotGap: Float,
        charGap: Float
    ): Float {
        if (text.isEmpty()) return 0f
        val charWidth = DotGlyphs.COL_COUNT * (dotRadius * 2f + dotGap)
        return text.length * charWidth + (text.length - 1) * charGap
    }

    private fun measureDotTextHeight(dotRadius: Float, dotGap: Float): Float {
        return DotGlyphs.ROW_COUNT * (dotRadius * 2f + dotGap) - dotGap
    }
}
