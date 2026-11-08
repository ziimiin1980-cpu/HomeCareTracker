package com.example.homecaretracker.navigation

object MaintenanceRoutes {
    const val CREATE = "maintenance/new"
    const val DETAIL = "maintenance/{taskId}"
    const val EDIT = "maintenance/{taskId}/edit"

    fun detail(taskId: Long): String = "maintenance/$taskId"

    fun edit(taskId: Long): String = "maintenance/$taskId/edit"
}
