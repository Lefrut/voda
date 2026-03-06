package com.m.vodovoz.feature.main

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.m.vodovoz.feature.catalog.CatalogEntry
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteEntry
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeEntry
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileEntry

@Composable
fun BottmNav(
    homeViewModel: HomeFlowViewModel,
    catalogFlowViewModel: CatalogFlowViewModel,
    favoriteFlowViewModel: FavoriteFlowViewModel
) {

    val navigationState = rememberNavigationState(
        startKey = BottomNavKey.Home,
        topLevelKeys = BottomNavKey.values
    )

    val navigator = remember { Navigator(navigationState) }


    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavKey.values.forEach { key ->
                    NavigationBarItem(
                        selected = key == navigationState.currentKey,
                        label = {
                            Text(text = key.title)
                        },
                        icon = {

                        },
                        onClick = {
                            navigator.navigate(key)
                        }
                    )
                }
            }
        }
    ) { padding ->
        padding

        val entryProvider = entryProvider<NavKey> {
            entry<BottomNavKey.Home> {
                HomeEntry(homeViewModel)
            }
            entry<BottomNavKey.Profile> {

            }
            entry<BottomNavKey.Cart> {

            }
            entry<BottomNavKey.Catalog> {
                CatalogEntry(catalogFlowViewModel)
            }
            entry<BottomNavKey.Favorites> {
                FavoriteEntry(favoriteFlowViewModel)
            }
        }
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            sceneStrategy = SinglePaneSceneStrategy()
        )
    }
}

@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val topLevelStack = rememberNavBackStack(startKey)
    val subStacks = topLevelKeys.associateWith { key ->
        rememberNavBackStack(key)
    }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}

class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

    val topLevelKeys
        get() = subStacks.keys

    @get:VisibleForTesting
    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey]
            ?: error("Sub stack for $currentTopLevelKey does not exist")

    @get:VisibleForTesting
    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            //todo
            //rememberViewModelStoreNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    return topLevelStack
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}

class Navigator(val state: NavigationState) {

    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    fun goBack() {
        when (state.currentKey) {
            state.startKey -> error("You cannot go back from the start route")
            state.currentTopLevelKey -> {
                // We're at the base of the current sub stack, go back to the previous top level
                // stack.
                state.topLevelStack.removeLastOrNull()
            }

            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            // Remove it if it's already in the stack so it's added at the end.
            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}

@Composable
fun AppNav() {

    var currentBottomKey: BottomNavKey by rememberSaveable {
        mutableStateOf(BottomNavKey.Home)
    }

    var showBottomBar: Boolean by rememberSaveable() {
        mutableStateOf(false)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavKey.values.forEach { key ->
                    NavigationBarItem(
                        selected = key == currentBottomKey,
                        label = {
                            Text(text = key.title)
                        },
                        icon = {

                        },
                        onClick = {
                            currentBottomKey = key
                        }
                    )
                }
            }
        }
    ) { padding ->
        padding
    }
}


@Stable
sealed interface BottomNavKey : NavKey {

    val title: String

    data object Home : BottomNavKey {
        override val title: String
            get() = "Дом"


    }

    data object Catalog : BottomNavKey {
        override val title: String
            get() = "Каталог"
    }


    data object Cart : BottomNavKey {
        override val title: String
            get() = "Корзина"
    }


    data object Favorites : BottomNavKey {
        override val title: String
            get() = "Избранные"
    }

    data object Profile : BottomNavKey {
        override val title: String
            get() = "Профиль"
    }


    companion object {
        val values
            get() = setOf(Home, Catalog, Cart, Favorites, Profile)

    }

}
