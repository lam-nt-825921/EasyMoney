package com.example.easymoney.domain.model

/**
 * Model cho dữ liệu metadata danh mục dùng chung
 */
data class MasterDataMetadata(
    val version: String,
    val expiredAt: String, // ISO 8601 format: 2026-05-15T00:00:00Z
    val provinces: List<MasterDataItem> = emptyList(),
    val professions: List<MasterDataItem> = emptyList(),
    val positions: List<MasterDataItem> = emptyList(),
    val educationLevels: List<MasterDataItem> = emptyList(),
    val maritalStatuses: List<MasterDataItem> = emptyList(),
    val relationships: List<MasterDataItem> = emptyList()
)
