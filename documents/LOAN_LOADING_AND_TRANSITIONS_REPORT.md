# Báo cáo kỹ thuật: chuyển màn mượt và loading/skeleton trong loan flow
## 1. Mục tiêu của tài liệu
Tài liệu này tổng hợp các kỹ thuật và quy trình hoạt động liên quan đến:
- Chuyển màn mượt trong Android Jetpack Compose.
- Tạo loading / skeleton đúng cách cho mobile app.
- Phân tích vì sao `LoanConfigurationScreen` / `LoanConfigurationSkeleton` có thể tạo cảm giác giật khi quay lại màn hình hoặc khi đi từ `ConfirmInfoScreen` về `LoanConfigurationScreen`.
Phạm vi của báo cáo tập trung vào kỹ thuật, không đi vào nghiệp vụ.
## 2. Hiện trạng quan sát được trong project
Các điểm liên quan trực tiếp trong luồng hiện tại:
- `ui/loan/flow/LoanFlowScreen.kt` đang dùng `AnimatedContent` để đổi giữa các bước.
- `ui/loan/configuration/LoanConfigurationScreen.kt` quyết định hiển thị giữa skeleton và content theo trạng thái loading.
- `ui/loan/configuration/LoanConfigurationSkeleton.kt` dựng một màn hình riêng hoàn chỉnh trong lúc chờ dữ liệu.
- `ui/common/loading/` đang có bộ primitive dùng chung:
  - `UiLoadState.kt`
  - `SkeletonPrimitives.kt`
  - `Shimmer.kt`
  - `SectionSkeleton.kt`
  - `ListSkeleton.kt`
  - `FullScreenBlockingLoading.kt`
Nhận định ban đầu:
- Nếu `LoanConfigurationScreen` thay toàn bộ cây UI giữa `Skeleton` và `Content`, Compose phải tạo lại nhiều node giao diện cùng lúc.
- Nếu bên trong còn có animation của flow chuyển bước, load skeleton, shimmer và back navigation cùng chạy, cảm giác “khựng” sẽ rõ hơn.
- `LoanConfigurationSkeleton` hiện là một màn hình lớn với `Scaffold`, `Column`, `Card`, nhiều `SkeletonBlock`, và shimmer liên tục; đây là ứng viên hợp lý khi nghi ngờ gây nặng lúc dựng lại UI.
## 3. Vì sao loading/skeleton có thể gây lag
### 3.1. Thay đổi cây UI quá lớn
Khi một màn hình chuyển từ:
- skeleton full-screen
sang
- content full-screen
thì Compose phải:
- tháo cây UI cũ,
- tạo cây mới,
- đo lại layout,
- thực hiện recompose nhiều node.
Nếu thao tác này xảy ra cùng lúc với navigation animation hoặc back transition, người dùng sẽ thấy khựng.
### 3.2. Shimmer chạy liên tục
Skeleton đẹp thường đi kèm shimmer. Nhưng shimmer là animation vô hạn, tức là:
- luôn có cập nhật frame,
- luôn có invalidation vùng hiển thị,
- tốn tài nguyên hơn skeleton tĩnh.
Shimmer phù hợp để tạo cảm giác đang tải, nhưng nếu dùng quá rộng hoặc quá nhiều block cùng lúc sẽ làm tăng chi phí render.
### 3.3. Nhiều tầng animation chồng nhau
Trong Compose, nếu đồng thời có:
- `AnimatedContent` / `Crossfade` ở cấp flow,
- `ModalBottomSheet`,
- shimmer skeleton,
- thay đổi state từ ViewModel,
thì các animation có thể chồng nhau và tạo cảm giác không mượt dù từng phần riêng lẻ vẫn đúng.
### 3.4. Root layout thay đổi làm mất ổn định khung hình
Nếu skeleton và content có cấu trúc khác nhau quá nhiều, các phần như:
- top bar,
- padding,
- background,
- bottom bar,
- khoảng trống giữa các khối
có thể bị đổi đột ngột.
Trong mobile UI, một skeleton tốt không chỉ là “đẹp”, mà còn phải giữ được hình học gần giống màn thật để tránh nhảy layout.
## 4. Kỹ thuật tạo loading/skeleton mượt trong Android Compose
### 4.1. Giữ shell ổn định
Nguyên tắc quan trọng nhất:
- giữ phần khung màn hình ổn định,
- chỉ thay phần nội dung thay đổi.
Ví dụ tốt:
- `Scaffold` / top bar / bottom bar giữ nguyên,
- chỉ thay nội dung body giữa loading và content.
Ví dụ nên tránh:
- một composable trả về skeleton hoàn toàn khác cấu trúc so với content,
- đổi cả background, top bar, bottom bar và body cùng lúc.
### 4.2. Skeleton nên mô phỏng đúng final layout
Skeleton hiệu quả nhất khi:
- số block gần giống số vùng thật,
- chiều cao tương đối khớp,
- khoảng cách giữa các khối gần giống UI thật,
- giữ các “điểm neo” layout như title, section, button.
Mục tiêu là giúp người dùng nhìn vào vẫn nhận ra bố cục cuối cùng sẽ như thế nào.
### 4.3. Ưu tiên placeholder cục bộ thay vì full-screen skeleton khi có thể
Có 3 mức độ:
1. **Full-screen blocking loading**
   - dùng khi chưa có gì để hiển thị.
   - phù hợp với entry screen hoặc màn cần tải dữ liệu bắt buộc trước khi cho thao tác.
2. **Section skeleton**
   - chỉ thay những vùng đang chờ dữ liệu.
   - phù hợp với màn form, màn có dữ liệu một phần.
3. **Inline loading**
   - nút bấm, chip, field, dropdown.
   - nhẹ nhất và ít gây xáo trộn nhất.
Với màn form hoặc màn có phần dữ liệu còn lại đã ổn định, section skeleton thường mượt hơn full-screen swap.
### 4.4. Giới hạn shimmer
Shimmer nên được dùng có kiểm soát:
- chỉ trên các vùng thực sự cần tạo cảm giác tải,
- tránh phủ full màn hình nếu không cần thiết,
- tránh skeleton quá chi tiết ở những vùng người dùng không quan sát kỹ.
Nếu muốn tối ưu cảm giác mượt, skeleton tĩnh hoặc ít vùng shimmer có thể tốt hơn shimmer toàn phần.
### 4.5. Dùng trạng thái loading rõ ràng và tách bạch
Tài liệu loading trong project hiện có `UiLoadState` với các trạng thái:
- `Idle`
- `InitialLoading`
- `Refreshing`
- `Submitting`
- `Error`
Đây là hướng tốt vì giúp UI biết chính xác đang ở giai đoạn nào.
Gợi ý kỹ thuật:
- `InitialLoading`: skeleton hoặc loading đầu màn.
- `Refreshing`: có thể giữ content cũ và chỉ làm mờ / show indicator nhẹ.
- `Submitting`: chặn thao tác, hiển thị loading ngắn.
- `Error`: hiển thị thông báo thay vì chỉ đổi skeleton vô hạn.
### 4.6. Tránh render lại toàn bộ màn chỉ vì một phần state đổi
Nếu một field nhỏ đổi mà toàn màn loading state bị toggle, UI sẽ recompose nhiều hơn mức cần thiết.
Mục tiêu là:
- dữ liệu đổi ở đâu thì chỉ cập nhật vùng đó,
- screen shell giữ nguyên,
- state phân vùng rõ.
## 5. Chọn loại loading nào cho từng loại màn hình
### 5.1. Màn tổng quan flow
Với màn như `LoanFlowScreen`:
- nên ưu tiên transition gọn, ít chi tiết, có cảm giác “đi tiếp” rõ ràng.
- Nếu phải loading, thường chỉ cần loading ở nội dung bước đang vào, không nên chặn toàn bộ flow nếu chưa cần.
### 5.2. Màn configuration / form
Với màn form như `LoanConfigurationScreen`:
- skeleton dạng section + field placeholder thường phù hợp hơn full-screen loading thuần.
- lý do: người dùng cần nhìn bố cục form sớm để cảm nhận tiến trình.
Nếu dữ liệu khởi tạo rất ít và toàn màn phụ thuộc vào backend, full-screen skeleton vẫn chấp nhận được, nhưng nên giữ shell giống content thật.
### 5.3. Màn chi tiết xác nhận
Với màn tóm tắt / confirm:
- thường chỉ cần block loading ngắn hoặc inline loading ở nút xác nhận.
- không nên dựng skeleton quá nặng nếu dữ liệu đã sẵn trong state.
## 6. Quy trình hoạt động nên áp dụng khi debug jank
### Bước 1: Tái hiện đúng tình huống
- Bấm back từ `LoanConfigurationScreen`.
- Chuyển từ `ConfirmInfoScreen` về `LoanConfigurationScreen`.
- Ghi nhận thời điểm giật: lúc bắt đầu transition, lúc skeleton xuất hiện, hay lúc content thật dựng lại.
### Bước 2: Tách từng lớp ảnh hưởng
Kiểm tra riêng:
- chỉ navigation animation,
- chỉ loading/skeleton,
- chỉ shimmer,
- chỉ dữ liệu mock có delay,
- chỉ back transition.
Mục tiêu là xác định lớp nào tạo ra khựng lớn nhất.
### Bước 3: Profiling theo UI timing
Nên quan sát:
- số lần recomposition,
- thời gian layout/measure,
- thời gian render frame,
- có hay không hiện tượng UI tree thay đổi quá mạnh.
### Bước 4: Đánh giá lại cấu trúc skeleton
Nếu skeleton hiện tại nặng hơn content thật hoặc layout khác quá nhiều, nên:
- giảm số block,
- giữ lại khung màn giống final,
- tránh animation không cần thiết,
- giữ background và spacing ổn định.
### Bước 5: Xác nhận bằng so sánh A/B
So sánh:
- version có skeleton full-screen,
- version dùng skeleton nhẹ hơn,
- version giữ shell và chỉ đổi body.
Cách này giúp biết lag do Compose tree swap hay do animation/loading.
## 7. Khuyến nghị áp dụng cho project hiện tại
### Nên làm
- Giữ `Scaffold`/shell ổn định giữa loading và content.
- Skeleton chỉ mô phỏng vùng đang tải, không dựng lại toàn bộ cấu trúc khác biệt.
- Tách loading thành các mức: full-screen, section, inline.
- Hạn chế dùng shimmer quá rộng.
- Khi cần back/forward step, chỉ để một lớp animation điều khiển chuyển bước.
### Nên tránh
- Dùng skeleton khác bố cục quá nhiều so với content thật.
- Chồng nhiều animation cùng một lúc trên cùng màn.
- Bắt `LoanConfigurationScreen` vừa đóng vai loading screen, vừa là content screen, vừa là transition screen nếu không cần thiết.
- Đổi background / top bar / bottom bar cùng lúc với đổi content.
## 8. Kết luận
Dựa trên hiện trạng của project, nghi ngờ `LoanConfigurationSkeleton` là hợp lý vì đây là màn full-screen skeleton có shimmer và cấu trúc khá lớn. Tuy nhiên, nguyên nhân giật thường không chỉ đến từ skeleton mà từ tổ hợp:
- thay đổi UI tree lớn,
- transition navigation,
- animation lồng nhau,
- recomposition do state đổi,
- và shimmer liên tục.
Giải pháp mượt hơn thường không phải là “loại bỏ skeleton”, mà là:
- giữ shell ổn định,
- dùng placeholder đúng mức,
- đơn giản hóa animation,
- và chỉ cho thay đổi phần thật sự cần thay.
## 9. Tài liệu liên quan
- `documents/LOAN_STEP2_PLAN.md`
- `documents/LOAN_STEP2_EKYC_SCREENS.md`
- `documents/LOAN_STEP2_FORM_SCREENS.md`
- `documents/LOAN_STEP2_VM_UISTATE_PLAN.md`
- `documents/CLEAN_ARCHITECTURE.md`
