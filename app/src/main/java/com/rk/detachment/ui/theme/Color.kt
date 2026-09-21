package com.rk.detachment.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Liquid Glass Dark Canvas Palette
val FrostedBackground = Color(0xFF0F172A)        // Deep obsidian navy (#0F172A) - main app canvas
val FrostedBackgroundDarker = Color(0xFF0A0F1D)  // Near-black slate (#0A0F1D) - bottom bars & headers

// Primary & Purple Accent Colors
val PurplePrimary = Color(0xFF7C5CF0)            // Vibrant purple (#7C5CF0) - primary buttons, toggles & active tiles
val PurpleLight = Color(0xFF9E86E8)              // Radiant lavender (#9E86E8) - icons, badges & score indicators
val PurpleSoft = Color(0xFFD6C8FB)               // Soft lilac (#D6C8FB) - secondary container text & chips
val PurpleDark = Color(0xFF4C338A)               // Deep slate purple (#4C338A) - container fills & card tinting

// Functional Accent Colors
val RoseAccent = Color(0xFFF43F5E)               // Vivid rose red (#F43F5E) - hard limits & danger/emergency actions
val RoseLight = Color(0xFFFB7185)                // Soft coral rose (#FB7185) - warning highlights & alert chips
val AmberAccent = Color(0xFFF59E0B)              // Warm amber orange (#F59E0B) - mindful delays & caution states
val AmberLight = Color(0xFFFBBF24)               // Light amber yellow (#FBBF24) - delay countdowns & sub-badges
val EmeraldAccent = Color(0xFF10B981)            // Fresh emerald green (#10B981) - completed goals, passes & unlocks
val EmeraldLight = Color(0xFF34D399)             // Mint green (#34D399) - success metrics & uptime chips
val CyanAccent = Color(0xFF06B6D4)               // Bright cyan (#06B6D4) - productivity tags & secondary charts

// Liquid Frosted Glass Surfaces
val GlassSurfaceHigh = Color(0x0EFFFFFF)         // ~5.5% white - prominent hero containers & modals
val GlassSurfaceMedium = Color(0x08FFFFFF)       // ~3% white - list item cards & standard rows
val GlassSurfaceLow = Color(0x05FFFFFF)          // ~2% white - subtle tiles, chips & secondary rows
val GlassSurfaceUltraLow = Color(0x03FFFFFF)     // ~1.2% white - nested micro-containers

// Liquid Glass Specular Borders
val GlassBorderHigh = Color(0x40FFFFFF)          // ~25% white rim - top specular highlight on active cards
val GlassBorderMedium = Color(0x22FFFFFF)        // ~13.5% white rim - standard card & row borders
val GlassBorderLow = Color(0x12FFFFFF)           // ~7% white rim - subtle divider lines & chip borders
val GlassHighlight = Color(0x60FFFFFF)           // ~38% white glint - intense specular border edges

// Liquid Glass Gradient Brushes
val LiquidGlassBodyGradient = listOf(            // Translucent gradient - card background bodies
    Color.White.copy(alpha = 0.12f),
    Color.White.copy(alpha = 0.04f),
    Color.White.copy(alpha = 0.02f),
    Color.White.copy(alpha = 0.06f)
)

val LiquidGlassBorderGradient = listOf(          // Specular gradient - glass border strokes
    Color.White.copy(alpha = 0.60f),
    Color.White.copy(alpha = 0.20f),
    Color.White.copy(alpha = 0.10f),
    Color.White.copy(alpha = 0.35f)
)

// Liquid Glass Floating Notch Pill Tokens
val GlassPillBackground = Color.Transparent      // 100% transparent - floating heads-up pill outer body
val GlassPillSurface = Color.Transparent         // 100% transparent - pill icon container fill
val GlassPillBorder = Color(0x40FFFFFF)          // ~25% white rim - floating pill specular outer stroke
val GlassPillBadgeBg = Color(0xEB0F172A)         // Deep midnight slate (~92% #0F172A) - time bubble background
val GlassPillText = Color(0xFFF8FAFC)            // Crisp off-white (#F8FAFC) - floating pill label text

// Typography & Slate Contrast Tones
val TextPrimary = Color(0xFFF8FAFC)              // Off-white (#F8FAFC) - primary titles & headings
val TextSecondary = Color(0xFF94A3B8)            // Muted slate (#94A3B8) - subtitles & body descriptions
val TextTertiary = Color(0xFFCBD5E1)             // Soft slate (#CBD5E1) - timestamps & auxiliary metadata
val TextMuted = Color(0xFF64748B)                // Dim blue-grey (#64748B) - disabled labels & hints

// Backward Compatibility Aliases
val NeonCyan = PurpleLight                       // Alias -> PurpleLight
val ElectricBlue = CyanAccent                    // Alias -> CyanAccent
val RadiantViolet = PurplePrimary                // Alias -> PurplePrimary
val VividMagenta = RoseAccent                    // Alias -> RoseAccent
val SunsetAmber = AmberAccent                    // Alias -> AmberAccent
val EmeraldGreen = EmeraldAccent                 // Alias -> EmeraldAccent
val CoralRed = RoseAccent                        // Alias -> RoseAccent
val MidnightDark = FrostedBackground             // Alias -> FrostedBackground
val DeepObsidian = FrostedBackground             // Alias -> FrostedBackground
val SurfaceGlassDark = GlassSurfaceMedium        // Alias -> GlassSurfaceMedium
val SurfaceGlassBorder = GlassBorderMedium      // Alias -> GlassBorderMedium
val SurfaceGlassHighlight = Color(0x229E86E8)    // Soft lavender shimmer overlay


