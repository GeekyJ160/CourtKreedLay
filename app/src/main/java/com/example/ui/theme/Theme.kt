package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CourtDarkColorScheme = darkColorScheme(
    primary = CourtOrange,
    onPrimary = ObsidianBg,
    primaryContainer = CourtOrangeContainer,
    onPrimaryContainer = CourtOrangeSoft,
    secondary = ElectricVolt,
    onSecondary = ObsidianBg,
    secondaryContainer = ElectricVoltContainer,
    onSecondaryContainer = ElectricVolt,
    tertiary = ArenaCyan,
    onTertiary = ObsidianBg,
    tertiaryContainer = ArenaCyanContainer,
    onTertiaryContainer = ArenaCyan,
    background = ObsidianBg,
    onBackground = ChalkWhite,
    surface = ObsidianSurface,
    onSurface = ChalkWhite,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = SlateText,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianSurfaceHigh,
    surfaceContainerHigh = ObsidianSurfaceHighest,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder.copy(alpha = 0.6f),
    error = CrimsonMiss,
    onError = ChalkWhite,
)

private val CourtLightColorScheme = lightColorScheme(
    primary = CourtOrange,
    onPrimary = ChalkWhite,
    primaryContainer = CourtOrange.copy(alpha = 0.14f),
    onPrimaryContainer = CourtOrangeContainer,
    secondary = ObsidianSurfaceHighest,
    onSecondary = ChalkWhite,
    secondaryContainer = ElectricVolt.copy(alpha = 0.25f),
    onSecondaryContainer = DeepInk,
    tertiary = ArenaCyanContainer,
    onTertiary = ChalkWhite,
    background = ArenaLightBg,
    onBackground = DeepInk,
    surface = ArenaLightSurface,
    onSurface = DeepInk,
    surfaceVariant = ArenaLightSurfaceVariant,
    onSurfaceVariant = DeepSlate,
    outline = ArenaLightSurfaceVariant,
    error = CrimsonMiss,
    onError = ChalkWhite,
)

val CourtShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CourtIQTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CourtDarkColorScheme else CourtLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CourtIqTypography,
        shapes = CourtShapes,
        content = content
    )
}
