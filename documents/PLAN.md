# PLAN — Roadmap & Detailed Tasks

Phiên bản này chuyển mô tả ngắn sang kế hoạch công việc (requirements) có thể hành động, phân loại theo mức ưu tiên (P0..P3). Mỗi task có mô tả, tiêu chí nghiệm thu (acceptance criteria), phụ thuộc và gợi ý owner/ước lượng.

## Mục tiêu chung

- Bảo đảm mọi luồng dữ liệu backend -> UI rõ ràng, dễ truy vết và được test.
- Hoàn thiện luồng thanh toán/dòng tiền, đổi điểm, quản lý khoản vay và hệ thống thông báo.
- Cải thiện trải nghiệm: navigation chính xác từ banner, hotline/term link hoạt động, UI responsive, không hard-code string.
- Tạo và duy trì tài liệu kỹ thuật cần thiết cho dev mới và QA.

---

## P0 — Nền tảng (High priority)

1) Guarantee API-first architecture
   - Task: Rà soát toàn bộ UI/ViewModel để chắc chắn không gọi network trực tiếp; tất cả phải đi qua `Repository`.
   - Acceptance:
	 - Danh sách file UI/ViewModel đã audit và report các trường hợp vi phạm.
	 - Tất cả calls được refactor hoặc có `TODO` rõ ràng nếu cần task follow-up.
   - Dependencies: `AppPreferences` (để biết mock/connected mode), test devices.
   - Owner: Core/Platform team. Est: 2–4d.

2) Mock vs Connected mode — stabilize
   - Task: Làm rõ nơi lưu mode (sandbox/AppPreferences), document cách chuyển, và đảm bảo repository tuân theo mode.
   - Acceptance:
	 - Tài liệu ngắn trong `documents/BACKEND_DATA_PATHS.md` mô tả cách bật/tắt mock.
	 - Kiểm tra danh sách flows hoạt động ở cả 2 mode.
   - Owner: Backend integration owner. Est: 1–2d.

3) DTO → Domain mapping audit
   - Task: Kiểm tra tất cả repository implementations, đặc biệt mapping trong `data/remote/*` và `domain/repository/*` để đảm bảo snake_case ↔ camelCase mapping đúng.
   - Acceptance: Unit tests hoặc mapping tests cho các DTO quan trọng (Loan list, Loan detail, Reward, Notification).
   - Owner: Feature owners. Est: 2–5d.

4) Create core docs
   - Task: Bổ sung `documents/BACKEND_DATA_PATHS.md` và `documents/PROJECT_STRUCTURE.md` (đã có khung) với detail endpoint → repository → screen mapping.
   - Acceptance: Draft hoàn chỉnh, link trong `CLAUDE.md`.
   - Owner: Tech writer / Lead. Est: 1–2d.

---

## P1 — Luồng nghiệp vụ cốt lõi (Core flows)

1) Cashflow / Wallet management
   - Task: Thiết kế và triển khai luồng nạp/rút/trừ tiền.
	 - Xác định phương thức thanh toán: sandbox bank (dev), app wallet, hoặc thẻ (card). Nếu cần tích hợp PSP (payment service provider) ghi rõ API.
	 - Implement UI + ViewModel + Repository + Remote endpoints.
   - Acceptance:
	 - Test flow nạp tiền thành công (mock & connected).
	 - Test flow rút tiền với validation, lỗi, và success path.
	 - Logs/trace cho mỗi giao dịch.
   - Dependencies: Backend payment sandbox endpoints, AppPreferences, security checks.
   - Owner: Payment team. Est: 7–14d depending on PSP integration.

2) Reward redemption (đổi điểm)
   - Task: Khi user chọn nhận quà phải mở màn hình xác nhận, gọi API backend trừ điểm, backend trả về payload quà (mã giảm giá / thẻ cào / voucher).
   - Acceptance:
	 - UI confirm dialog/modal hiển thị trước khi gọi API.
	 - Sau API success, hiển thị thông tin quà theo response, lưu vào wallet/reward history.
	 - Xử lý lỗi (không đủ điểm, server error).
   - Dependencies: Reward API endpoints, domain model mapping.
   - Owner: Rewards feature owner. Est: 3–6d.

3) Home banner → Loan management navigation
   - Task: Đảm bảo banner trên Home điều hướng chính xác đến màn quản lý khoản vay.
   - Acceptance:
	 - Click banner opens Loan Management screen.
	 - Deep link và analytics event được ghi lại.
   - Owner: Home/Loan owners. Est: 1d.

4) Loan management: contract listing, cancel, sign (eSign)
   - Task: Màn quản lý khoản vay hiển thị danh sách hợp đồng đã được hệ thống duyệt; user có 2 action: cancel hoặc sign.
	 - Nếu sign → chuyển sang luồng eSign (native/webview depending on backend).
   - Acceptance:
	 - Contracts list fetched and mapped correctly.
	 - Cancel action calls backend and updates UI.
	 - Sign action opens eSign flow and returns status to app.
   - Dependencies: Loan API, eSign integration, auth/token handling.
   - Owner: Loan team. Est: 5–10d.

---

## P2 — Trải nghiệm người dùng & điều hướng

1) Notifications end-to-end
   - Task: Hoàn thiện pipeline: Backend → FCM → App (notification model) → local display → Notification list screen.
   - Acceptance:
	 - FCM token registration is reliable; notifications arrive when app in background/foreground.
	 - Notification list shows items with read/unread and deep link to related screen.
   - Owner: Infra + Notification owner. Est: 3–7d.

2) Highlighted links: hotline / contracts / terms
   - Task: Tìm tất cả vị trí UI có phone number / contract / terms được highlight; gắn intent/route tương ứng.
   - Acceptance:
	 - Phone links open phone dialer with number.
	 - Contract/term links navigate to in-app screens or open external URL as specified.
   - Owner: UX devs. Est: 1–3d.

3) AI Chatbot with navigation components
   - Task: Thiết kế chat component hỗ trợ:
	 - Text responses
	 - Action components (buttons/cards) trong khung chat để điều hướng sang màn hoặc gọi function (ví dụ: Mở Loan Management, Mở FAQ, Gọi hotline).
   - Acceptance:
	 - Demo scenario: bot trả lời và hiển thị 1 component có nút điều hướng; khi bấm sẽ navigate correctly.
   - Dependencies: Chat backend/AI service or mock; navigation routing.
   - Owner: Chat/UX team. Est: 7–14d.

4) UI guidelines enforcement
   - Task: Đảm bảo tất cả màn mới responsive, Material Design, strings trong resources.
   - Acceptance: Peer review + automated lint checks.
   - Owner: UI lead. Est: ongoing.

---

## P3 — QA, Cleanup & Documentation

1) Audit repository coverage
   - Task: Quét lại một lượt xem chỗ nào còn chưa gọi API (UI gọi đến trung gian là `Repository` thôi nên check `Repository` impl có gọi thật không).
   - Acceptance: Report với file list và remediation tasks.
   - Owner: QA/Platform. Est: 2–4d.

2) Documentation and onboarding
   - Task: Hoàn thiện `documents/BACKEND_DATA_PATHS.md` (endpoint mapping, DTO↔domain), `documents/PROJECT_STRUCTURE.md` (ownership), và `CLAUDE.md` sync.
   - Acceptance: Docs reviewed and linked from `CLAUDE.md`.
   - Owner: Tech writer/Lead. Est: 1–3d.

3) Testing & monitoring
   - Task: Thêm integration tests cho critical flows, setup basic monitoring/logging for payments and notifications.
   - Acceptance: Tests green locally; CI integration optional.
   - Owner: QA/DevOps. Est: 3–7d.

---

## Acceptance criteria (Project-level)

- All P0 tasks must be completed before major feature merges.
- Critical flows (payments, reward redemption, loan sign) must have end-to-end tested happy-path in connected mode.
- Documentation for backend paths and project structure available and linked from `CLAUDE.md`.

## How to use this plan

- Developers: pick tasks from P0→P3; update task status in the plan or in your issue tracker.
- Review: every sprint, move items from P1/P2 into sprint backlog when dependencies ready.

## Next steps (immediately)

1. Assign owners for P0 tasks and start the DTO mapping audit.
2. Create issue cards for Payment integration and Reward redemption.
3. Implement a smoke test that verifies repository calls for Loan list and Notification list.

---

_Tài liệu này là bản kế hoạch cấp repo để triển khai. Sau khi task được approve, chuyển vào issue tracker (JIRA/GitHub Issues) và gán nhãn priority/epic._
