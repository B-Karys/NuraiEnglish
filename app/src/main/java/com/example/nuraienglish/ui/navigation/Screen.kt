package com.example.nuraienglish.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object VerifyEmail : Screen("verify_email")
    data object Home : Screen("home")
    data object CourseList : Screen("courses")
    data object Progress : Screen("progress")
    data object Review : Screen("review")
    data object Admin : Screen("admin")

    data class LessonList(val courseId: String = "{courseId}") :
        Screen("lessons/{courseId}") {
        fun createRoute(courseId: String) = "lessons/$courseId"
    }

    data class TaskSession(val courseId: String = "{courseId}", val lessonId: String = "{lessonId}") :
        Screen("tasks/{courseId}/{lessonId}") {
        fun createRoute(courseId: String, lessonId: String) = "tasks/$courseId/$lessonId"
    }
}
