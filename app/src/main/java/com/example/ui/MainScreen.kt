package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.model.SchoolLoad
import com.example.ui.theme.ElectricVolt
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.viewmodel.CourtIqViewModel
import com.example.viewmodel.UiState

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_WORKOUTS = "workouts"
const val ROUTE_SHOT_LAB = "shot_lab"
const val ROUTE_COACH = "coach"
const val ROUTE_PLAYER_DNA = "player_dna"

private data class MainTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val mainTabs = listOf(
    MainTab(
        route = ROUTE_DASHBOARD,
        labelRes = R.string.nav_dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    MainTab(
        route = ROUTE_WORKOUTS,
        labelRes = R.string.nav_workouts,
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter,
        testTag = "nav_workouts"
    ),
    MainTab(
        route = ROUTE_SHOT_LAB,
        labelRes = R.string.nav_shot_lab,
        selectedIcon = Icons.Filled.SportsBasketball,
        unselectedIcon = Icons.Outlined.SportsBasketball,
        testTag = "nav_shot_lab"
    ),
    MainTab(
        route = ROUTE_COACH,
        labelRes = R.string.nav_coach,
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology,
        testTag = "nav_coach"
    ),
)

@Composable
fun MainScreen(
    viewModel: CourtIqViewModel,
    userEmail: String?,
    onSignOut: () -> Unit,
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val selectedZoneId by viewModel.selectedZoneId.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val aiStreamState by viewModel.aiStreamState.collectAsStateWithLifecycle()
    val focusPlanState by viewModel.focusPlanState.collectAsStateWithLifecycle()

    var selectedCoachSubTab by remember { mutableStateOf(CoachSubTab.AI_COACH) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_screen_scaffold"),
            containerColor = ObsidianBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onSelectRoute = navigateToRoute
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    MainNavigationRail(
                        currentRoute = currentRoute,
                        onSelectRoute = navigateToRoute
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    when (val state = dashboardState) {
                        is UiState.Loading -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = ElectricVolt)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Loading Court IQ...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        is UiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Could not sync training data",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = onSignOut) {
                                    Text("Switch Account / Sign Out")
                                }
                            }
                        }

                        is UiState.Success -> {
                            val bundle = state.data
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_DASHBOARD,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(ROUTE_DASHBOARD) {
                                    Dashboard(
                                        bundle = bundle,
                                        onStartWorkout = { program, dayId ->
                                            viewModel.startWorkout(program, dayId)
                                        },
                                        onToggleScheduleComplete = { day ->
                                            viewModel.updateScheduleDay(
                                                day = day,
                                                focusTitle = day.focusTitle,
                                                durationMinutes = day.durationMinutes,
                                                schoolLoad = SchoolLoad.safeValueOf(day.schoolLoad),
                                                completedThisWeek = !day.completedThisWeek
                                            )
                                        },
                                        onOpenMindset = {
                                            selectedCoachSubTab = CoachSubTab.MINDSET
                                            navigateToRoute(ROUTE_COACH)
                                        },
                                        onOpenShotLab = { navigateToRoute(ROUTE_SHOT_LAB) },
                                        onOpenPlayerDna = { navigateToRoute(ROUTE_PLAYER_DNA) }
                                    )
                                }
                                composable(ROUTE_WORKOUTS) {
                                    TrainScreen(
                                        bundle = bundle,
                                        onStartWorkout = { program ->
                                            viewModel.startWorkout(program, null)
                                        },
                                        onOpenShotLab = { navigateToRoute(ROUTE_SHOT_LAB) },
                                        onOpenPlayerDna = { navigateToRoute(ROUTE_PLAYER_DNA) }
                                    )
                                }
                                composable(ROUTE_SHOT_LAB) {
                                    ShotLabScreen(
                                        bundle = bundle,
                                        selectedZoneId = selectedZoneId,
                                        onSelectZone = { viewModel.selectCourtZone(it) },
                                        onRecordShot = { zone, made -> viewModel.recordShot(zone, made) },
                                        onRecordBatch = { zone, makes, attempts ->
                                            viewModel.recordBatchShots(zone, makes, attempts)
                                        },
                                        onResetSession = { viewModel.resetShotSession() },
                                        onOpenPlayerDna = { navigateToRoute(ROUTE_PLAYER_DNA) }
                                    )
                                }
                                composable(ROUTE_PLAYER_DNA) {
                                    PlayerDnaProfileScreen(
                                        bundle = bundle,
                                        onBack = { navController.popBackStack() },
                                        onOpenShotLab = { navigateToRoute(ROUTE_SHOT_LAB) },
                                        onStartRecommendedDrill = { navigateToRoute(ROUTE_WORKOUTS) }
                                    )
                                }
                                composable(ROUTE_COACH) {
                                    CoachHubScreen(
                                        selectedSubTab = selectedCoachSubTab,
                                        onSelectSubTab = { selectedCoachSubTab = it },
                                        bundle = bundle,
                                        userEmail = userEmail,
                                        chatMessages = chatMessages,
                                        aiStreamState = aiStreamState,
                                        focusPlanState = focusPlanState,
                                        onIncrementMission = { viewModel.incrementMission(it) },
                                        onToggleMission = { viewModel.toggleMission(it) },
                                        onAddCustomMission = { title, cat, target ->
                                            viewModel.addCustomMission(title, cat, target)
                                        },
                                        onResetWeeklyMissions = { viewModel.resetWeeklyMissions() },
                                        onAddObservation = { role, tag, note ->
                                            viewModel.addObservation(role, tag, note)
                                        },
                                        onDeleteObservation = { viewModel.deleteObservation(it) },
                                        onAskCoachIq = { viewModel.askCoachIq(it) },
                                        onGenerateFocusPlan = { viewModel.generatePersonalizedFocusPlan() },
                                        onSaveCalibration = { name, focus, goal, affirm, fatigue, lvl, ready, sh, bh, fin, def, cv, conf, lead, wh ->
                                            viewModel.saveCoachCalibration(
                                                name, focus, goal, affirm, fatigue, lvl, ready,
                                                sh, bh, fin, def, cv, conf, lead, wh
                                            )
                                        },
                                        onUpdateScheduleDay = { day, title, mins, load, done ->
                                            viewModel.updateScheduleDay(day, title, mins, load, done)
                                        },
                                        onSignOut = onSignOut
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = ObsidianSurface,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier.testTag("main_bottom_navigation_bar")
    ) {
        mainTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectRoute(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = stringResource(tab.labelRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ObsidianBg,
                    selectedTextColor = ElectricVolt,
                    indicatorColor = ElectricVolt,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}

@Composable
private fun MainNavigationRail(
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
) {
    NavigationRail(
        containerColor = ObsidianSurface,
        modifier = Modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Spacer(Modifier.height(12.dp))
        mainTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectRoute(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = stringResource(tab.labelRes)
                    )
                },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = ObsidianBg,
                    selectedTextColor = ElectricVolt,
                    indicatorColor = ElectricVolt,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
