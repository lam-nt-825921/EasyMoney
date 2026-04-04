# Ke hoach Step 2 - Dien thong tin

## Muc tieu
Step 2 duoc chia thanh 2 phan:
1. eKYC
2. Dien thong tin sau khi eKYC hoan tat

Tai giai doan nay, muc tieu chi la lap danh sach man hinh Compose, component dung chung, va khung ViewModel/UiState.

## Tai lieu lien ket
- Danh sach man hinh eKYC: [LOAN_STEP2_EKYC_SCREENS.md](./LOAN_STEP2_EKYC_SCREENS.md)
- Danh sach man hinh form: [LOAN_STEP2_FORM_SCREENS.md](./LOAN_STEP2_FORM_SCREENS.md)
- ViewModel + UiState: [LOAN_STEP2_VM_UISTATE_PLAN.md](./LOAN_STEP2_VM_UISTATE_PLAN.md)
- Quy uoc repository/Hilt da thong nhat: [CREATE_REPOSITORY_PLAN.md](./CREATE_REPOSITORY_PLAN.md)

## Pham vi hien tai
- In scope:
  - Liet ke file screen cho eKYC.
  - Liet ke file screen cho phan dien thong tin.
  - Dinh nghia component bottom sheet dropdown dung chung (nhan list, tra item duoc chon).
  - Chot ten ViewModel va UiState cung trach nhiem.
- Out of scope:
  - Chua mo ta chi tiet nghiep vu field/validation.
  - Chua implement API/repository cho step 2.
  - Chua code ket noi nav that.

## Thu tu thuc hien de code sau nay
1. Tao khung package + root `LoanInformationScreen`.
2. Tao man hinh eKYC.
3. Tao man hinh form sau eKYC.
4. Tao component dung chung (dac biet la bottom sheet dropdown).
5. Tao ViewModel + UiState contracts.

## Tieu chi hoan thanh tai lieu
- Co danh sach file ro rang cho tung nhom (eKYC/form/shared).
- Co mapping ViewModel/UiState theo pham vi trach nhiem, khong chong cheo.
- Co 1 mau bottom sheet dropdown co the tai su dung cho cac field dropdown.
