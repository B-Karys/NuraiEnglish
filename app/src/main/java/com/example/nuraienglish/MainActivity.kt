package com.example.nuraienglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.nuraienglish.core.data.preferences.AppPreferences
import com.example.nuraienglish.core.data.repository.AuthRepository
import com.example.nuraienglish.ui.navigation.AppNavGraph
import com.example.nuraienglish.ui.navigation.Screen
import com.example.nuraienglish.ui.theme.NuraiEnglishTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val language by appPreferences.language.collectAsState(
                initial = com.example.nuraienglish.core.data.model.AppLanguage.ENGLISH
            )
            NuraiEnglishTheme {
                val navController = rememberNavController()
                val startDestination = if (authRepository.isLoggedIn) Screen.Home.route
                                       else Screen.Login.route
                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    language = language
                )
            }
        }
    }
}
