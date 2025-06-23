package net.acrobits.interview.test.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.acrobits.interview.AcrobitsApp
import net.acrobits.interview.test.presentation.ui.call.CallScreen
import net.acrobits.interview.test.presentation.ui.call.CallViewModel
import net.acrobits.interview.test.presentation.ui.dialer.DialerScreen
import net.acrobits.interview.test.presentation.ui.dialer.DialerViewModel
import net.acrobits.interview.test.presentation.ui.welcome.WelcomeScreen

/**
 * Navigation logic for 3 screens: welcome screen with the permission check,
 * dialer to enter number to dial to and a call screen with the ability to hangup, mute and hold
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.Dialer.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dialer.route) {
            val dialerViewModel: DialerViewModel = viewModel(
                factory = DialerViewModel.provideFactory(repository = AcrobitsApp.appModule.sipRepository)
            )
            DialerScreen(
                viewModel = dialerViewModel,
                onNavigateToCall = {
                    navController.navigate(Screen.Call.route)
                }
            )
        }
        composable(Screen.Call.route) {
            val callViewModel: CallViewModel = viewModel(
                factory = CallViewModel.provideFactory(repository = AcrobitsApp.appModule.sipRepository)
            )
            CallScreen(
                viewModel = callViewModel,
                onCallEnded = {
                    navController.popBackStack()
                }
            )
        }
    }
}