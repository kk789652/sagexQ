package com.tiswu.sagex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Fixed palette for deterministic avatar backgrounds. */
private val AVATAR_COLORS = listOf(
    Color(0xFF5B8FF9),
    Color(0xFF61DDAA),
    Color(0xFFF6BD16),
    Color(0xFFEF6F6C),
    Color(0xFF9270CA),
    Color(0xFF6DC8EC),
    Color(0xFFFF9D4D),
    Color(0xFF269A99),
)

/** First non-blank character (uppercased) of [name], or "?" when blank. */
fun avatarInitial(name: String): String {
    val ch = name.firstOrNull { !it.isWhitespace() } ?: return "?"
    return ch.uppercaseChar().toString()
}

/** Deterministic background color for [name]; stable across runs. */
fun avatarColor(name: String): Color {
    if (name.isBlank()) return AVATAR_COLORS.first()
    val index = ((name.hashCode() % AVATAR_COLORS.size) + AVATAR_COLORS.size) % AVATAR_COLORS.size
    return AVATAR_COLORS[index]
}

/** Rounded-square avatar showing the deterministic initial + color for [name]. */
@Composable
fun Avatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(avatarColor(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = avatarInitial(name),
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
