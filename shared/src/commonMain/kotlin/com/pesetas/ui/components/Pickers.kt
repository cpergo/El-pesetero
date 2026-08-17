package com.pesetas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.pesetas.ui.util.IconCatalog

val CategoryColors: List<Int> = listOf(
    0xFFB4432E, 0xFFE08B3B, 0xFFC89B3C, 0xFF6FBF8C, 0xFF3E9E68,
    0xFF2D9596, 0xFF3F72AF, 0xFF6D597A, 0xFFC0619E, 0xFF8A6D3B,
    0xFF2D5F4C, 0xFF4C7A5B, 0xFF9C6644, 0xFF5A7D9A, 0xFFB5651D,
    0xFF7B6D8D, 0xFFA23E48, 0xFF3D8361, 0xFF1C6DD0, 0xFF525252,
).map { it.toInt() }

@Composable
fun IconPickerDialog(
    selectedKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = { Text("Elige un icono") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(52.dp),
                state = rememberLazyGridState(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                items(IconCatalog.keys, key = { it }) { key ->
                    val selected = key == selectedKey
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            )
                            .clickable { onSelect(key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = IconCatalog.iconFor(key),
                            contentDescription = key,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun ColorPickerGrid(
    selectedColor: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(44.dp),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 180.dp),
    ) {
        items(CategoryColors, key = { it }) { colorArgb ->
            val color = Color(colorArgb)
            val selected = colorArgb == selectedColor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = CircleShape,
                    )
                    .clickable { onSelect(colorArgb) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (color.luminance() > 0.55f) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
