package com.dramallama.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dramallama.app.data.repository.TeamRepository
import com.dramallama.app.ui.screens.MemberDetailScreen
import com.dramallama.app.ui.screens.TeamListScreen
import com.dramallama.app.ui.viewmodel.MemberDetailViewModel
import com.dramallama.app.ui.viewmodel.TeamListViewModel

sealed class Screen(val route: String) {
    data object TeamList : Screen("team_list")
    data object MemberDetail : Screen("member_detail/{memberId}") {
        fun createRoute(memberId: Long) = "member_detail/$memberId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: TeamRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TeamList.route
    ) {
        composable(Screen.TeamList.route) {
            val viewModel: TeamListViewModel = viewModel(
                factory = TeamListViewModel.Factory(repository)
            )
            TeamListScreen(
                viewModel = viewModel,
                onMemberClick = { memberId ->
                    navController.navigate(Screen.MemberDetail.createRoute(memberId))
                }
            )
        }
        
        composable(
            route = Screen.MemberDetail.route,
            arguments = listOf(
                navArgument("memberId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getLong("memberId") ?: return@composable
            val viewModel: MemberDetailViewModel = viewModel(
                factory = MemberDetailViewModel.Factory(repository, memberId)
            )
            MemberDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

