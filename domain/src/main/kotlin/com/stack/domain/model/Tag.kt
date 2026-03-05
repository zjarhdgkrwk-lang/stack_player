package com.stack.domain.model

import com.stack.domain.model.enums.SystemTagType

data class Tag(
    val id: Long,
    val name: String,
    val color: Int,
    val isSystem: Boolean = false,
    val systemType: SystemTagType? = null,
    val createdAt: Long,
    val updatedAt: Long
)
