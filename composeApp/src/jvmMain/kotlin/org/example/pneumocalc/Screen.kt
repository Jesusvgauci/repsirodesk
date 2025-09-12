package org.example.pneumocalc

sealed class Screen {
    object Home : Screen()
    object Light : Screen()
    object ABG : Screen()
    object BODE : Screen()
    object CAT : Screen()
    object CURB65 : Screen()
    object Fleischner : Screen()
    object Geneva : Screen()
    object mMRC : Screen()
    object Steroid : Screen()
    object PSI : Screen()
    object PESI : Screen()
    object RESECT90 : Screen()
    object SPN : Screen()
    object Oxygenation : Screen()
    object STOPBANG : Screen()
    object TNM9 : Screen()
}
