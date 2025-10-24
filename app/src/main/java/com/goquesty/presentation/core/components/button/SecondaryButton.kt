package com.goquesty.presentation.core.components.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.goquesty.presentation.core.theme.GoquestlyTheme

@Composable
fun SecondaryButton(
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
        backgroundColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary
    )
}

@Preview
@Composable
fun SecondaryButtonPreview() {
    GoquestlyTheme {
        SecondaryButton(
            text = "Secondary Button",
            onClick = {}
        )
    }
}