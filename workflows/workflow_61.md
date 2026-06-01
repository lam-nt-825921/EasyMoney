# Workflow #61 — P6.2: REMOTE cleanup & UI polish sweep

## Status
Completed and committed before Final A. Claude/agents should treat REMOTE cleanup as done unless a new regression is found while implementing later workflows.

## Goal
Đảm bảo UX trong REMOTE mode không bị nhiễm mock/debug behavior, không hiển thị raw HTTP/exception text, và có empty/error states polished.

## Requirements

### Functional
- Audit mọi repository có nhánh `REMOTE` fallback sang sample/mock khi backend fail → đổi sang trả Error state thực.
- Xóa debug labels, raw HTTP status, raw exception text khỏi user-facing screen.
- Polish empty/error states cho:
  - Notifications: "Chưa có thông báo nào".
  - Transactions: "Chưa có giao dịch nào".
  - Cards: action "Thêm thẻ" thay vì trống.
- Mọi error có retry action khi phù hợp.
- KHÔNG cần xóa hết MOCK branch khỏi codebase — chỉ đảm bảo REMOTE mode UX sạch.

### Files likely involved
- `app/src/main/java/com/example/easymoney/domain/repository/*Impl.kt` (audit toàn bộ)
- `app/src/main/java/com/example/easymoney/ui/home/`
- `app/src/main/java/com/example/easymoney/ui/account/`
- `app/src/main/java/com/example/easymoney/ui/notification/`
- `app/src/main/java/com/example/easymoney/ui/history/`
- `app/src/main/java/com/example/easymoney/ui/loan/management/`
- `app/src/main/java/com/example/easymoney/ui/payment/`
- `app/src/main/java/com/example/easymoney/ui/reward/`

### Acceptance criteria
- [x] REMOTE mode không fallback sample data khi backend fail.
- [x] Không hiển thị raw stack trace/HTTP code/debug label trên production screen.
- [x] Empty state có copy thân thiện + action phù hợp.
- [x] Error state có retry button khi phù hợp.
- [x] Build passes: `./gradlew assembleDebug`
- [x] Unit tests pass: `./gradlew test`

## Notes
- Tham chiếu: AGENT_TASKS.md Problem 6.2.
- Chạy sau khi #54, #59, #60 đã xong.
