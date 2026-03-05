package com.stack.feature.gate

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.core.ui.theme.Spacing
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GateScreen(
    onGateCompleted: () -> Unit,
    viewModel: GateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.dispatch(GateIntent.MediaPermissionResult(granted))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.dispatch(GateIntent.NotificationPermissionResult(granted))
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)

            val displayName = uri.lastPathSegment?.substringAfterLast(':') ?: uri.toString()
            viewModel.dispatch(GateIntent.FolderSelected(uri.toString(), displayName))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                GateEffect.NavigateToMain -> onGateCompleted()
                GateEffect.RequestMediaPermission -> {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    mediaPermissionLauncher.launch(permission)
                }
                GateEffect.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                GateEffect.OpenFolderPicker -> {
                    folderPickerLauncher.launch(null)
                }
                GateEffect.OpenAppSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    AnimatedContent(
        targetState = state.currentStep,
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        label = "gate_step"
    ) { step ->
        when (step) {
            GateStep.WELCOME -> WelcomeCard(
                onGetStarted = { viewModel.dispatch(GateIntent.StartOnboarding) }
            )
            GateStep.MEDIA_ACCESS -> MediaAccessCard(
                permissionDenied = state.permissionDeniedPermanently,
                onGrantPermission = { viewModel.dispatch(GateIntent.RequestMediaPermission) },
                onOpenSettings = { viewModel.dispatch(GateIntent.OpenAppSettings) }
            )
            GateStep.NOTIFICATION -> NotificationCard(
                onGrantPermission = { viewModel.dispatch(GateIntent.RequestNotificationPermission) },
                onSkip = { viewModel.dispatch(GateIntent.SkipNotificationPermission) }
            )
            GateStep.FOLDER_SELECT -> FolderSelectCard(
                folderSelected = state.folderSelected,
                selectedFolderName = state.selectedFolderName,
                onSelectFolder = { viewModel.dispatch(GateIntent.OpenFolderPicker) },
                onNext = { viewModel.dispatch(GateIntent.StartScan) }
            )
            GateStep.SCANNING -> ScanCard(
                scanProgress = state.scanProgress,
                scanCompleted = state.scanCompleted,
                scanError = state.scanError,
                onRetry = { viewModel.dispatch(GateIntent.RetryScan) },
                onFinish = { viewModel.dispatch(GateIntent.FinishGate) }
            )
        }
    }
}

@Composable
private fun WelcomeCard(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.gate_welcome_title),
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = stringResource(R.string.gate_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Button(onClick = onGetStarted) {
            Text(text = stringResource(R.string.gate_get_started))
        }
    }
}

@Composable
private fun MediaAccessCard(
    permissionDenied: Boolean,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.gate_media_access_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = if (permissionDenied) {
                stringResource(R.string.gate_permission_denied)
            } else {
                stringResource(R.string.gate_media_access_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        if (permissionDenied) {
            Button(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.gate_open_settings))
            }
        } else {
            Button(onClick = onGrantPermission) {
                Text(text = stringResource(R.string.gate_grant_permission))
            }
        }
    }
}

@Composable
private fun NotificationCard(
    onGrantPermission: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.gate_notification_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = stringResource(R.string.gate_notification_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Button(onClick = onGrantPermission) {
            Text(text = stringResource(R.string.gate_grant_permission))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        OutlinedButton(onClick = onSkip) {
            Text(text = stringResource(R.string.gate_notification_skip))
        }
    }
}

@Composable
private fun FolderSelectCard(
    folderSelected: Boolean,
    selectedFolderName: String?,
    onSelectFolder: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.gate_source_folder_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = if (folderSelected && selectedFolderName != null) {
                stringResource(R.string.gate_folder_selected, selectedFolderName)
            } else {
                stringResource(R.string.gate_source_folder_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        if (folderSelected) {
            Button(onClick = onNext) {
                Text(text = stringResource(R.string.gate_next))
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            OutlinedButton(onClick = onSelectFolder) {
                Text(text = stringResource(R.string.gate_select_folder))
            }
        } else {
            Button(onClick = onSelectFolder) {
                Text(text = stringResource(R.string.gate_select_folder))
            }
        }
    }
}

@Composable
private fun ScanCard(
    scanProgress: com.stack.domain.model.ScanProgress,
    scanCompleted: Boolean,
    scanError: Boolean,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (scanCompleted) {
                stringResource(R.string.gate_scan_complete)
            } else {
                stringResource(R.string.gate_scan_title)
            },
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(Spacing.sm))

        when {
            scanError -> {
                Text(
                    text = stringResource(R.string.gate_scan_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.gate_retry))
                }
            }
            scanCompleted -> {
                Text(
                    text = stringResource(R.string.gate_scan_complete_count, scanProgress.totalFiles),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Button(onClick = onFinish) {
                    Text(text = stringResource(R.string.gate_finish))
                }
            }
            else -> {
                Text(
                    text = stringResource(R.string.gate_scan_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.lg))

                if (scanProgress.isScanning && scanProgress.totalFiles > 0) {
                    LinearProgressIndicator(
                        progress = { scanProgress.progressPercent },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = stringResource(
                            R.string.gate_scan_progress,
                            scanProgress.scannedFiles,
                            scanProgress.totalFiles
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
