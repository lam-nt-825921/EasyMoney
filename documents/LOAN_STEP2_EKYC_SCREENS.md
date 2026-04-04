# Step 2 - Danh sach man hinh eKYC

Lien ket quay lai plan tong: [LOAN_STEP2_PLAN.md](./LOAN_STEP2_PLAN.md)

## Muc tieu
Liet ke cac man hinh eKYC can co de sau nay map dung luong nghiep vu.

## Cac file de tao
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycIntroScreen.kt`
  - Man hinh huong dan tong quan eKYC, mo ta cac buoc truoc khi bat dau.
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycDocumentCaptureScreen.kt`
  - Man hinh chup giay to (truoc/sau), hien overlay va huong dan.
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycFaceCaptureScreen.kt`
  - Man hinh chup khuon mat/face matching.
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycProcessingScreen.kt`
  - Man hinh cho/he thong dang xu ly ket qua eKYC.
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/EkycResultScreen.kt`
  - Man hinh ket qua eKYC (thanh cong/that bai/cho thu lai).

## Component dung chung rieng cho eKYC
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/components/EkycInstructionCard.kt`
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/components/EkycCameraFrameOverlay.kt`
- `app/src/main/java/com/example/easymoney/ui/loan/information/ekyc/components/EkycCaptureBottomBar.kt`

## Done criteria
- Co du 5 man hinh eKYC tren.
- Moi man hinh co root composable rieng de flow goi duoc.
- Component eKYC tach rieng trong package `ekyc/components`.

## Out-of-scope
- Chua goi SDK camera/face matching that.
- Chua ket noi permission flow chi tiet.
