package com.rk.detachment.ui.theme

import androidx.compose.ui.graphics.Color

// Liquid Glass Dark Canvas Palette
// Deep obsidian backdrop providing high contrast for transparent glass layers
val FrostedBackground = Color(0xFF0F172A)
val FrostedBackgroundDarker = Color(0xFF0A0F1D)

// Primary & Accent Colors
// Core brand tones for focus metrics, buttons, and state indicators
val IndigoPrimary = Color(0xFF6366F1)
val IndigoLight = Color(0xFF818CF8)
val IndigoSoft = Color(0xFFA5B4FC)
val IndigoDark = Color(0xFF4338CA)

// Functional Accent Colors
// Rose for hard limits/danger, Amber for mindful delays, Emerald for completed goals, Cyan for productivity
val RoseAccent = Color(0xFFF43F5E)
val RoseLight = Color(0xFFFB7185)
val AmberAccent = Color(0xFFF59E0B)
val AmberLight = Color(0xFFFBBF24)
val EmeraldAccent = Color(0xFF10B981)
val EmeraldLight = Color(0xFF34D399)
val CyanAccent = Color(0xFF06B6D4)

// Liquid Frosted Glass Surfaces
// Ultra-transparent glass layers creating airy depth without heavy white fills
val GlassSurfaceHigh = Color(0x10FFFFFF)      // ~6.5% white for prominent hero containers & cards
val GlassSurfaceMedium = Color(0x0AFFFFFF)    // ~4% white for liquid glass container rows & list items
val GlassSurfaceLow = Color(0x06FFFFFF)       // ~2.5% white for subtle tiles, chips & secondary rows
val GlassSurfaceUltraLow = Color(0x03FFFFFF)  // ~1.2% white for nested micro-containers

// Liquid Glass Specular Borders
// Refined luminous outlines giving containers their crisp glass edge reflection
val GlassBorderHigh = Color(0x28FFFFFF)       // ~16% white specular border for prominent cards
val GlassBorderMedium = Color(0x18FFFFFF)     // ~9.5% white specular border for standard rows
val GlassBorderLow = Color(0x0EFFFFFF)        // ~5.5% white border for subtle dividers & chips
val GlassHighlight = Color(0x35FFFFFF)        // ~21% white top-edge specular highlight

// Liquid Glass Floating Pill & Overlay Tokens
// Translucent obsidian glass and crystal indigo accents for the notch pill and overlay card
val GlassPillBackground = Color(0xD90F172A)   // Translucent liquid dark glass backdrop
val GlassPillSurface = Color(0x14FFFFFF)      // Liquid glass inner surface
val GlassPillBorder = Color(0x33818CF8)       // Indigo specular glass rim
val GlassPillBadgeBg = Color(0x24818CF8)      // Crystal indigo capsule fill
val GlassPillText = Color(0xFFF8FAFC)         // Crisp white readable text

// Typography & Slate Contrast Tones
// High-legibility text hierarchy optimized for translucent glass backgrounds
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFFCBD5E1)
val TextMuted = Color(0xFF64748B)

// Backward Compatibility Aliases
val NeonCyan = IndigoLight
val ElectricBlue = CyanAccent
val RadiantViolet = IndigoPrimary
val VividMagenta = RoseAccent
val SunsetAmber = AmberAccent
val EmeraldGreen = EmeraldAccent
val CoralRed = RoseAccent
val MidnightDark = FrostedBackground
val DeepObsidian = FrostedBackground
val SurfaceGlassDark = GlassSurfaceMedium
val SurfaceGlassBorder = GlassBorderMedium
val SurfaceGlassHighlight = Color(0x22818CF8)

