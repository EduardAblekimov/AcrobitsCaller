package net.acrobits.interview.test.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Dialer : Screen("dialer")
    object Call : Screen("call")
}