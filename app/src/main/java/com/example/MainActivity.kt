package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.SessionManager
import com.example.data.repository.InfraRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.components.CyberLoader
import com.example.ui.viewmodel.InfraViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var infraRepository: InfraRepository
    private lateinit var viewModel: InfraViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup state framework
        sessionManager = SessionManager(applicationContext)
        infraRepository = InfraRepository(sessionManager)
        viewModel = InfraViewModel(infraRepository, sessionManager)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val token by viewModel.token.collectAsState()
                val isDemoMode by viewModel.demoMode.collectAsState()
                val toastMsg by viewModel.toastMessage.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val userRole by viewModel.userRole.collectAsState()
                val lockdownActive by viewModel.lockdownActive.collectAsState()

                // Active Tab Control
                var activeTab by remember { mutableStateOf(0) } // 0:Dash, 1:Nodes, 2:Conts, 3:CF, 4:Emergency
                var showSpecialLogsView by remember { mutableStateOf(false) }
                var showSpecialSettingsView by remember { mutableStateOf(false) }
                var showLoginOverlay by remember { mutableStateOf(false) }

                // Automatically trigger config/verification overlay if unlinked / logged out
                LaunchedEffect(token) {
                    if (token == null) {
                        showLoginOverlay = true
                    }
                }

                // Dynamic Toast system handler
                LaunchedEffect(toastMsg) {
                    toastMsg?.let {
                        Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Mainframe Secure Console Environment with high-density header
                    Scaffold(
                    topBar = {
                        Surface(
                            color = ThemeBackground,
                            modifier = Modifier.statusBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "BUILDOS . INFRA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = ThemeOnSurface,
                                        letterSpacing = 2.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isDemoMode) "Demo Sandbox" else "Fleet Dashboard",
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ThemeOnBackground
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF10B981)) // bg-emerald-500 status dot
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (token == null) {
                                        Button(
                                            onClick = { showLoginOverlay = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ThemePrimary.copy(alpha = 0.15f),
                                                contentColor = ThemePrimary
                                            ),
                                            border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Login,
                                                contentDescription = "Guest Login",
                                                modifier = Modifier.size(14.dp),
                                                tint = ThemePrimary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "LOGIN",
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { viewModel.logout {} },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Logout,
                                                contentDescription = "Log Out",
                                                tint = ThemeError,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Terminal toggle
                                    IconButton(
                                        onClick = {
                                            showSpecialLogsView = !showSpecialLogsView
                                            showSpecialSettingsView = false
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Terminal,
                                            contentDescription = "Terminal",
                                            tint = if (showSpecialLogsView) ThemePrimary else ThemeOnSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Settings toggle
                                    IconButton(
                                        onClick = {
                                            showSpecialSettingsView = !showSpecialSettingsView
                                            showSpecialLogsView = false
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = if (showSpecialSettingsView) ThemePrimary else ThemeOnSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Emergency button:
                                    Button(
                                        onClick = {
                                            activeTab = 4 // Emergency Tab
                                            showSpecialLogsView = false
                                            showSpecialSettingsView = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ThemeError.copy(alpha = 0.15f),
                                            contentColor = ThemeError
                                        ),
                                        border = BorderStroke(1.dp, ThemeError.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = ThemeError
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LOCKDOWN",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        Column {
                            HorizontalDivider(
                                color = ThemeGridBorder,
                                thickness = 1.dp
                            )
                            NavigationBar(
                                containerColor = ThemeSurface,
                                tonalElevation = 8.dp
                            ) {
                                val items = listOf(
                                    Triple("DASH", Icons.Default.Dashboard, 0),
                                    Triple("SERVERS", Icons.Default.Dns, 1),
                                    Triple("CONTS", Icons.Default.ViewModule, 2),
                                    Triple("CF DNS", Icons.Default.Language, 3),
                                    Triple("SHUTDOWN", Icons.Default.Dangerous, 4)
                                )

                                items.forEach { (label, icon, index) ->
                                    val isSelected = activeTab == index && !showSpecialLogsView && !showSpecialSettingsView
                                    val color = if (index == 4 && lockdownActive) ThemeError else if (isSelected) ThemePrimary else ThemeOnSurface

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            activeTab = index
                                            showSpecialLogsView = false
                                            showSpecialSettingsView = false
                                        },
                                        label = {
                                            Text(
                                                label,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (index == 4 && lockdownActive) {
                                                        Badge(containerColor = ThemeError) {
                                                            Text("!", color = Color.White)
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = color
                                                )
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ThemePrimary,
                                            selectedTextColor = ThemePrimary,
                                            unselectedIconColor = ThemeOnSurface,
                                            unselectedTextColor = ThemeOnSurface,
                                            indicatorColor = ThemeSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(ThemeBackground)
                    ) {
                        // Render top screens based on active actions state
                        when {
                            showSpecialLogsView -> {
                                LogsScreen(viewModel = viewModel)
                            }
                            showSpecialSettingsView -> {
                                SettingsScreen(viewModel = viewModel)
                            }
                            else -> {
                                AnimatedContent(
                                    targetState = activeTab,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                                    }
                                ) { targetTab ->
                                    when (targetTab) {
                                        0 -> DashboardScreen(
                                            viewModel = viewModel,
                                            onMoveToTab = { tab ->
                                                activeTab = tab
                                                showSpecialLogsView = false
                                                showSpecialSettingsView = false
                                            }
                                        )
                                        1 -> ServersScreen(viewModel = viewModel)
                                        2 -> ContainersScreen(viewModel = viewModel)
                                        3 -> DomainsScreen(viewModel = viewModel)
                                        4 -> EmergencyScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }

                        // Dynamic Global overlay Loading state indicator
                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant),
                                    border = BorderStroke(1.dp, ThemePrimary)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CyberLoader(
                                            size = 20.dp,
                                            strokeWidth = 2.dp,
                                            color1 = ThemePrimary,
                                            color2 = ThemeSecondary
                                        )
                                        Text(
                                            "COMMUNICATING FLEET...",
                                            color = ThemeOnBackground,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Secure Dialog-free overlay sheet
                    AnimatedVisibility(
                        visible = showLoginOverlay,
                        enter = fadeIn(animationSpec = tween(250)) + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = ThemeBackground
                        ) {
                            LoginScreen(
                                viewModel = viewModel,
                                onDismiss = { showLoginOverlay = false },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
