# Agent Tasks - Frontend Loan Configuration Polish

Purpose: this is the only active frontend task list. Completed older tasks are intentionally removed.

Read first:

- `documents/CLAUDE.md`
- `documents/PROJECT_STRUCTURE.md`
- `documents/API_SPEC.md`
- `documents/backend_contract.yaml`
- This file

Assume the implementation agent may only have the Android frontend repository. Backend source is not available to the frontend agent.

## Source Issues Covered

| Issue | Main files to inspect | Expected outcome |
|---|---|---|
| Remove the red `i` info icon beside the insurance checkbox in loan configuration | `app/src/main/java/com/example/easymoney/ui/loan/components/LoanBottomButton.kt` | Insurance row shows checkbox and label only; no red info icon/text marker remains |
| Selected tenor tick in loan configuration bottom sheet is not aligned with unselected controls | `app/src/main/java/com/example/easymoney/ui/loan/configuration/TenorBottomSheet.kt` | Selected and unselected rows keep the right-side control in the same horizontal position |

## 1. Remove Insurance Info Icon

Observed issue:

- In the loan configuration screen, the insurance row shows a red `i` icon after the insurance label.
- Product request: remove that `i`.

Current root cause:

- `LoanBottomButton` imports `Icons.Default.Info` and renders an `Icon` immediately after `loan_bottom_insurance_label`.
- The icon is decorative and has no action, tooltip, or detail screen, so it appears as a stray character/control.

Proposed fix:

- Remove the `Icons.Default.Info` import and the `Icon(...)` block from `LoanBottomButton`.
- Keep the checkbox, insurance label, and existing next-button behavior unchanged.

Acceptance:

- Loan configuration insurance row no longer shows the red `i`.
- Checkbox and label alignment still look clean.
- No behavior changes to insurance selection or next button.

## 2. Align Selected Tenor Tick With Other Rows

Observed issue:

- In the loan configuration tenor bottom sheet, the selected option's tick/check icon is not aligned with the controls on unselected rows.
- Product request: when a tenor is selected, its right-side indicator should stay aligned with the other rows.

Current root cause:

- `TenorBottomSheetContent` uses different trailing components per state:
  - selected row: `Icon(Icons.Default.CheckCircle, Modifier.size(24.dp))`;
  - unselected row: `RadioButton(...)`.
- Material `RadioButton` has its own touch target/minimum size and padding, while the check icon is only `24.dp`; therefore the selected trailing control sits on a different visual axis.

Proposed fix:

- Use a stable trailing-control container with the same size for both selected and unselected rows.
- Recommended options:
  - simplest: use `RadioButton(selected = tenor == selectedTenor, ...)` for both states and let Material handle alignment;
  - or keep the check icon but wrap both the selected icon and unselected radio in the same fixed-size `Box`, e.g. `48.dp`, aligned center.
- Do not let selecting a row change row height, padding, or trailing control X position.

Acceptance:

- Selected tenor indicator aligns with unselected row indicators.
- Switching selection does not cause horizontal jump.
- Row click behavior remains unchanged.
