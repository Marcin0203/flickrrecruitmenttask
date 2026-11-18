package pl.marcin.malocha.flickrrecruitmenttask.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail.PhotoDetailScreen
import pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.photodetail.PhotoDetailViewModel
import pl.marcin.malocha.flickrrecruitmenttask.presentation.screen.search.SearchScreen

/** The app's navigation graph: [SearchRoute] → [PhotoDetailRoute]. */
@Composable
fun FlickrNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = SearchRoute) {
        composable<SearchRoute> {
            SearchScreen(
                onNavigateToPhoto = { photo ->
                    navController.navigate(PhotoDetailRoute(imageUrl = photo.fullSizeUrl, title = photo.title))
                },
            )
        }
        composable<PhotoDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PhotoDetailRoute>()
            PhotoDetailScreen(
                viewModel = hiltViewModel<PhotoDetailViewModel, PhotoDetailViewModel.Factory>(
                    creationCallback = { factory -> factory.create(route) },
                ),
                onNavigateBack = navController::navigateUp,
            )
        }
    }
}
