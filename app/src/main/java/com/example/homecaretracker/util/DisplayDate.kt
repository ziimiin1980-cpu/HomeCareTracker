package com.example.homecaretracker.util

import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

fun formatUtcDate(timestamp: Long): String = timestamp.toUtcDate().format(dateFormatter)
