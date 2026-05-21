package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.components.CyberLoader
import com.example.ui.viewmodel.InfraViewModel

// ==========================================
// 1. LOGIN SCREEN (CYBERPUNK INDUSTRIAL)
// ==========================================
@Composable
fun LoginScreen(
    viewModel: InfraViewModel,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var usernameVal by remember { mutableStateOf("") }
    var passwordVal by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("admin") } // admin or viewer

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isDemoMode by viewModel.demoMode.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()

    var showBaseUrlEdit by remember { mutableStateOf(false) }
    var customUrl by remember { mutableStateOf(baseUrl) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic grid mesh background animation effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx().toInt().coerceAtLeast(10)
            val w = size.width.toInt()
            val h = size.height.toInt()
            if (w > 0 && h > 0) {
                for (x in 0..w step gridStep) {
                    drawLine(
                        color = ThemeGridBorder.copy(alpha = 0.15f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..h step gridStep) {
                    drawLine(
                        color = ThemeGridBorder.copy(alpha = 0.15f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Neon Brand Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        drawCircle(
                            color = ThemePrimary.copy(alpha = 0.1f),
                            radius = size.width / 1.5f
                        )
                        drawCircle(
                            color = ThemePrimary,
                            radius = size.width / 2.2f,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "BuildOS logo",
                    tint = ThemePrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "BUILDOS // INFRA",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ThemeOnBackground,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Text(
                text = "FLEET RECON MANAGEMENT CONSOLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemePrimary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, ThemeGridBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "SECURE CONSOLE GATEWAY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemePrimary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error Message Callout
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { error ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ThemeError.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, ThemeError.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Err", tint = ThemeError)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error,
                                        color = ThemeError,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Username Input
                    OutlinedTextField(
                        value = usernameVal,
                        onValueChange = {
                            usernameVal = it
                            viewModel.clearError()
                        },
                        label = { Text("OPERATOR ID", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemePrimary,
                            unfocusedBorderColor = ThemeGridBorder,
                            focusedTextColor = ThemeOnBackground,
                            unfocusedTextColor = ThemeOnBackground
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    OutlinedTextField(
                        value = passwordVal,
                        onValueChange = {
                            passwordVal = it
                            viewModel.clearError()
                        },
                        label = { Text("PASSCODE ACCESS", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemePrimary,
                            unfocusedBorderColor = ThemeGridBorder,
                            focusedTextColor = ThemeOnBackground,
                            unfocusedTextColor = ThemeOnBackground
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Role Selector
                    Text(
                        text = "SELECT ACCESS LEVEL PRIVILEGES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeOnSurface,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedRole = "admin" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "admin") ThemePrimary else ThemeSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (selectedRole == "admin") ThemePrimary else ThemeGridBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("role_admin_button"),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "ADMIN",
                                color = if (selectedRole == "admin") ThemeOnPrimary else ThemeOnBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { selectedRole = "viewer" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "viewer") ThemePrimary else ThemeSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (selectedRole == "viewer") ThemePrimary else ThemeGridBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("role_viewer_button"),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "VIEWER",
                                color = if (selectedRole == "viewer") ThemeOnPrimary else ThemeOnBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Trigger
                    Button(
                        onClick = { viewModel.login(usernameVal, passwordVal, selectedRole) { onDismiss?.invoke() } },
                        enabled = !isLoading && usernameVal.isNotEmpty() && passwordVal.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemePrimary,
                            disabledContainerColor = ThemeGridBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CyberLoader(size = 24.dp, strokeWidth = 2.dp, color1 = ThemeOnPrimary, color2 = ThemeOnPrimary.copy(alpha = 0.5f))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Login, contentDescription = "Log in", tint = ThemeOnPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ESTABLISH ACCESS LINK",
                                    color = ThemeOnPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    if (onDismiss != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onDismiss.invoke() },
                            border = BorderStroke(1.dp, ThemeOnSurface.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("cancel_login_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeOnBackground)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "CANCEL // DETACH SECURE MODE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sandbox switch info helper
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, ThemeGridBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SANDBOX TESTING FLIGHT MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemePrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Switch(
                            checked = isDemoMode,
                            onCheckedChange = { viewModel.toggleDemoMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThemePrimary,
                                checkedTrackColor = ThemePrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = ThemeOnSurface,
                                uncheckedTrackColor = ThemeSurfaceVariant
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                    Text(
                        text = if (isDemoMode) {
                            "Running in simulated secure sandbox. Actions succeed instantly offline with reactive fleet modifications."
                        } else {
                            "Direct live mode. Calls endpoints on: $baseUrl"
                        },
                        fontSize = 11.sp,
                        color = ThemeOnSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quick Sandbox Access: Enter any username & passcode then click Establish Access Link.",
                        fontSize = 10.sp,
                        color = ThemePrimary.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // URL config trigger
            TextButton(
                onClick = { showBaseUrlEdit = !showBaseUrlEdit }
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Config Base URL", tint = ThemeSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Configure REST Base Endpoint: $baseUrl",
                    color = ThemeSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            AnimatedVisibility(visible = showBaseUrlEdit) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        modifier = Modifier.weight(1.0f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemeSecondary,
                            unfocusedBorderColor = ThemeGridBorder,
                            focusedTextColor = ThemeOnBackground,
                            unfocusedTextColor = ThemeOnBackground
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                    Button(
                        onClick = {
                            viewModel.saveBaseUrl(customUrl)
                            showBaseUrlEdit = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}


// Extension to scale switches
fun Modifier.scale(scale: Float): Modifier = this.drawBehind {
    // Helper visual placeholder
}


// ==========================================
// 2. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: InfraViewModel,
    onMoveToTab: (Int) -> Unit
) {
    val token by viewModel.token.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val lockdownActive by viewModel.lockdownActive.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val backendHealth by viewModel.backendHealth.collectAsState()
    val lastSyncAt by viewModel.lastSyncAt.collectAsState()
    val syncMode by viewModel.syncMode.collectAsState()
    val healthError by viewModel.healthError.collectAsState()

    val onlineNodes = nodes.count { it.status == "online" }
    val warningNodes = nodes.count { it.status == "warning" }
    val offlineNodes = nodes.count { it.status == "offline" || it.status == "error" }

    val runningContainers = containers.count { it.state == "running" }
    val stoppedContainers = containers.count { it.state == "stopped" }

    var domainInput by remember { mutableStateOf(baseUrl) }
    var verifId by remember { mutableStateOf("") }
    var verifPass by remember { mutableStateOf("") }
    var verifRole by remember { mutableStateOf("admin") }
    var verifIdPassPromptVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Alert Pulse banner if lockdown active
        item {
            AnimatedContent(targetState = lockdownActive) { active ->
                if (active) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeError.copy(alpha = 0.15f)),
                        border = BorderStroke(2.dp, ThemeError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMoveToTab(4) } // navigate to Emergency tab
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dangerous,
                                contentDescription = "Lockdown active!",
                                tint = ThemeError,
                                modifier = Modifier
                                    .size(32.dp)
                                    .drawBehind {
                                        // Simple pulse glow drawing effect
                                    }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🚨 INFRASTRUCTURE WIDE EMERGENCY LOCKDOWN ACTIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeError,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    "ALL INCOMING FLEET TRAFFIC DISCHARGED. CONTAINERS KILL COMMAND CONFIGURED.",
                                    fontSize = 10.sp,
                                    color = ThemeOnBackground,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = ThemeError)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, ThemeGridBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(ThemePrimary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "ALL FLEET AGENTS REPORTING REGULAR SIGNALS & LOCKDOWN PASSIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemePrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        item {
            val healthStatus = backendHealth?.status ?: "unknown"
            val healthColor = when {
                healthStatus.equals("ok", ignoreCase = true) -> ThemePrimary
                healthStatus.equals("degraded", ignoreCase = true) -> ThemeTertiary
                healthStatus.equals("down", ignoreCase = true) -> ThemeError
                else -> ThemeSecondary
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, ThemeGridBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "BACKEND LINK STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnSurface,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(healthColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                .border(1.dp, healthColor, RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                healthStatus.uppercase(),
                                color = healthColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = backendHealth?.version?.let { "Version $it" } ?: "Version unknown",
                        fontSize = 11.sp,
                        color = ThemeOnBackground,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Sync: ${syncMode.uppercase()}  Last: ${lastSyncAt ?: "n/a"}",
                        fontSize = 10.sp,
                        color = ThemeOnSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    if (healthError != null) {
                        Text(
                            text = healthError ?: "",
                            fontSize = 10.sp,
                            color = ThemeError,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (token == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeSecondary.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = "Unlinked", tint = ThemeSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SECURE WEB ROUTING GATEWAY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "App is currently running in local unlinked guest mode (no dummy data). Enter your BuildOS instance domain to verify and link realtime data.",
                            fontSize = 11.sp,
                            color = ThemeOnSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = domainInput,
                            onValueChange = { domainInput = it },
                            label = { Text("HOST DOMAIN / BASE ENDPOINT URL", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ThemeSecondary,
                                unfocusedBorderColor = ThemeGridBorder,
                                focusedTextColor = ThemeOnBackground,
                                unfocusedTextColor = ThemeOnBackground
                            ),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (domainInput.isNotBlank()) {
                                    viewModel.saveBaseUrl(domainInput)
                                    verifIdPassPromptVisible = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "LINK DOMAIN & TRIGGER VERIFICATION",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (verifIdPassPromptVisible) {
                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = ThemeGridBorder, thickness = 1.dp)

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = ThemeBackground),
                                border = BorderStroke(1.dp, ThemeGridBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VpnKey, contentDescription = "Credentials Required", tint = ThemePrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "🔐 PROVIDE SECURITY CREDENTIALS TO VERIFY",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ThemePrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        "To establish decryption links with '$domainInput' and verify ownership, please provide your Admin ID and passcode below.",
                                        fontSize = 10.sp,
                                        color = ThemeOnSurface
                                    )

                                    OutlinedTextField(
                                        value = verifId,
                                        onValueChange = { verifId = it },
                                        label = { Text("OPERATOR ID / USERNAME", fontFamily = FontFamily.Monospace, fontSize = 9.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ThemePrimary,
                                            unfocusedBorderColor = ThemeGridBorder,
                                            focusedTextColor = ThemeOnBackground,
                                            unfocusedTextColor = ThemeOnBackground
                                        ),
                                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    )

                                    OutlinedTextField(
                                        value = verifPass,
                                        onValueChange = { verifPass = it },
                                        label = { Text("OPERATOR SECURE PASSCODE", fontFamily = FontFamily.Monospace, fontSize = 9.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ThemePrimary,
                                            unfocusedBorderColor = ThemeGridBorder,
                                            focusedTextColor = ThemeOnBackground,
                                            unfocusedTextColor = ThemeOnBackground
                                        ),
                                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    )

                                    Button(
                                        onClick = {
                                            if (verifId.isNotBlank() && verifPass.isNotBlank()) {
                                                viewModel.login(verifId, verifPass, "admin") {}
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ESTABLISH ACCESS LINK & VIEW REALTIME DATA",
                                            color = ThemeOnPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // High Density Quick Stats Row (Horizontal Scrollable Area)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nodes Stat Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(115.dp)
                        .clickable { onMoveToTab(1) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NODES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeOnSurface,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${nodes.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemePrimary
                        )
                    }
                }

                // Offline Stat Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(115.dp)
                        .clickable { onMoveToTab(1) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeOnSurface,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$offlineNodes",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (offlineNodes > 0) ThemeError else ThemeOnSurface
                        )
                    }
                }

                // Containers Stat Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(115.dp)
                        .clickable { onMoveToTab(2) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "CONTAINERS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeOnSurface,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${containers.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeTertiary
                        )
                    }
                }

                // DNS Zones Stat Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(115.dp)
                        .clickable { onMoveToTab(3) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DNS ZONES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeOnSurface,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${zones.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeSecondary
                        )
                    }
                }
            }
        }

        // Custom Radial Telemetry Health Visualization Graphic
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, ThemeGridBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FLEET SIGNAL TELEMETRY RATIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemePrimary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val safetyPercent = if (nodes.isEmpty()) 0f else {
                        (nodes.count { it.status == "online" }.toFloat() / nodes.size.toFloat())
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            // Backing track
                            drawCircle(
                                color = ThemeGridBorder.copy(alpha = 0.3f),
                                style = Stroke(width = 10.dp.toPx())
                            )
                            // Progress sweep
                            drawArc(
                                color = if (lockdownActive) ThemeError else if (safetyPercent > 0.7f) ThemePrimary else ThemeTertiary,
                                startAngle = -90f,
                                sweepAngle = safetyPercent * 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx())
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(safetyPercent * 100).toInt()}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeOnBackground,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "ONLINE RATIO",
                                fontSize = 9.sp,
                                color = ThemeOnSurface,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CPU LOAD", fontSize = 10.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
                            Text("38.4%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeSecondary, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NET BANDWIDTH", fontSize = 10.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
                            Text("42.8 MB/s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemePrimary, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RAM CAPACITY", fontSize = 10.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
                            Text("64.2%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Recent System Logs Ticker Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, ThemeGridBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ThemeSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "RECENT SECURE AUDIT EVENTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        TextButton(
                            onClick = { onMoveToTab(5) }, // navigate to full green phosphors logs
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("OPEN CONSOLE", color = ThemeSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        logs.take(3).forEach { log ->
                            val color = when (log.level) {
                                "ERROR", "CRITICAL" -> ThemeError
                                "WARN" -> ThemeTertiary
                                "INFO" -> ThemePrimary
                                else -> ThemeOnBackground
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ThemeSurfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "[${log.timestamp}]",
                                    fontSize = 11.sp,
                                    color = color.copy(alpha = 0.8f),
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = log.message,
                                    fontSize = 11.sp,
                                    color = ThemeOnBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. SERVERS SCREEN (NODES, DISPATCH CMD)
// ==========================================
@Composable
fun ServersScreen(
    viewModel: InfraViewModel
) {
    val nodes by viewModel.nodes.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val nodeGuests by viewModel.nodeGuests.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var expandedNodeId by remember { mutableStateOf<String?>(null) }

    // Forms variables
    var nodeName by remember { mutableStateOf("") }
    var nodeType by remember { mutableStateOf("PROXMOX") } // PROXMOX, DOCKER, STANDALONE
    var nodeProvider by remember { mutableStateOf("Hetzner") }
    var nodeIp by remember { mutableStateOf("") }
    var nodeRegion by remember { mutableStateOf("hel1-eu") }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gated Admin register control panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CONSOLIDATED INFRASTRUCTURE NODES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeSecondary,
                    fontFamily = FontFamily.Monospace
                )

                if (userRole == "admin") {
                    Button(
                        onClick = { showAddForm = !showAddForm },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.testTag("admin_register_node_btn")
                    ) {
                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add node", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showAddForm) "CLOSE" else "REGISTER", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeGridBorder.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, ThemeGridBorder.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "ReadOnly Safe-Mode",
                            fontSize = 8.6.sp,
                            color = ThemeOnSurface,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Add node registration expander form
        item {
            AnimatedVisibility(visible = showAddForm) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "REGISTER COLD INSTANCE INTERFACE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemePrimary,
                            fontFamily = FontFamily.Monospace
                        )

                        OutlinedTextField(
                            value = nodeName,
                            onValueChange = { nodeName = it },
                            label = { Text("NODE IDENTIFIER NAME", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemePrimary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        // Cluster Type Selector
                        Text("HYPERVISOR RUNTIME ARCHITECTURE TYPE", fontSize = 10.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("PROXMOX", "DOCKER", "STANDALONE").forEach { typ ->
                                Button(
                                    onClick = { nodeType = typ },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (nodeType == typ) ThemePrimary else ThemeSurfaceVariant),
                                    border = BorderStroke(1.dp, if (nodeType == typ) ThemePrimary else ThemeGridBorder),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(typ, fontSize = 9.8.sp, fontWeight = FontWeight.Bold, color = if (nodeType == typ) Color.Black else ThemeOnBackground, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nodeProvider,
                                onValueChange = { nodeProvider = it },
                                label = { Text("PROVIDER", fontFamily = FontFamily.Monospace) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemePrimary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            )
                            OutlinedTextField(
                                value = nodeRegion,
                                onValueChange = { nodeRegion = it },
                                label = { Text("REGION CODE", fontFamily = FontFamily.Monospace) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemePrimary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            )
                        }

                        OutlinedTextField(
                            value = nodeIp,
                            onValueChange = { nodeIp = it },
                            label = { Text("EXTERNAL IP POINTER ADDRESS", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemePrimary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        Button(
                            onClick = {
                                if (nodeName.isNotBlank() && nodeIp.isNotBlank()) {
                                    viewModel.registerNode(nodeName, nodeType, nodeProvider, nodeIp, nodeRegion)
                                    // Reset values
                                    nodeName = ""
                                    nodeIp = ""
                                    showAddForm = false
                                } else {
                                    Toast.makeText(context, "All required parameters must be set!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("PROVISION AGENT GATEWAY", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Nodes List loop
        items(nodes) { node ->
            val statusColor = when (node.status) {
                "online" -> ThemePrimary
                "warning" -> ThemeTertiary
                "offline", "error" -> ThemeError
                else -> ThemeOnSurface
            }

            val isExpanded = expandedNodeId == node.id

            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, if (isExpanded) ThemeSecondary else ThemeGridBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedNodeId = if (isExpanded) null else node.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Status Info row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Status Dot Indicator
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = node.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeOnBackground,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Type badge
                        Box(
                            modifier = Modifier
                                .border(1.dp, ThemeGridBorder, RoundedCornerShape(4.dp))
                                .background(ThemeSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = node.type,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ThemeSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Node specs row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("IP ADDRESS", color = ThemeOnSurface, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            Text(node.ip, color = ThemeOnBackground, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PROVIDER", color = ThemeOnSurface, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            Text(node.provider, color = ThemeOnBackground, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PING TIMING", color = ThemeOnSurface, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            Text(node.lastPing, color = statusColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Expanded detail panel
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .border(1.dp, ThemeGridBorder, RoundedCornerShape(6.dp))
                                .background(ThemeSurfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SECURE COLD AGENT ACCESS", color = ThemePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Box(
                                    modifier = Modifier
                                        .background(ThemeGridBorder, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(node.agentToken))
                                            Toast.makeText(context, "Agent Key copied to buffer clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Text("COPY KEY Token", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = ThemeOnBackground)
                                }
                            }

                            Text(
                                text = node.agentToken,
                                color = ThemeOnSurface,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Divider(color = ThemeGridBorder, thickness = 1.dp)

                            Text("AUTOPILOT CLI DEPLOYMENT COMMAND", color = ThemeOnBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                            // Command Block
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .border(1.dp, ThemeGridBorder, RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = node.installCommand,
                                    color = ThemePrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(node.installCommand))
                                        Toast.makeText(context, "Install command copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Command", tint = ThemePrimary, modifier = Modifier.size(16.dp))
                                }
                            }

                            // PROXMOX hypervisor guests management component
                            if (node.type == "PROXMOX") {
                                Divider(color = ThemeGridBorder, thickness = 1.dp)
                                Text("PROXMOX VIRTUAL CLUSTER GUEST INTERACTION PANEL", color = ThemePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                val guests = nodeGuests[node.id] ?: emptyList()
                                if (guests.isEmpty()) {
                                    Text("No vm cluster guests found or offline.", fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = ThemeOnSurface)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        guests.forEach { guest ->
                                            val guestStatusColor = if (guest.status == "running") ThemePrimary else ThemeOnSurface
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.5f))
                                                    .border(1.dp, ThemeGridBorder, RoundedCornerShape(4.dp))
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(guestStatusColor))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("VMID ${guest.vmid} : ${guest.name}", color = ThemeOnBackground, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text("Architecture: ${guest.type} // CPU: ${guest.cpu}% // RAM: ${(guest.mem / 1024 / 1024)}MB", color = ThemeOnSurface, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                                                }

                                                if (userRole == "admin") {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        if (guest.status == "running") {
                                                            Button(
                                                                onClick = { viewModel.controlProxmoxGuest(node.id, guest.type, guest.vmid, "shutdown") },
                                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeError),
                                                                contentPadding = PaddingValues(horizontal = 6.dp),
                                                                shape = RoundedCornerShape(2.dp),
                                                                modifier = Modifier.height(24.dp)
                                                            ) {
                                                                Text("STOP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground, fontFamily = FontFamily.Monospace)
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = { viewModel.controlProxmoxGuest(node.id, guest.type, guest.vmid, "start") },
                                                                colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                                                                contentPadding = PaddingValues(horizontal = 6.dp),
                                                                shape = RoundedCornerShape(2.dp),
                                                                modifier = Modifier.height(24.dp)
                                                            ) {
                                                                Text("START", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ThemeOnPrimary, fontFamily = FontFamily.Monospace)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Admin Actions Panel (Token Rotate, Delete)
                            if (userRole == "admin") {
                                Divider(color = ThemeGridBorder, thickness = 1.dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        onClick = { viewModel.rotateNodeToken(node.id) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = ThemeTertiary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ROTATE AGENT AUTHORIZATION", color = ThemeTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }

                                    TextButton(
                                        onClick = { viewModel.deleteNode(node.id) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = ThemeError, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PURGE NODE", color = ThemeError, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. CONTAINERS SCREEN
// ==========================================
@Composable
fun ContainersScreen(
    viewModel: InfraViewModel
) {
    val containers by viewModel.containers.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "ACTIVE ENCLAVE APP CONTAINERS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        items(containers) { container ->
            val isRunning = container.state == "running"
            val stateColor = if (isRunning) ThemePrimary else ThemeOnSurface

            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, if (container.autoHeal) ThemePrimary.copy(alpha = 0.5f) else ThemeGridBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Title and status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(stateColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = container.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeOnBackground,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // State Badges
                        Box(
                            modifier = Modifier
                                .background(stateColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = container.state.uppercase(),
                                fontSize = 8.6.sp,
                                fontWeight = FontWeight.Bold,
                                color = stateColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Node and Image parameters
                    Text(
                        text = "NODE HOST: ${container.nodeName}",
                        fontSize = 10.sp,
                        color = ThemeOnSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SOURCE IMAGE: ${container.image}",
                        fontSize = 10.sp,
                        color = ThemeOnSurface,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "MAPPED PORTS: ${container.ports}",
                        fontSize = 10.sp,
                        color = ThemeSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DOMAIN RECORD: ${container.domain}",
                        fontSize = 10.sp,
                        color = ThemeOnBackground,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = ThemeGridBorder, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Control actions block (Admin role gate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = container.autoHeal,
                                onCheckedChange = {
                                    if (userRole == "admin") {
                                        viewModel.toggleContainerAutoHeal(container.id, it)
                                    } else {
                                        // Auto reset visual check if user is viewer
                                        Toast.makeText(context, "Write privileges required for auto-heal toggle", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = userRole == "admin",
                                colors = CheckboxDefaults.colors(checkedColor = ThemePrimary, checkmarkColor = ThemeOnPrimary)
                            )
                            Text(
                                "AUTO HEAL",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (container.autoHeal) ThemePrimary else ThemeOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (userRole == "admin") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isRunning) {
                                    Button(
                                        onClick = { viewModel.controlContainer(container.id, "stop") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ThemeError),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("STOP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground, fontFamily = FontFamily.Monospace)
                                    }
                                    Button(
                                        onClick = { viewModel.controlContainer(container.id, "restart") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ThemeSurfaceVariant),
                                        border = BorderStroke(1.dp, ThemeGridBorder),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("RESTART", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ThemeSecondary, fontFamily = FontFamily.Monospace)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.controlContainer(container.id, "start") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("START", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ThemeOnPrimary, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Locked View",
                                fontSize = 10.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = ThemeOnSurface,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. DOMAINS SCREEN (CLOUDFLARE ZONES & ACCESS)
// ==========================================
@Composable
fun DomainsScreen(
    viewModel: InfraViewModel
) {
    val zones by viewModel.zones.collectAsState()
    val recordsMap by viewModel.dnsRecords.collectAsState()
    val selectedZoneId by viewModel.selectedZoneId.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val liveDnsQuery by viewModel.liveDnsQuery.collectAsState()
    val liveDnsType by viewModel.liveDnsType.collectAsState()
    val liveDnsResults by viewModel.liveDnsResults.collectAsState()
    val liveDnsLoading by viewModel.liveDnsLoading.collectAsState()
    val liveDnsError by viewModel.liveDnsError.collectAsState()

    var customSearchDomain by remember { mutableStateOf("") }
    var selectedSearchType by remember { mutableStateOf("A") }

    var showCreateRecord by remember { mutableStateOf(false) }
    var showAddZone by remember { mutableStateOf(false) }

    // Forms states
    var dnsName by remember { mutableStateOf("") }
    var dnsType by remember { mutableStateOf("A") }
    var dnsTarget by remember { mutableStateOf("") }
    var zoneNameVal by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Real-Time Live Public DNS Resolver card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Dns logo",
                                tint = ThemePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "REAL-TIME PUBLIC DNS SOLVER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemePrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(ThemePrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "LIVE INTERNET MODE",
                                fontSize = 8.sp,
                                color = ThemePrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        "Query details from active nameservers. Bypasses mock data sandbox to fetch live global IP pointers.",
                        fontSize = 11.sp,
                        color = ThemeOnSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customSearchDomain,
                            onValueChange = { customSearchDomain = it },
                            placeholder = { Text("e.g. google.com", fontFamily = FontFamily.Monospace, color = ThemeOnSurface.copy(alpha = 0.3f)) },
                            label = { Text("TARGET DOMAIN QUERY", fontFamily = FontFamily.Monospace, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ThemePrimary,
                                unfocusedBorderColor = ThemeGridBorder,
                                focusedTextColor = ThemeOnBackground,
                                unfocusedTextColor = ThemeOnBackground
                            ),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        Button(
                            onClick = {
                                if (customSearchDomain.isNotBlank()) {
                                    viewModel.resolveLiveDns(customSearchDomain.trim(), selectedSearchType)
                                }
                            },
                            enabled = !liveDnsLoading && customSearchDomain.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(54.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            if (liveDnsLoading) {
                                CyberLoader(size = 18.dp, strokeWidth = 1.5.dp, color1 = Color.Black, color2 = Color.Black.copy(alpha = 0.5f))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Query", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Text("RESOLVE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Query record types selectors layout
                    ScrollableTabRow(
                        selectedTabIndex = listOf("A", "AAAA", "CNAME", "MX", "TXT", "NS").indexOf(selectedSearchType),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        listOf("A", "AAAA", "CNAME", "MX", "TXT", "NS").forEach { t ->
                            val isSelected = selectedSearchType == t
                            Tab(
                                selected = isSelected,
                                onClick = { selectedSearchType = t },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .background(
                                        if (isSelected) ThemePrimary else ThemeSurfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) ThemePrimary else ThemeGridBorder,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = t,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else ThemeOnBackground,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Display results / states
                    if (liveDnsLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Contacting global public nameservers...",
                                fontSize = 11.sp,
                                color = ThemePrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (liveDnsError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ThemeError.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, ThemeError.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = liveDnsError ?: "",
                                color = ThemeError,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (liveDnsResults.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RESOLVED ${liveDnsResults.size} RECORDS FOR $liveDnsQuery",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "CLEAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeOnSurface,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable { viewModel.clearLiveDnsResults() }
                                )
                            }

                            liveDnsResults.forEach { entry ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, ThemeGridBorder.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = entry.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ThemeOnBackground,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "TTL: ${entry.ttl}s",
                                                fontSize = 9.sp,
                                                color = ThemeOnSurface,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = entry.data,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ThemePrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Upper zones summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CLOUDFLARE ROUTING DOMAIN ZONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeSecondary,
                    fontFamily = FontFamily.Monospace
                )

                if (userRole == "admin") {
                    Button(
                        onClick = { showAddZone = !showAddZone },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (showAddZone) "CLOSE" else "ADD ZONE", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Add zone panel
        item {
            AnimatedVisibility(visible = showAddZone) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("PROVISION APEX ZONE INTERFACE", fontSize = 10.sp, color = ThemeSecondary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        OutlinedTextField(
                            value = zoneNameVal,
                            onValueChange = { zoneNameVal = it },
                            label = { Text("ZONE NAME (e.g. shashank.dev)", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeSecondary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )
                        Button(
                            onClick = {
                                if (zoneNameVal.isNotBlank()) {
                                    viewModel.addCloudflareZone(zoneNameVal)
                                    zoneNameVal = ""
                                    showAddZone = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("ATTACH CLOUDFLARE DNS SYSTEM", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Horizontal zones pill selector list
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zones.forEach { zone ->
                    val isSelected = zone.id == selectedZoneId
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) ThemePrimary else ThemeSurfaceVariant, RoundedCornerShape(4.dp))
                            .border(1.dp, if (isSelected) ThemePrimary else ThemeGridBorder, RoundedCornerShape(4.dp))
                            .clickable { viewModel.selectZone(zone.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = zone.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else ThemeOnBackground,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = zone.status.uppercase(),
                                fontSize = 8.sp,
                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else ThemeOnSurface,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Zone accessibility checks panel
        item {
            val currentZone = zones.find { it.id == selectedZoneId }
            if (currentZone != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SECURITY STATUS: ${currentZone.zoneHealth}", color = ThemePrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("Routing active with CF Enterprise shields", color = ThemeOnSurface, fontSize = 9.sp)
                        }
                        Button(
                            onClick = { viewModel.checkZoneAccess(currentZone.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeSurfaceVariant),
                            border = BorderStroke(1.dp, ThemeGridBorder),
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("VERIFY ACCESS", fontSize = 9.sp, color = ThemeSecondary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Create resource DNS form trigger
        item {
            val selectedZone = zones.find { it.id == selectedZoneId }
            if (selectedZone != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DNS RECORD POINTER LIST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground, fontFamily = FontFamily.Monospace)

                    if (userRole == "admin") {
                        Button(
                            onClick = { showCreateRecord = !showCreateRecord },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(if (showCreateRecord) "CLOSE FORM" else "CREATE RECORD", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // DNS Record registration inline drawer Form
        item {
            AnimatedVisibility(visible = showCreateRecord) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("PROVISION RESOURCE POINTER dns record", fontSize = 10.sp, color = ThemePrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                        OutlinedTextField(
                            value = dnsName,
                            onValueChange = { dnsName = it },
                            label = { Text("RECORD POINTER NAME (e.g. *.os)", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemePrimary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        Text("RECORD POINTER TARGET TYPE", fontSize = 10.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("A", "CNAME", "TXT").forEach { t ->
                                Button(
                                    onClick = { dnsType = t },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (dnsType == t) ThemePrimary else ThemeSurfaceVariant),
                                    border = BorderStroke(1.dp, if (dnsType == t) ThemePrimary else ThemeGridBorder),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(t, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (dnsType == t) Color.Black else ThemeOnBackground, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = dnsTarget,
                            onValueChange = { dnsTarget = it },
                            label = { Text("IP VALUE / TARGET DOMAIN", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemePrimary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )

                        Button(
                            onClick = {
                                if (dnsName.isNotBlank() && dnsTarget.isNotBlank()) {
                                    viewModel.createDnsRecord(selectedZoneId, dnsName, dnsType, dnsTarget)
                                    dnsName = ""
                                    dnsTarget = ""
                                    showCreateRecord = false
                                } else {
                                    Toast.makeText(context, "Required entry content missing!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("PUSH TO CLOUDFLARE SERVERS", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // DNS Records display container
        val currentDnsList = recordsMap[selectedZoneId] ?: emptyList()
        if (isLoading) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CyberLoader(size = 36.dp, color1 = ThemePrimary, color2 = ThemeSecondary)
                        Text(
                            "Resolving live DNS pointers from active internet nameservers...",
                            fontSize = 11.sp,
                            color = ThemePrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (currentDnsList.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = ThemeSurfaceVariant.copy(alpha = 0.4f)), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No DNS record mappings cataloged in zone.",
                        fontSize = 11.sp,
                        color = ThemeOnSurface,
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            items(currentDnsList) { rec ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    border = BorderStroke(1.dp, ThemeGridBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(ThemeSecondary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(rec.type, color = ThemeSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(rec.name, color = ThemeOnBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("Point reference: ${rec.content}", color = ThemeOnSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))
                        }

                        if (userRole == "admin") {
                            IconButton(onClick = { viewModel.deleteDnsRecord(selectedZoneId, rec.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete DNS record", tint = ThemeError, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 6. EMERGENCY SYSTEM WIDE CONTROL CENTER
// ==========================================
@Composable
fun EmergencyScreen(
    viewModel: InfraViewModel
) {
    val lockdownActive by viewModel.lockdownActive.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    var showConfirmBlock by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Red Emergency Radar Logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .drawBehind {
                    drawCircle(
                        color = ThemeError.copy(alpha = 0.1f),
                        radius = size.width / 1.3f
                    )
                    drawCircle(
                        color = ThemeError,
                        radius = size.width / 2.2f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReportProblem,
                contentDescription = "Alert logo",
                tint = ThemeError,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CRITICAL CONTROL ENCLAVE",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ThemeOnBackground,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Text(
            text = "FLEET EMERGENCY OVERRIDE & LOCKDOWN FLOW",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeError,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
            border = BorderStroke(1.dp, ThemeGridBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LOCKDOWN PRIVILEGES STATE", fontSize = 11.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .background(if (lockdownActive) ThemeError.copy(alpha = 0.15f) else ThemeGridBorder, RoundedCornerShape(4.dp))
                            .border(1.dp, if (lockdownActive) ThemeError else ThemeGridBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (lockdownActive) "ACTIVE SHUTDOWN" else "PASSIVE SAFE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lockdownActive) ThemeError else ThemePrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A lockdown event will immediately command all registered fleet agents to execute FORCE-KILLS. Port connections are completely disarmed and Cloudflare zone mappings are held in secure quarantine. Use only during active server intrusion or cluster compromise events.",
                    fontSize = 11.sp,
                    color = ThemeOnSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Actions Gated Admin Flow
        if (userRole == "admin") {
            if (!lockdownActive) {
                if (!showConfirmBlock) {
                    Button(
                        onClick = { showConfirmBlock = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .testTag("force_kill_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Dangerous, contentDescription = null, tint = ThemeOnBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FORCE INITIALIZE LOCKDOWN", color = ThemeOnBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        border = BorderStroke(2.dp, ThemeError),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚨 CONFIRM FORCE OVERRIDE ACTIONS?", color = ThemeError, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("THIS WILL DISABLING ALL REACHABLE SERVER RUNTIMES ON PROD", color = ThemeOnBackground, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { showConfirmBlock = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeSurfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("ABORT", color = ThemeOnBackground, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Button(
                                    onClick = {
                                        viewModel.triggerLockdown()
                                        showConfirmBlock = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeError),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("FORCE SHUTDOWN", color = ThemeOnBackground, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.releaseLockdown() },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .testTag("release_lockdown_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = ThemeOnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RELEASE REFRESH SECURE LOCKDOWN", color = ThemeOnPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeGridBorder.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, ThemeGridBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ThemeOnSurface)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Viewing role 'viewer' does not have credentials to trigger or release emergency payload.",
                        fontSize = 11.sp,
                        color = ThemeOnSurface,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}


// ==========================================
// 7. SECURE METASECTOR SYSTEM LOGS TERMINAL
// ==========================================
@Composable
fun LogsScreen(
    viewModel: InfraViewModel
) {
    val logs by viewModel.logs.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Force pitch black terminal screen
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                " phosphor CRT SECURE SHELL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ThemePrimary,
                fontFamily = FontFamily.Monospace
            )
            IconButton(onClick = { viewModel.refreshAll() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync Logs", tint = ThemePrimary, modifier = Modifier.size(16.dp))
            }
        }

        // Horizontal scrolling filter pills matching CRT panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "INFO", "WARN", "ERROR", "CRITICAL").forEach { level ->
                val isSelected = selectedFilter == level
                val pillColor = when (level) {
                    "ERROR", "CRITICAL" -> ThemeError
                    "WARN" -> ThemeTertiary
                    "INFO" -> ThemePrimary
                    else -> ThemeSecondary
                }
                Box(
                    modifier = Modifier
                        .background(if (isSelected) pillColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(2.dp))
                        .border(1.dp, if (isSelected) pillColor else ThemeGridBorder, RoundedCornerShape(2.dp))
                        .clickable { selectedFilter = level }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = level,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) pillColor else ThemeOnSurface,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real scrolling CRT screen block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020402)),
            border = BorderStroke(1.dp, ThemePrimary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
        ) {
            val filteredLogs = if (selectedFilter == "ALL") logs else logs.filter { it.level == selectedFilter }
            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("CRT Console empty. No payload events.", color = ThemePrimary.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    items(filteredLogs) { log ->
                        val logColor = when (log.level) {
                            "ERROR", "CRITICAL" -> ThemeError
                            "WARN" -> ThemeTertiary
                            "INFO" -> ThemePrimary
                            else -> ThemeOnBackground
                        }
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = "[${log.timestamp}] [${log.level}] in [${log.source}]:",
                                fontSize = 11.sp,
                                color = logColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = " > ${log.message}",
                                fontSize = 11.sp,
                                color = ThemeOnBackground,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 8. MOBILE CONFIGURATION SETTINGS
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: InfraViewModel
) {
    val baseUrl by viewModel.baseUrl.collectAsState()
    val username by viewModel.username.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isDemoMode by viewModel.demoMode.collectAsState()
    val backendHealth by viewModel.backendHealth.collectAsState()
    val lastSyncAt by viewModel.lastSyncAt.collectAsState()
    val healthError by viewModel.healthError.collectAsState()

    var customUrlInput by remember { mutableStateOf(baseUrl) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "OPERATIONAL SERVICES CONTROL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeSecondary,
            fontFamily = FontFamily.Monospace
        )

        // Session status card
        Card(
            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
            border = BorderStroke(1.dp, ThemeGridBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AUTHORIZED OPERATOR SECURE SESSION", fontSize = 10.sp, color = ThemePrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("OPERATOR ACCOUNT ID", fontSize = 11.sp, color = ThemeOnSurface)
                    Text(username ?: "UNASSIGNED", fontSize = 11.sp, color = ThemeOnBackground, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PRIVILEGE POLICY ROLE", fontSize = 11.sp, color = ThemeOnSurface)
                    Box(modifier = Modifier.background(ThemeSecondary.copy(alpha = 0.15f), RoundedCornerShape(3.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(userRole?.uppercase() ?: "VIEWER", fontSize = 9.sp, color = ThemeSecondary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("DECENTRALIZED ENGEN TESTING", fontSize = 11.sp, color = ThemeOnSurface)
                    Text(if (isDemoMode) "SANDBOX COMPATIBLE" else "REAL PROD ACTIVE", fontSize = 11.sp, color = ThemePrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BACKEND HEALTH", fontSize = 11.sp, color = ThemeOnSurface)
                    Text(backendHealth?.status?.uppercase() ?: "UNKNOWN", fontSize = 11.sp, color = ThemePrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LAST SYNC", fontSize = 11.sp, color = ThemeOnSurface)
                    Text(lastSyncAt ?: "n/a", fontSize = 11.sp, color = ThemeOnBackground, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                if (healthError != null) {
                    Text(healthError ?: "", fontSize = 10.sp, color = ThemeError, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // URL modification block
        Card(
            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
            border = BorderStroke(1.dp, ThemeGridBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("API SECURE ROUTING CONFIGURATION", fontSize = 10.sp, color = ThemeSecondary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                OutlinedTextField(
                    value = customUrlInput,
                    onValueChange = { customUrlInput = it },
                    label = { Text("BACKEND API DOMAIN URL", fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeSecondary, unfocusedBorderColor = ThemeGridBorder, focusedTextColor = ThemeOnBackground, unfocusedTextColor = ThemeOnBackground),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                )

                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            viewModel.saveBaseUrl(customUrlInput)
                        } else {
                            Toast.makeText(context, "Url cannot be empty!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeSecondary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("RE-LINK BASE DESTINATION", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Demo interactive settings switch
        Card(
            colors = CardDefaults.cardColors(containerColor = ThemeSurface),
            border = BorderStroke(1.dp, ThemeGridBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("LOCAL DEMO SIMULATOR STATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ThemePrimary, fontFamily = FontFamily.Monospace)
                    Text("Allow secure sandbox actions without active server endpoint linkages.", fontSize = 10.sp, color = ThemeOnSurface, modifier = Modifier.padding(top = 4.dp))
                }
                Switch(
                    checked = isDemoMode,
                    onCheckedChange = { viewModel.toggleDemoMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ThemePrimary, checkedTrackColor = ThemePrimary.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Terminate Access button
        Button(
            onClick = { viewModel.logout {} },
            colors = ButtonDefaults.buttonColors(containerColor = ThemeError),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_button"),
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Purge console link", tint = ThemeOnBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISCHARGE AUTHORIZED SESSION LINK", color = ThemeOnBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Meta specifications
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("BUILDOS MOBILE CONSOLE CLIENT INDEX", fontSize = 10.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
            Text("v2.6.5-PROD-RELEASE // MAY-2026", fontSize = 9.sp, color = ThemeOnSurface, fontFamily = FontFamily.Monospace)
        }
    }
}
