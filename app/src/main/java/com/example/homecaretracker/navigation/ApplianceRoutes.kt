package com.example.homecaretracker.navigation

object ApplianceRoutes {
    const val CREATE = "appliances/new"
    const val DETAIL = "appliances/{applianceId}"
    const val EDIT = "appliances/{applianceId}/edit"

    fun detail(applianceId: Long): String = "appliances/$applianceId"

    fun edit(applianceId: Long): String = "appliances/$applianceId/edit"
}
