package com.quacko.billing.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quacko.billing.ui.theme.QuackoAmber
import com.quacko.billing.ui.theme.QuackoGreen
import com.quacko.billing.ui.theme.QuackoRed
import com.quacko.billing.ui.theme.QuackoTeal
import java.util.Locale

fun money(amount: Double): String =
    "$" + String.format(Locale.US, "%,.2f", amount)

fun minutesLabel(min: Int): String {
    val h = min / 60
    val m = min % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/** A compact stat tile used in the totals row. */
@Composable
fun StatTile(
    label: String,
    value: String,
    accent: Color = QuackoTeal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(accent)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) { Text(" ", style = MaterialTheme.typography.labelSmall) }
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/** A small colored pill for tags (origin, language, capped). */
@Composable
fun Pill(text: String, bg: Color, fg: Color = Color.White) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, color = fg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OriginPill(is800: Boolean) =
    Pill(if (is800) "800" else "Web", if (is800) QuackoAmber else QuackoTeal)

@Composable
fun CappedPill() = Pill("⚠ CAP", QuackoRed)

object Accents {
    val teal = QuackoTeal
    val amber = QuackoAmber
    val red = QuackoRed
    val green = QuackoGreen
}
