package com.tiswu.sagex.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tiswu.sagex.ui.conversations.ConversationDetailScreen
import com.tiswu.sagex.ui.conversations.ConversationDetailViewModel
import com.tiswu.sagex.ui.conversations.ConversationListScreen
import com.tiswu.sagex.ui.home.HomeScreen
import com.tiswu.sagex.ui.settings.SettingsScreen
import com.tiswu.sagex.ui.summary.SummaryScreen

object Routes {
    const val HOME = "home"
    const val CONVERSATIONS = "conversations"
    const val SETTINGS = "settings"
    private const val KEY = ConversationDetailViewModel.ARG_CONVERSATION_KEY
    const val CONVERSATION_DETAIL = "conversation/{$KEY}"
    const val SUMMARY = "summary/{$KEY}"

    fun conversationDetail(conversationKey: String) = "conversation/$conversationKey"
    fun summary(conversationKey: String) = "summary/$conversationKey"
}

@Composable
fun SageXNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenConversations = { navController.navigate(Routes.CONVERSATIONS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.CONVERSATIONS) {
            ConversationListScreen(
                onOpenConversation = { key -> navController.navigate(Routes.conversationDetail(key)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.CONVERSATION_DETAIL,
            arguments = listOf(navArgument(ConversationDetailViewModel.ARG_CONVERSATION_KEY) { type = NavType.StringType }),
        ) { entry ->
            val key = entry.arguments?.getString(ConversationDetailViewModel.ARG_CONVERSATION_KEY).orEmpty()
            ConversationDetailScreen(
                onBack = { navController.popBackStack() },
                onSummarize = { navController.navigate(Routes.summary(key)) },
            )
        }
        composable(
            route = Routes.SUMMARY,
            arguments = listOf(navArgument(ConversationDetailViewModel.ARG_CONVERSATION_KEY) { type = NavType.StringType }),
        ) {
            SummaryScreen(
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
