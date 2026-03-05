package com.stack.domain.model

data class ScanProgress(
    val totalFiles: Int = 0,
    val scannedFiles: Int = 0,
    val isScanning: Boolean = false,
    val currentFile: String? = null
) {
    val progressPercent: Float
        get() = if (totalFiles > 0) scannedFiles.toFloat() / totalFiles else 0f
}
