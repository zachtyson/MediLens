package com.ztch.medilens_android_app.Camera
enum class ShapeValuesDC {
    ANY_SHAPE,
    BARREL,
    CAPSULE_OBLONG,
    CHARACTER_SHAPE,
    EGG_SHAPE,
    EIGHT_SIDED,
    OVAL,
    FIGURE_EIGHT_SHAPE,
    FIVE_SIDED,
    FOUR_SIDED,
    GEAR_SHAPE,
    HEART_SHAPE,
    KIDNEY_SHAPE,
    RECTANGLE,
    ROUND,
    SEVEN_SIDED,
    SIX_SIDED,
    THREE_SIDED,
    U_SHAPE
}


data class PillShapes(
    val shape: ShapeValuesDC
) {
    fun encodeShape(shape: ShapeValuesDC): Int {
        val shapeString = shape.toString()
        when(shapeString.lowercase()) {
            "any shape" -> return 0
            "barrel" -> return 1
            "capsule_oblong" -> return 5
            "character_shape" -> return 6
            "egg_shape" -> return 9
            "eight_sided" -> return 10
            "oval" -> return 11
            "figure_eight_shape" -> return 12
            "five_sided" -> return 13
            "four_sided" -> return 14
            "gear_shape" -> return 15
            "heart_shape" -> return 16
            "kidney_shape" -> return 18
            "rectangle" -> return 23
            "round" -> return 24
            "seven_sided" -> return 25
            "six_sided" -> return 27
            "three_sided" -> return 32
            "u_shape" -> return 33
            else -> return -1
        }
    }
}
