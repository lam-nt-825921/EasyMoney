package com.example.easymoney.data.remote.dto

/**
 * Workflow #59 — `data` là nullable vì backend trả `null` cho mutating endpoints
 * (`ApiResponse[None]` / `{}` empty body). Mỗi caller phải tự kiểm tra null
 * trước khi map sang domain — hoặc dùng [com.example.easymoney.data.remote.safeApiCall]
 * (tự sinh `Resource.Error` khi `status == success` nhưng `data == null`).
 */
data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
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
