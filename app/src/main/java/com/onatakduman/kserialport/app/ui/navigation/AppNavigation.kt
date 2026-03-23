package com.onatakduman.kserialport.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.onatakduman.kserialport.app.ui.screens.about.AboutScreen
import com.onatakduman.kserialport.app.ui.screens.home.HomeScreen
import com.onatakduman.kserialport.app.ui.screens.recordings.RecordingsScreen
import com.onatakduman.kserialport.app.ui.screens.settings.SettingsScreen
import com.onatakduman.kserialport.app.ui.screens.terminal.TerminalScreen
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    serialViewModel: SerialViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(250)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(250)
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                serialViewModel = serialViewModel,
                onNavigateToTerminal = {
                    navController.navigate(Screen.Terminal.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Terminal.route) {
            TerminalScreen(serialViewModel = serialViewModel)
        }

        composable(Screen.Recordings.route) {
            RecordingsScreen(serialViewModel = serialViewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                serialViewModel = serialViewModel,
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
