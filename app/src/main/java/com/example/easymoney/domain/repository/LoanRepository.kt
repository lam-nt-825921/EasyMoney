package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanProviderInfoModel
import com.example.easymoney.domain.model.MyInfoModel
import com.example.easymoney.domain.model.EkycCaptureRequest
import com.example.easymoney.domain.model.EkycCaptureResponse
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.domain.model.MasterDataItem
import java.io.File

interface LoanRepository {
    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel>
    suspend fun getMyPackage(): Resource<LoanPackageModel>
    suspend fun getMyInfo(): Resource<MyInfoModel>
    suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel>

    // Master Data
    suspend fun getProvinces(): Resource<List<MasterDataItem>>
    suspend fun getDistricts(provinceId: String): Resource<List<MasterDataItem>>
    suspend fun getWards(districtId: String): Resource<List<MasterDataItem>>
    suspend fun getProfessions(): Resource<List<MasterDataItem>>
    suspend fun getPositions(): Resource<List<MasterDataItem>>
    suspend fun getEducationLevels(): Resource<List<MasterDataItem>>
    suspend fun getMaritalStatuses(): Resource<List<MasterDataItem>>
    suspend fun getRelationships(): Resource<List<MasterDataItem>>

    // Application
    suspend fun submitApplication(request: LoanApplicationRequest): Resource<Unit>

    // eKYC

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
