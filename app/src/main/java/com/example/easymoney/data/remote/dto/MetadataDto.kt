package com.example.easymoney.data.remote.dto

data class ApiResponse<T>(
    val status: String,
    val data: T,
    val message: String? = null
)

data class MasterDataMetadataDto(
    val version: String,
    val expiredAt: String,
    val masterData: MasterDataContentDto
)

data class MasterDataContentDto(
    val provinces: List<MasterDataItemDto>,
    val professions: List<MasterDataItemDto>,
    val positions: List<MasterDataItemDto>,
    val educationLevels: List<MasterDataItemDto>,
    val maritalStatuses: List<MasterDataItemDto>,
    val relationships: List<MasterDataItemDto>
)

data class MasterDataItemDto(
    val id: String,
    val name: String,
    val parentId: String? = null
)
