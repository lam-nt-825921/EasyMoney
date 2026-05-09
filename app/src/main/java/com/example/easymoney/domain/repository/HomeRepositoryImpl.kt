package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.EKycStatus
import kotlinx.coroutines.delay
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor() : HomeRepository {
    override suspend fun getBanners(): Resource<List<Banner>> {
        delay(500)
        return Resource.Success(
            listOf(
                Banner("b1", "https://img.freepik.com/free-vector/horizontal-banner-template-with-finance-concept_23-2149156382.jpg", "Ưu đãi lãi suất 0%", "EVENT", "e1"),
                Banner("b2", "https://img.freepik.com/free-vector/flat-design-business-banner-template_23-2149151551.jpg", "Vay nhanh 24/7", "LOAN", "lp1"),
                Banner("b3", "https://img.freepik.com/free-vector/gradient-finance-horizontal-banner-template_23-2149156381.jpg", "Hạng thành viên mới", "WEB", "https://example.com/loyalty")
            )
        )
    }

    override suspend fun getHotLoans(): Resource<List<LoanProduct>> {
        delay(500)
        return Resource.Success(
            listOf(
                LoanProduct("lp1", "Vay tiêu dùng nhanh", 1.5, 50000000, true, "HOT", "Giải ngân trong 30 phút"),
                LoanProduct("lp2", "Vay tín chấp ưu đãi", 1.2, 100000000, false, "NEW", "Không cần tài sản thế chấp"),
                LoanProduct("lp3", "Vay sinh viên", 0.8, 10000000, true, "CHO BẠN", "Hỗ trợ học phí lãi suất thấp")
            )
        )
    }

    override suspend fun getEKycStatus(): Resource<EKycStatus> {
        delay(300)
        return Resource.Success(
            EKycStatus(
                isIdentified = false,
                missingDocuments = listOf("ID_CARD_FRONT", "ID_CARD_BACK", "FACE_VIDEO"),
                message = "Bạn cần hoàn thiện eKYC để kích hoạt hạn mức 100 triệu"
            )
        )
    }
}
