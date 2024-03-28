package com.ztch.medilens_android_app.Camera
enum class ColorValuesDC {
    ANY_COLOR,
    WHITE,
    BEIGE,
    BLACK,
    BLUE,
    BROWN,
    CLEAR,
    GOLD,
    GRAY,
    GREEN,
    MAROON,
    ORANGE,
    PEACH,
    PINK,
    PURPLE,
    RED,
    TAN,
    YELLOW,
    BEIGE_AND_RED,
    BLACK_AND_GREEN,
    BLACK_AND_TEAL,
    BLACK_AND_YELLOW,
    BLUE_AND_BROWN,
    BLUE_AND_GRAY,
    BLUE_AND_GREEN,
    BLUE_AND_ORANGE,
    BLUE_AND_PEACH,
    BLUE_AND_PINK,
    BLUE_AND_WHITE,
    BLUE_AND_WHITE_SPECKS,
    BLUE_AND_YELLOW,
    BROWN_AND_CLEAR,
    BROWN_AND_ORANGE,
    BROWN_AND_PEACH,
    BROWN_AND_RED,
    BROWN_AND_WHITE,
    BROWN_AND_YELLOW,
    CLEAR_AND_GREEN,
    DARK_AND_LIGHT_GREEN,
    GOLD_AND_WHITE,
    GRAY_AND_PEACH,
    GRAY_AND_PINK,
    GRAY_AND_RED,
    GRAY_AND_WHITE,
    GRAY_AND_YELLOW,
    GREEN_AND_ORANGE,
    GREEN_AND_PEACH,
    GREEN_AND_PINK,
    GREEN_AND_PURPLE,
    GREEN_AND_TURQUOISE,
    GREEN_AND_WHITE,
    GREEN_AND_YELLOW,
    LAVENDER_AND_WHITE,
    MAROON_AND_PINK,
    ORANGE_AND_TURQUOISE,
    ORANGE_AND_WHITE,
    ORANGE_AND_YELLOW,
    PEACH_AND_PURPLE,
    PEACH_AND_RED,
    PEACH_AND_WHITE,
    PINK_AND_PURPLE,
    PINK_AND_RED_SPECKS,
    PINK_AND_TURQUOISE,
    PINK_AND_WHITE,
    PINK_AND_YELLOW,
    RED_AND_TURQUOISE,
    RED_AND_WHITE,
    RED_AND_YELLOW,
    TAN_AND_WHITE,
    TURQUOISE_AND_WHITE,
    TURQUOISE_AND_YELLOW,
    WHITE_AND_BLUE_SPECKS,
    WHITE_AND_RED_SPECKS,
    WHITE_AND_YELLOW,
    YELLOW_AND_GRAY,
    YELLOW_AND_WHITE
}
data class PillColors(
    val color: ColorValuesDC
) {
    fun encodeColor(color: ColorValuesDC): Int {
        val colorString = color.toString()
        when(colorString.lowercase()) {
            "any color" -> return -1
            "white" -> return 12
            "beige" -> return 14
            "black" -> return 73
            "blue" -> return 1
            "brown" -> return 2
            "clear" -> return 3
            "gold" -> return 4
            "gray" -> return 5
            "grey" -> return 5
            "green" -> return 6
            "maroon" -> return 44
            "orange" -> return 7
            "peach" -> return 74
            "pink" -> return 8
            "purple" -> return 9
            "red" -> return 10
            "tan" -> return 11
            "yellow" -> return 13
            "beige & red" -> return 69
            "black & green" -> return 55
            "black & teal" -> return 70
            "black & yellow" -> return 48
            "blue & brown" -> return 52
            "blue & gray" -> return 45
            "blue & green" -> return 75
            "blue & orange" -> return 71
            "blue & peach" -> return 53
            "blue & pink" -> return 34
            "blue & white" -> return 19
            "blue & white specks" -> return 26
            "blue & yellow" -> return 21
            "brown & clear" -> return 47
            "brown & orange" -> return 54
            "brown & peach" -> return 28
            "brown & red" -> return 16
            "brown & white" -> return 57
            "brown & yellow" -> return 27
            "clear & green" -> return 49
            "dark & light green" -> return 46
            "gold & white" -> return 51
            "gray & peach" -> return 61
            "gray & pink" -> return 39
            "gray & red" -> return 58
            "gray & white" -> return 67
            "gray & yellow" -> return 68
            "green & orange" -> return 65
            "green & peach" -> return 63
            "green & pink" -> return 56
            "green & purple" -> return 43
            "green & turquoise" -> return 62
            "green & white" -> return 30
            "green & yellow" -> return 22
            "lavender & white" -> return 42
            "maroon & pink" -> return 40
            "orange & turquoise" -> return 50
            "orange & white" -> return 64
            "orange & yellow" -> return 23
            "peach & purple" -> return 60
            "peach & red" -> return 66
            "peach & white" -> return 18
            "pink & purple" -> return 15
            "pink & red specks" -> return 37
            "pink & turquoise" -> return 29
            "pink & white" -> return 25
            "pink & yellow" -> return 72
            "red & turquoise" -> return 17
            "red & white" -> return 35
            "red & yellow" -> return 20
            "tan & white" -> return 33
            "turquoise & white" -> return 59
            "turquoise & yellow" -> return 24
            "white & blue specks" -> return 32
            "white & red specks" -> return 41
            "white & yellow" -> return 38
            "yellow & gray" -> return 31
            "yellow & white" -> return 36
            else -> return -1
        }
    }
}
