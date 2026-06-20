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

object Routes {
    const val HOME = "home"
    const val CONVERSATIONS = "conversations"
    const val CONVERSATION_DETAIL = "conversation/{${ConversationDetailViewModel.ARG_CONVERSATION_KEY}}"

    fun conversationDetail(conversationKey: String) = "conversation/$conversationKey"
}

@Composable
fun SageXNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenConversations = { navController.navigate(Routes.CONVERSATIONS) },
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
            arguments = listOf(
                navArgument(ConversationDetailViewModel.ARG_CONVERSATION_KEY) {
                    type = NavType.StringType
                },
            ),
        ) {
            ConversationDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
