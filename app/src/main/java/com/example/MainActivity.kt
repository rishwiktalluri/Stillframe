package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.drafts.DraftsScreen
import com.example.ui.screens.review.ReviewScreen
import com.example.ui.screens.universe.UniverseScreen
import com.example.ui.screens.world.WorldScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.StillframeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: StillframeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activeProject by viewModel.activeProject.collectAsState()
            val currentDestination by viewModel.currentDestination.collectAsState()
            val isFocusMode by viewModel.isFocusMode.collectAsState()
            val activeScene by viewModel.activeScene.collectAsState()

            val accentColor = remember(activeProject?.accentColorHex) {
                try {
                    Color(android.graphics.Color.parseColor(activeProject?.accentColorHex ?: "#E8B058"))
                } catch (e: Exception) {
                    AccentAmber
                }
            }

            // Handle back presses gracefully
            BackHandler(enabled = isFocusMode || activeScene != null || currentDestination != AppDestination.UNIVERSE) {
                when {
                    isFocusMode -> viewModel.setFocusMode(false)
                    activeScene != null && currentDestination == AppDestination.DRAFTS -> viewModel.setActiveScene(null)
                    currentDestination != AppDestination.UNIVERSE -> viewModel.setDestination(AppDestination.UNIVERSE)
                }
            }

            StillframeTheme(accentColor = accentColor) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = StillframeBlack,
                    bottomBar = {
                        // Hide bottom navigation in Focus Mode for distraction-free writing
                        if (!isFocusMode) {
                            StillframeBottomBar(
                                currentDestination = currentDestination,
                                onNavigate = { viewModel.setDestination(it) },
                                accentColor = accentColor
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(StillframeBlack)
                    ) {
                        when (currentDestination) {
                            AppDestination.UNIVERSE -> {
                                UniverseScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            AppDestination.WORLD -> {
                                WorldScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            AppDestination.DRAFTS -> {
                                DraftsScreen(
                                    viewModel = viewModel,
                                    modifier = if (isFocusMode) Modifier else Modifier.padding(innerPadding)
                                )
                            }
                            AppDestination.REVIEW -> {
                                ReviewScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StillframeBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    accentColor: Color
) {
    Surface(
        color = StillframeSurface0,
        modifier = Modifier
            .fillMaxWidth()
            .border(androidx.compose.foundation.BorderStroke(1.dp, StillframeBorder))
    ) {
        NavigationBar(
            containerColor = StillframeSurface0,
            contentColor = TextPrimary,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = currentDestination == AppDestination.UNIVERSE,
                onClick = { onNavigate(AppDestination.UNIVERSE) },
                icon = {
                    Icon(
                        if (currentDestination == AppDestination.UNIVERSE) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                        contentDescription = "Universe Archive"
                    )
                },
                label = { Text("Universe", style = StillframeTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StillframeBlack,
                    indicatorColor = accentColor,
                    selectedTextColor = accentColor,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("nav_universe")
            )

            NavigationBarItem(
                selected = currentDestination == AppDestination.WORLD,
                onClick = { onNavigate(AppDestination.WORLD) },
                icon = {
                    Icon(
                        if (currentDestination == AppDestination.WORLD) Icons.Filled.Public else Icons.Outlined.Public,
                        contentDescription = "World Bible"
                    )
                },
                label = { Text("World", style = StillframeTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StillframeBlack,
                    indicatorColor = accentColor,
                    selectedTextColor = accentColor,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("nav_world")
            )

            NavigationBarItem(
                selected = currentDestination == AppDestination.DRAFTS,
                onClick = { onNavigate(AppDestination.DRAFTS) },
                icon = {
                    Icon(
                        if (currentDestination == AppDestination.DRAFTS) Icons.Filled.DriveFileRenameOutline else Icons.Outlined.DriveFileRenameOutline,
                        contentDescription = "Drafts & Scripts"
                    )
                },
                label = { Text("Drafts", style = StillframeTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StillframeBlack,
                    indicatorColor = accentColor,
                    selectedTextColor = accentColor,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("nav_drafts")
            )

            NavigationBarItem(
                selected = currentDestination == AppDestination.REVIEW,
                onClick = { onNavigate(AppDestination.REVIEW) },
                icon = {
                    Icon(
                        if (currentDestination == AppDestination.REVIEW) Icons.Filled.RateReview else Icons.Outlined.RateReview,
                        contentDescription = "Editorial Review"
                    )
                },
                label = { Text("Review", style = StillframeTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StillframeBlack,
                    indicatorColor = accentColor,
                    selectedTextColor = accentColor,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("nav_review")
            )
        }
    }
}
