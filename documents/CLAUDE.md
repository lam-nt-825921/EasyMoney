# Claude Agent Context - EasyMoney Frontend

This directory is written for an implementation agent, not for end-user documentation. Read these files before editing code:

1. `PROJECT_STRUCTURE.md`
   - Current Android package map, architecture ownership, navigation map, and where each screen lives.
2. `API_SPEC.md`
   - Backend HTTP contract summary for agents that only have this frontend repo.
3. `backend_contract.yaml`
   - Machine-readable backend contract snapshot copied into this repo; use it for endpoint/DTO checks.
4. `backend_openapi.yaml`
   - Raw OpenAPI snapshot; use only when `backend_contract.yaml` lacks request-body detail.
5. `SRS.md`
   - Business meaning of each major screen and workflow.
6. `AGENT_TASKS.md`
   - The only active task list. It contains the latest frontend fix batch for edit-profile validation/selectors and profile avatar update removal.

## Repository Context

- Treat the current repository root as the Android/mobile frontend root.
- All file paths in these documents are relative to the frontend root, for example `app/src/main/java/com/example/easymoney/...`.
- Backend source is not assumed to be available to the implementation agent.
- Backend contract details needed for frontend work are in `documents/backend_contract.yaml` and summarized in `documents/API_SPEC.md`.

Do not invent alternate backend behavior to make frontend code easier. If current frontend assumptions conflict with `documents/backend_contract.yaml` or `documents/API_SPEC.md`, fix the frontend or add a documented frontend problem.

## Agent Rules

- Verify docs against code before relying on them.
- Current product target is a commercial-quality mobile app running against the public REMOTE backend. Treat REMOTE as the production path.
- Do not spend implementation effort on MOCK mode unless the task explicitly asks for it. Never change production UX to preserve MOCK behavior.
- Keep UI work inside `ui/**`, state/event handling inside ViewModels, data access in repositories and remote data sources.
- In `REMOTE` mode, do not return mock success when the backend call fails.
- Remove sandbox/developer entry points from production screens, especially Home. Sandbox code may remain reachable only through non-production/debug-only paths if needed.
- Prefer `/api/v1/**` routes when both legacy and v1 routes exist.
- Protected backend calls require `Authorization: Bearer mock_access_token_{user_id}`. Login/register store `accessToken` in `AppPreferences`; `AuthInterceptor` sends it.
- Public demo backend is `https://easymoney.lamgd.dev/`. Local URLs such as `10.0.2.2` are only for explicit local backend testing.
- Most backend response fields are `snake_case`; Gson is configured with `LOWER_CASE_WITH_UNDERSCORES`, but explicit `@SerializedName` is still safer when a DTO mixes camelCase and snake_case.
- A registered user starts with a wallet account and no payment cards. Never seed or fake a card in `REMOTE` mode to hide `CARD_REQUIRED`/empty-card states.
- Lists that represent time events must be newest first: notifications, transactions, repayments, contracts/events where applicable.
- Use Material3 `MaterialTheme.colorScheme` unless a local design token already exists. Avoid hard-coded production colors. UI/UX changes should feel complete enough for customer demo, not like a debug scaffold.
- The current product direction is in `AGENT_TASKS.md`; do not resurrect older task batches from stale docs or commits.
