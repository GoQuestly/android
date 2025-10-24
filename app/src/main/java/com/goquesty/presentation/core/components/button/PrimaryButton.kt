package com.goquesty.presentation.core.components.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.surface
    )
}

@Preview
@Composable
fun PrimaryButtonPreview() {
    GoquestlyTheme {
        PrimaryButton(
            text = "Primary Button",
            onClick = {}
        )
    }
}