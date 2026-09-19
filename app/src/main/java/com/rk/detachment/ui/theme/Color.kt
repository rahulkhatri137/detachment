package com.rk.detachment.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass Dark Canvas Palette
// Deep obsidian backdrop keeping background color consistent while glass elements remain transparent
val FrostedBackground = Color(0xFF0F172A)
val FrostedBackgroundDarker = Color(0xFF0A0F1D)

// Primary & Purple Accent Colors (Derived from the Liquid Glass Lavender/Purple Design)
// Modern vibrant purple tones for focus metrics, action buttons, active switches, and state indicators
val PurplePrimary = Color(0xFF7C5CF0)        // Core purple accent (#7C5CF0) matching the switch and active tiles
val PurpleLight = Color(0xFF9E86E8)          // Radiant lavender accent (#9E86E8) for icons, badges, and highlights
val PurpleSoft = Color(0xFFD6C8FB)           // Soft lilac tint (#D6C8FB) for secondary container text
val PurpleDark = Color(0xFF4C338A)           // Deep slate-purple (#4C338A) for primary container fills

// Functional Accent Colors
// Rose for hard limits/danger, Amber for mindful delays, Emerald for completed goals, Cyan for productivity
val RoseAccent = Color(0xFFF43F5E)
val RoseLight = Color(0xFFFB7185)
val AmberAccent = Color(0xFFF59E0B)
val AmberLight = Color(0xFFFBBF24)
val EmeraldAccent = Color(0xFF10B981)
val EmeraldLight = Color(0xFF34D399)
val CyanAccent = Color(0xFF06B6D4)

// Liquid Frosted Glass Surfaces (Vision-style Acrylic & Liquid Glass)
// Multi-stop translucent glass fills that let the dark obsidian background shine through
val GlassSurfaceHigh = Color(0x0EFFFFFF)      // ~5.5% white for prominent hero containers & cards
val GlassSurfaceMedium = Color(0x08FFFFFF)    // ~3% white for liquid glass container rows & list items
val GlassSurfaceLow = Color(0x05FFFFFF)       // ~2% white for subtle tiles, chips & secondary rows
val GlassSurfaceUltraLow = Color(0x03FFFFFF)  // ~1.2% white for nested micro-containers

// Liquid Glass Specular Borders (Bright top-lit rim reflections & chromatic edge depth)
val GlassBorderHigh = Color(0x40FFFFFF)       // ~25% white specular highlight for top edges
val GlassBorderMedium = Color(0x22FFFFFF)     // ~13.5% white specular rim for standard rows
val GlassBorderLow = Color(0x12FFFFFF)        // ~7% white translucent edge
val GlassHighlight = Color(0x60FFFFFF)        // ~38% white brilliant top-edge specular glint

// Liquid Glass Gradient Brushes for Containers & Rows
val LiquidGlassBodyGradient = listOf(
    Color.White.copy(alpha = 0.12f),
    Color.White.copy(alpha = 0.04f),
    Color.White.copy(alpha = 0.02f),
    Color.White.copy(alpha = 0.06f)
)

val LiquidGlassBorderGradient = listOf(
    Color.White.copy(alpha = 0.60f),
    Color.White.copy(alpha = 0.20f),
    Color.White.copy(alpha = 0.10f),
    Color.White.copy(alpha = 0.35f)
)

// Liquid Glass Floating Notch Pill & Block Overlay Tokens
val GlassPillBackground = Color(0xBF0F172A)   // Ultra-clear dark liquid glass backdrop (~75% opacity)
val GlassPillSurface = Color(0x14FFFFFF)      // Liquid glass inner surface
val GlassPillBorder = Color(0x40FFFFFF)       // Bright specular glass rim
val GlassPillBadgeBg = Color(0x28FFFFFF)      // Translucent liquid badge fill
val GlassPillText = Color(0xFFF8FAFC)         // Crisp white readable text

// Typography & Slate Contrast Tones
// High-legibility text hierarchy optimized for translucent glass backgrounds
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFFCBD5E1)
val TextMuted = Color(0xFF64748B)

// Backward Compatibility Aliases
val NeonCyan = PurpleLight
val ElectricBlue = CyanAccent
val RadiantViolet = PurplePrimary
val VividMagenta = RoseAccent
val SunsetAmber = AmberAccent
val EmeraldGreen = EmeraldAccent
val CoralRed = RoseAccent
val MidnightDark = FrostedBackground
val DeepObsidian = FrostedBackground
val SurfaceGlassDark = GlassSurfaceMedium
val SurfaceGlassBorder = GlassBorderMedium
val SurfaceGlassHighlight = Color(0x229E86E8)


