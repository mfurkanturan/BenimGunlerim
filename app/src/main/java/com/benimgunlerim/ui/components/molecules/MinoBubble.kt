package com.benimgunlerim.ui.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.AppTokens

/** Mino'nun konuşma balonu; köşesi Mino'ya bakan yuvarlak kutu. */
@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color? = MaterialTheme.colorScheme.outlineVariant,
) {
    val shape = RoundedCornerShape(
        topStart = AppTokens.Radius.lg,
        topEnd = AppTokens.Radius.lg,
        bottomEnd = AppTokens.Radius.lg,
        bottomStart = AppTokens.Radius.xs,
    )
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = textColor,
        modifier = modifier
            .background(background, shape)
            .then(if (borderColor != null) Modifier.border(AppTokens.BorderWidth.thin, borderColor, shape) else Modifier)
            .padding(horizontal = AppTokens.Spacing.sm, vertical = AppTokens.Spacing.xs),
    )
}

/** Mino + konuşma balonu, yan yana. */
@Composable
fun MinoBubble(
    text: String,
    modifier: Modifier = Modifier,
    mascotSize: Dp = AppTokens.IconSize.mascot,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Spacing.xs),
    ) {
        Image(
            painter = painterResource(R.drawable.mascot_mino),
            contentDescription = stringResource(R.string.mascot_content_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(mascotSize),
        )
        SpeechBubble(
            text = text,
            modifier = Modifier
                .padding(bottom = AppTokens.Spacing.xs)
                .widthIn(max = AppTokens.Sky.bubbleWidth * 1.2f),
        )
    }
}
