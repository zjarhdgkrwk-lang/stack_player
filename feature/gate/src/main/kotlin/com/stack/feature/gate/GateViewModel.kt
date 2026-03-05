package com.stack.feature.gate

import android.content.Context
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.stack.core.ui.BaseViewModel
import com.stack.domain.model.ScanProgress
import com.stack.domain.model.SourceFolder
import com.stack.domain.repository.ScanService
import com.stack.domain.repository.SettingsRepository
import com.stack.domain.repository.SourceFolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val sourceFolderRepository: SourceFolderRepository,
    private val scanService: ScanService
) : BaseViewModel<GateState, GateIntent, GateEffect>(GateState()) {

    init {
        viewModelScope.launch {
            val gateCompleted = settingsRepository.isGateCompleted().first()
            if (gateCompleted) {
                emitEffect(GateEffect.NavigateToMain)
            }
        }

        viewModelScope.launch {
            scanService.scanProgress.collect { progress ->
                updateState { copy(scanProgress = progress) }
            }
        }
    }

    override fun dispatch(intent: GateIntent) {
        when (intent) {
            GateIntent.StartOnboarding -> {
                updateState { copy(currentStep = GateStep.MEDIA_ACCESS) }
            }

            GateIntent.RequestMediaPermission -> {
                viewModelScope.launch { emitEffect(GateEffect.RequestMediaPermission) }
            }

            is GateIntent.MediaPermissionResult -> {
                if (intent.granted) {
                    updateState {
                        copy(
                            mediaPermissionGranted = true,
                            permissionDeniedPermanently = false,
                            currentStep = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                GateStep.NOTIFICATION
                            } else {
                                GateStep.FOLDER_SELECT
                            }
                        )
                    }
                } else {
                    updateState {
                        copy(
                            mediaPermissionGranted = false,
                            permissionDeniedPermanently = true
                        )
                    }
                }
            }

            GateIntent.RequestNotificationPermission -> {
                viewModelScope.launch { emitEffect(GateEffect.RequestNotificationPermission) }
            }

            is GateIntent.NotificationPermissionResult -> {
                updateState {
                    copy(
                        notificationPermissionGranted = intent.granted,
                        currentStep = GateStep.FOLDER_SELECT
                    )
                }
            }

            GateIntent.SkipNotificationPermission -> {
                updateState { copy(currentStep = GateStep.FOLDER_SELECT) }
            }

            GateIntent.OpenFolderPicker -> {
                viewModelScope.launch { emitEffect(GateEffect.OpenFolderPicker) }
            }

            is GateIntent.FolderSelected -> {
                viewModelScope.launch {
                    val folder = SourceFolder(
                        id = 0,
                        treeUri = intent.treeUri,
                        displayName = intent.displayName,
                        addedAt = System.currentTimeMillis()
                    )
                    sourceFolderRepository.insertFolder(folder)
                    updateState {
                        copy(
                            selectedFolderName = intent.displayName,
                            folderSelected = true
                        )
                    }
                }
            }

            GateIntent.StartScan -> {
                updateState { copy(currentStep = GateStep.SCANNING, scanError = false) }
                viewModelScope.launch {
                    try {
                        scanService.fullScan()
                        updateState { copy(scanCompleted = true) }
                    } catch (e: Exception) {
                        updateState { copy(scanError = true) }
                    }
                }
            }

            GateIntent.RetryScan -> {
                dispatch(GateIntent.StartScan)
            }

            GateIntent.FinishGate -> {
                viewModelScope.launch {
                    settingsRepository.setGateCompleted(true)
                    emitEffect(GateEffect.NavigateToMain)
                }
            }

            GateIntent.OpenAppSettings -> {
                viewModelScope.launch { emitEffect(GateEffect.OpenAppSettings) }
            }
        }
    }

}

enum class GateStep {
    WELCOME,
    MEDIA_ACCESS,
    NOTIFICATION,
    FOLDER_SELECT,
    SCANNING
}

data class GateState(
    val currentStep: GateStep = GateStep.WELCOME,
    val mediaPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false,
    val permissionDeniedPermanently: Boolean = false,
    val folderSelected: Boolean = false,
    val selectedFolderName: String? = null,
    val scanProgress: ScanProgress = ScanProgress(),
    val scanCompleted: Boolean = false,
    val scanError: Boolean = false
)

sealed interface GateIntent {
    data object StartOnboarding : GateIntent
    data object RequestMediaPermission : GateIntent
    data class MediaPermissionResult(val granted: Boolean) : GateIntent
    data object RequestNotificationPermission : GateIntent
    data class NotificationPermissionResult(val granted: Boolean) : GateIntent
    data object SkipNotificationPermission : GateIntent
    data object OpenFolderPicker : GateIntent
    data class FolderSelected(val treeUri: String, val displayName: String) : GateIntent
    data object StartScan : GateIntent
    data object RetryScan : GateIntent
    data object FinishGate : GateIntent
    data object OpenAppSettings : GateIntent
}

sealed interface GateEffect {
    data object RequestMediaPermission : GateEffect
    data object RequestNotificationPermission : GateEffect
    data object OpenFolderPicker : GateEffect
    data object OpenAppSettings : GateEffect
    data object NavigateToMain : GateEffect
}
