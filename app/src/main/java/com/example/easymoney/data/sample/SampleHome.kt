package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.EKycStatus
import com.example.easymoney.domain.model.LoanProduct

val SAMPLE_BANNERS: List<Banner> = listOf(
    Banner(
        "b1",
        "https://img.freepik.com/free-vector/horizontal-banner-template-with-finance-concept_23-2149156382.jpg",
        "Ưu đãi lãi suất 0%",
        "EVENT",
        "e1"
    ),
    Banner(
        "b2",
        "https://img.freepik.com/free-vector/flat-design-business-banner-template_23-2149151551.jpg",
        "Vay nhanh 24/7",
        "LOAN",
        "lp1"
    ),
    Banner(
        "b3",
        "https://img.freepik.com/free-vector/gradient-finance-horizontal-banner-template_23-2149156381.jpg",
        "Hạng thành viên mới",
        "WEB",
        "https://example.com/loyalty"
    )
)

val SAMPLE_HOT_LOANS: List<LoanProduct> = listOf(
    LoanProduct("lp1", "Vay tiêu dùng nhanh", 1.5, 50_000_000, true, "HOT", "Giải ngân trong 30 phút"),
    LoanProduct("lp2", "Vay tín chấp ưu đãi", 1.2, 100_000_000, false, "NEW", "Không cần tài sản thế chấp"),
    LoanProduct("lp3", "Vay sinh viên", 0.8, 10_000_000, true, "CHO BẠN", "Hỗ trợ học phí lãi suất thấp")
)

val SAMPLE_EKYC_STATUS: EKycStatus = EKycStatus(
    isIdentified = false,
    missingDocuments = listOf("ID_CARD_FRONT", "ID_CARD_BACK", "FACE_VIDEO"),
    message = "Bạn cần hoàn thiện eKYC để kích hoạt hạn mức 100 triệu"
)
