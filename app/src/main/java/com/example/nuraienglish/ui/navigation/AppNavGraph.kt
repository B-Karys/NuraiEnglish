package com.example.nuraienglish.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nuraienglish.core.data.model.AppLanguage
import com.example.nuraienglish.feature.admin.AdminScreen
import com.example.nuraienglish.feature.auth.login.LoginScreen
import com.example.nuraienglish.feature.auth.register.RegisterScreen
import com.example.nuraienglish.feature.courses.CourseListScreen
import com.example.nuraienglish.feature.home.HomeScreen
import com.example.nuraienglish.feature.lesson.LessonListScreen
import com.example.nuraienglish.feature.progress.ProgressScreen
import com.example.nuraienglish.feature.review.ReviewScreen
import com.example.nuraienglish.feature.task.TaskScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                language = language,
                onNavigateToCourses = { navController.navigate(Screen.CourseList.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onNavigateToReview = { navController.navigate(Screen.Review.route) },
                onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CourseList.route) {
            CourseListScreen(
                language = language,
                onCourseClick = { courseId ->
                    navController.navigate(Screen.LessonList().createRoute(courseId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LessonList().route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStack ->
            val courseId = backStack.arguments?.getString("courseId") ?: return@composable
            LessonListScreen(
                courseId = courseId,
                language = language,
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.TaskSession().createRoute(courseId, lessonId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TaskSession().route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) { backStack ->
            val courseId = backStack.arguments?.getString("courseId") ?: return@composable
            val lessonId = backStack.arguments?.getString("lessonId") ?: return@composable
            TaskScreen(
                courseId = courseId,
                lessonId = lessonId,
                language = language,
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(
                language = language,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                language = language,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Admin.route) {
            AdminScreen(
                language = language,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
