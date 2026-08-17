package com.pesetas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesetas.ui.util.IconCatalog
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class DonutSlice(
    val value: Double,
    val color: Color,
    val iconKey: String? = null,
)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    center: @Composable (selectedIndex: Int?) -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 250.dp,
    thickness: Dp = 44.dp,
) {
    val total = slices.sumOf { it.value }.toFloat()
    val progress by animateFloatAsState(
        targetValue = if (total > 0f) 1f else 0f,
        animationSpec = tween(600),
        label = "donut",
    )
    var selected by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .size(diameter)
            .pointerInput(slices) {
                detectTapGestures(
                    onPress = { position ->
                        val hit = sliceAt(position, size.width.toFloat(), thickness.toPx(), slices)
                        if (hit != null) {
                            selected = hit
                            tryAwaitRelease()
                            selected = null
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = Stroke(width = thickness.toPx())
            val inset = thickness.toPx() / 2
            val arcSize = Size(size.width - thickness.toPx(), size.height - thickness.toPx())
            val topLeft = Offset(inset, inset)
            if (total <= 0f) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                return@Canvas
            }
            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = (slice.value.toFloat() / total) * 360f * progress
                val dimmed = selected != null && selected != index
                drawArc(
                    color = if (dimmed) slice.color.copy(alpha = 0.3f) else slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                startAngle += sweep
            }
        }

        if (total > 0f) {
            val ringRadius = (diameter.value - thickness.value) / 2f
            val maxIconSize = thickness.value * 0.5f
            val minIconSize = 9.5f
            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val fraction = slice.value.toFloat() / total
                val sweep = fraction * 360f
                val arcWidth = (sweep.toDouble() * kotlin.math.PI / 180.0).toFloat() * ringRadius
                val iconSize = (arcWidth * 0.82f).coerceAtMost(maxIconSize)
                if (slice.iconKey != null && iconSize >= minIconSize) {
                    val angle = (startAngle + sweep / 2f).toDouble() * kotlin.math.PI / 180.0
                    val x = (ringRadius * cos(angle)).toFloat()
                    val y = (ringRadius * sin(angle)).toFloat()
                    val faded = selected != null && selected != index
                    val tint = if (slice.color.luminance() > 0.55f) {
                        Color.Black.copy(alpha = 0.75f)
                    } else {
                        Color.White
                    }
                    Icon(
                        imageVector = IconCatalog.iconFor(slice.iconKey),
                        contentDescription = null,
                        tint = if (faded) tint.copy(alpha = 0.3f) else tint,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = x.dp, y = y.dp)
                            .size(iconSize.dp),
                    )
                }
                startAngle += sweep
            }
        }

        center(selected)
    }
}

private fun sliceAt(
    position: Offset,
    widthPx: Float,
    thicknessPx: Float,
    slices: List<DonutSlice>,
): Int? {
    val total = slices.sumOf { it.value }
    if (total <= 0.0) return null
    val cx = widthPx / 2f
    val distance = hypot(position.x - cx, position.y - cx)
    val outer = widthPx / 2f
    val inner = outer - thicknessPx
    if (distance > outer || distance < inner * 0.7f) return null
    val degrees = atan2((position.y - cx).toDouble(), (position.x - cx).toDouble()) *
        180.0 / kotlin.math.PI
    var relative = (degrees + 90.0)
    if (relative < 0) relative += 360.0
    var accumulated = 0.0
    slices.forEachIndexed { index, slice ->
        val sweep = slice.value / total * 360.0
        if (relative >= accumulated && relative < accumulated + sweep) return index
        accumulated += sweep
    }
    return slices.lastIndex.takeIf { it >= 0 }
}
