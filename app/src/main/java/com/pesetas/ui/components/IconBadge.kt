package com.pesetas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesetas.ui.util.IconCatalog

@Composable
fun IconBadge(
    iconKey: String,
    colorArgb: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val background = Color(colorArgb)
    val content = if (background.luminance() > 0.55f) Color.Black.copy(alpha = 0.7f) else Color.White
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = IconCatalog.iconFor(iconKey),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}
