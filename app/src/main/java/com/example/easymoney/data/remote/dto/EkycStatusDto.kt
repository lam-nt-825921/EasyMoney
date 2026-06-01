package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.EKycStatus
import com.google.gson.annotations.SerializedName

/**
 * Workflow #59 — backend `EkycStatusDto` đầy đủ (status, session, document method,
 * verified timestamp, match score). Domain model `EKycStatus` giữ subset cần cho UI
 * hiện tại + optional rich fields để các màn future hiển thị thêm chi tiết.
 */
data class EkycStatusDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("is_identified") val isIdentified: Boolean? = null,
    @SerializedName("missing_documents") val missingDocuments: List<String>? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("document_method") val documentMethod: String? = null,
    @SerializedName("verified_at") val verifiedAt: Long? = null,
    @SerializedName("match_score") val matchScore: Double? = null,
    @SerializedName("face_match_score") val faceMatchScore: Double? = null,
    @SerializedName("document_match_score") val documentMatchScore: Double? = null
)

fun EkycStatusDto.toDomain(): EKycStatus = EKycStatus(
    isIdentified = isIdentified ?: (status.equals("VERIFIED", ignoreCase = true)),
    missingDocuments = missingDocuments.orEmpty(),
    message = message,
    status = status,
    sessionId = sessionId,
    documentMethod = documentMethod,
    verifiedAt = verifiedAt,
    matchScore = matchScore ?: faceMatchScore ?: documentMatchScore
)
