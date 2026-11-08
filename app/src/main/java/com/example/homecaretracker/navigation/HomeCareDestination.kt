package com.example.homecaretracker.navigation

enum class HomeCareDestination(
    val route: String,
    val label: String,
    val icon: String
) {
    HOME("home", "Главная", "⌂"),
    APPLIANCES("appliances", "Моя техника", "▤"),
    MAINTENANCE("maintenance", "Обслуживание", "✓"),
    HISTORY("history", "История", "◷")
}
