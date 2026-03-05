package com.stack.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val isSystem: Boolean = false,
    val systemType: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
