package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanProviderInfoModel
import com.example.easymoney.domain.model.MyInfoModel
import com.example.easymoney.domain.model.EkycCaptureRequest
import com.example.easymoney.domain.model.EkycCaptureResponse
import java.io.File

interface LoanRepository {
    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel>
    suspend fun getMyPackage(): Resource<LoanPackageModel>
    suspend fun getMyInfo(): Resource<MyInfoModel>
    suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel>

    // ========== eKYC Face Capture ==========
    /**
     * Upload ảnh face + metadata tới backend
     */
    suspend fun captureFace(request: EkycCaptureRequest): Resource<EkycCaptureResponse>
    
    /**
     * Upload ảnh với custom file (fallback)
     */
    suspend fun captureFaceCustom(
        imageFile: File,
        metadataJson: String
    ): Resource<EkycCaptureResponse>
}
