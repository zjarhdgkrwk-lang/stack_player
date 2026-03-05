package com.stack.domain.model

data class SourceFolder(
    val id: Long,
    val treeUri: String,
    val displayName: String,
    val addedAt: Long,
    val lastScanAt: Long? = null
)
