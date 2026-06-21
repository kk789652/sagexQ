package com.tiswu.sagex.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tiswu.sagex.ui.about.AboutScreen
import com.tiswu.sagex.ui.conversations.ConversationDetailScreen
import com.tiswu.sagex.ui.conversations.ConversationDetailViewModel
import com.tiswu.sagex.ui.conversations.ConversationListScreen
import com.tiswu.sagex.ui.conversations.ConversationListViewModel
import com.tiswu.sagex.ui.capture.CaptureSettingsScreen
import com.tiswu.sagex.ui.home.HomeScreen
import com.tiswu.sagex.ui.prompt.PromptConfigScreen
import com.tiswu.sagex.ui.settings.SettingsScreen
import com.tiswu.sagex.ui.summary.SummaryScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PROMPT_CONFIG = "prompt-config"
    const val CAPTURE_SETTINGS = "capture-settings"
    const val ABOUT = "about"
    private const val SOURCE_PACKAGE = ConversationListViewModel.ARG_SOURCE_PACKAGE
    private const val KEY = ConversationDetailViewModel.ARG_CONVERSATION_KEY
    const val CONVERSATIONS = "conversations/{$SOURCE_PACKAGE}"
    const val CONVERSATION_DETAIL = "conversation/{$KEY}"
    const val SUMMARY = "summary/{$KEY}"

    fun conversations(sourcePackage: String) = "conversations/${Uri.encode(sourcePackage)}"
    fun conversationDetail(conversationKey: String) = "conversation/$conversationKey"
    fun summary(conversationKey: String) = "summary/$conversationKey"
}

@Composable
fun SageXNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenApp = { sourcePackage -> navController.navigate(Routes.conversations(sourcePackage)) },
                onOpenCaptureSettings = { navController.navigate(Routes.CAPTURE_SETTINGS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenPromptConfig = { navController.navigate(Routes.PROMPT_CONFIG) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onSyncFeishu = { },
            )
        }
        composable(
            route = Routes.CONVERSATIONS,
            arguments = listOf(navArgument(ConversationListViewModel.ARG_SOURCE_PACKAGE) { type = NavType.StringType }),
        ) {
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
        composable(Routes.PROMPT_CONFIG) {
            PromptConfigScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CAPTURE_SETTINGS) {
            CaptureSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
