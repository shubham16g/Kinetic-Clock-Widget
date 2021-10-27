package com.shubhamgupta16.advancedwidget

class ClockData {
    companion object {
        private val hidden_____ = floatArrayOf(315f, 315f)
        private val bottomRight = floatArrayOf(0f, 90f)
        private val bottomLeft_ = floatArrayOf(0f, 270f)
        private val horizontal_ = floatArrayOf(270f, 90f)
        private val vertical___ = floatArrayOf(0f, 180f)
        private val topRight___ = floatArrayOf(90f, 180f)
        private val topLeft____ = floatArrayOf(270f, 180f)

        private val topLSide___ = floatArrayOf(315f, 180f)
        private val topRSide___ = floatArrayOf(45f, 180f)
        private val bottomLSide = floatArrayOf(225f, 0f)
        private val bottomRSide = floatArrayOf(135f, 0f)

        private val sideBottomR = floatArrayOf(90f, 315f)
        private val sideTopL___ = floatArrayOf(270f, 135f)

        private val s0 = arrayOf(
            bottomRight, bottomLeft_,
            horizontal_, horizontal_,
            topRight___, topLSide___,
        )

        private val m0 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            vertical___, vertical___, vertical___, vertical___,
            vertical___, vertical___, vertical___, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m1 = arrayOf(
            bottomRight, horizontal_, bottomLeft_, hidden_____,
            topRight___, bottomLeft_, vertical___, hidden_____,
            hidden_____, vertical___, vertical___, hidden_____,
            hidden_____, vertical___, vertical___, hidden_____,
            bottomRight, topLeft____, topRight___, bottomLeft_,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m2 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, sideBottomR, topLeft____, topLSide___,
            bottomRSide, bottomRight, sideTopL___, hidden_____,
            vertical___, topRight___, horizontal_, bottomLeft_,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m3 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, bottomRight, topLeft____, topLSide___,
            hidden_____, topRight___, bottomLeft_, bottomLSide,
            bottomRight, horizontal_, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m4 = arrayOf(
            bottomRight, bottomLeft_, bottomRight, bottomLeft_,
            vertical___, vertical___, vertical___, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, hidden_____, vertical___, vertical___,
            hidden_____, hidden_____, topRight___, topLeft____,
        )

        private val m5 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, horizontal_, topLeft____,
            vertical___, topRight___, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            bottomRight, horizontal_, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m6 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, horizontal_, topLeft____,
            vertical___, topRight___, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m7 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            topRight___, horizontal_, bottomLeft_, vertical___,
            hidden_____, hidden_____, topLSide___, topLSide___,
            hidden_____, bottomRSide, bottomRSide, hidden_____,
            hidden_____, vertical___, vertical___, hidden_____,
            hidden_____, topRight___, topLeft____, hidden_____,
        )
        private val m8 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            topRSide___, topRight___, topLeft____, topLSide___,
            bottomRSide, bottomRight, bottomLeft_, bottomLSide,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        private val m9 = arrayOf(
            bottomRight, horizontal_, horizontal_, bottomLeft_,
            vertical___, bottomRight, bottomLeft_, vertical___,
            vertical___, topRight___, topLeft____, vertical___,
            topRight___, horizontal_, bottomLeft_, vertical___,
            bottomRight, horizontal_, topLeft____, vertical___,
            topRight___, horizontal_, horizontal_, topLeft____,
        )
        fun getLargeArray(num: Int): Array<FloatArray> {
            return when (num) {
                0 -> m0
                1 -> m1
                2 -> m2
                3 -> m3
                4 -> m4
                5 -> m5
                6 -> m6
                7 -> m7
                8 -> m8
                9 -> m9
                else -> m0
            }
        }
    }
//    class system started here


}