package io.github.gaurav95998030.habittracker.ui.theme

/**
 * Phosphor icon glyphs (MIT, phosphoricons.com) used by the HabitForge design.
 * The fonts in res/font/phosphor_*.ttf are subset to exactly these code points —
 * to add an icon, re-run the subset step with the new name and add it here.
 * Render with [PhIcon]. [byName] resolves the design's "ph-xxx" names (stored on habits).
 */
object PhIcons {
    const val ArrowClockwise: String = "\uE036"
    const val ArrowCounterClockwise: String = "\uE038"
    const val ArrowLeft: String = "\uE058"
    const val ArrowRight: String = "\uE06C"
    const val Barbell: String = "\uE0B6"
    const val Bell: String = "\uE0CE"
    const val BellRinging: String = "\uE5E8"
    const val BellSlash: String = "\uE0D4"
    const val BookOpen: String = "\uE0E6"
    const val Brain: String = "\uE74E"
    const val CaretDown: String = "\uE136"
    const val CaretLeft: String = "\uE138"
    const val CaretRight: String = "\uE13A"
    const val ChartLineUp: String = "\uE156"
    const val Check: String = "\uE182"
    const val CheckCircle: String = "\uE184"
    const val CheckFat: String = "\uEBA6"
    const val Clock: String = "\uE19A"
    const val Cookie: String = "\uE6CA"
    const val DeviceMobile: String = "\uE1E0"
    const val Drop: String = "\uE210"
    const val Eye: String = "\uE220"
    const val Fire: String = "\uE242"
    const val FlowerLotus: String = "\uE6CC"
    const val Gear: String = "\uE270"
    const val HandTap: String = "\uEC90"
    const val Heartbeat: String = "\uE2AC"
    const val House: String = "\uE2C2"
    const val Info: String = "\uE2CE"
    const val Lightning: String = "\uE2DE"
    const val Moon: String = "\uE330"
    const val MoonStars: String = "\uE58E"
    const val Notebook: String = "\uE34E"
    const val Palette: String = "\uE6C8"
    const val PencilSimple: String = "\uE3B4"
    const val PersonSimpleWalk: String = "\uE73A"
    const val PiggyBank: String = "\uEA04"
    const val PlayCircle: String = "\uE3D2"
    const val PauseCircle: String = "\uE3A0"
    const val Plus: String = "\uE3D4"
    const val PlusSquare: String = "\uED4A"
    const val SealCheck: String = "\uE606"
    const val SneakerMove: String = "\uED60"
    const val Sparkle: String = "\uE6A2"
    const val SquaresFour: String = "\uE464"
    const val Sun: String = "\uE472"
    const val Target: String = "\uE47C"
    const val TextAlignLeft: String = "\uE484"
    const val Trash: String = "\uE4A6"
    const val TrendUp: String = "\uE4AE"
    const val Trophy: String = "\uE67E"
    const val User: String = "\uE4C2"
    const val UsersThree: String = "\uE68E"
    const val X: String = "\uE4F6"
    const val XCircle: String = "\uE4F8"
    const val Briefcase: String = "\uE0EE"
    const val GraduationCap: String = "\uE62C"
    const val Heart: String = "\uE2A8"
    const val BowlFood: String = "\uEAA4"
    const val Timer: String = "\uE492"
    const val CalendarBlank: String = "\uE10A"

    private val names: Map<String, String> = mapOf(
        "ph-arrow-clockwise" to ArrowClockwise,
        "ph-arrow-counter-clockwise" to ArrowCounterClockwise,
        "ph-arrow-left" to ArrowLeft,
        "ph-arrow-right" to ArrowRight,
        "ph-barbell" to Barbell,
        "ph-bell" to Bell,
        "ph-bell-ringing" to BellRinging,
        "ph-bell-slash" to BellSlash,
        "ph-book-open" to BookOpen,
        "ph-brain" to Brain,
        "ph-caret-down" to CaretDown,
        "ph-caret-left" to CaretLeft,
        "ph-caret-right" to CaretRight,
        "ph-chart-line-up" to ChartLineUp,
        "ph-check" to Check,
        "ph-check-circle" to CheckCircle,
        "ph-check-fat" to CheckFat,
        "ph-clock" to Clock,
        "ph-cookie" to Cookie,
        "ph-device-mobile" to DeviceMobile,
        "ph-drop" to Drop,
        "ph-eye" to Eye,
        "ph-fire" to Fire,
        "ph-flower-lotus" to FlowerLotus,
        "ph-gear" to Gear,
        "ph-hand-tap" to HandTap,
        "ph-heartbeat" to Heartbeat,
        "ph-house" to House,
        "ph-info" to Info,
        "ph-lightning" to Lightning,
        "ph-moon" to Moon,
        "ph-moon-stars" to MoonStars,
        "ph-notebook" to Notebook,
        "ph-palette" to Palette,
        "ph-pencil-simple" to PencilSimple,
        "ph-person-simple-walk" to PersonSimpleWalk,
        "ph-piggy-bank" to PiggyBank,
        "ph-play-circle" to PlayCircle,
        "ph-pause-circle" to PauseCircle,
        "ph-plus" to Plus,
        "ph-plus-square" to PlusSquare,
        "ph-seal-check" to SealCheck,
        "ph-sneaker-move" to SneakerMove,
        "ph-sparkle" to Sparkle,
        "ph-squares-four" to SquaresFour,
        "ph-sun" to Sun,
        "ph-target" to Target,
        "ph-text-align-left" to TextAlignLeft,
        "ph-trash" to Trash,
        "ph-trend-up" to TrendUp,
        "ph-trophy" to Trophy,
        "ph-user" to User,
        "ph-users-three" to UsersThree,
        "ph-x" to X,
        "ph-x-circle" to XCircle,
        "ph-briefcase" to Briefcase,
        "ph-graduation-cap" to GraduationCap,
        "ph-heart" to Heart,
        "ph-bowl-food" to BowlFood,
        "ph-timer" to Timer,
        "ph-calendar-blank" to CalendarBlank,
    )

    /** Glyph for a "ph-xxx" name, or null when the icon isn't bundled. */
    fun byName(name: String?): String? = name?.let { names[it] }
}
