# EasyMoney

Android app project for loan simulation and related flows.

# Giải thích code
Đọc phần này để biết ý nghĩa cơ bản của các package trong project.

## Các gói có chứa model
gói [.data] chuyên làm nhiệm vụ giao tiếp với bên ngoài (gọi đến backend)
gói [.domain] để chứa code thuần nghiệp vụ không hề liên quan gì đến giao diện. Chỉ chịu trách nhiệm xử lý logic nghiệp vụ.
gói [.ui] chuyên về giao diện

Vậy có 3 loại model tương ứng với 3 gói này:
model <tên>Dto -> gói [.data] chuyên dùng cho Retrofit/Room
model <tên>Model -> gói [.domain] thuần kotlin dùng để tính toán nghiệp vụ
model <tên>UiState -> gói [.ui] dùng khi model ở domain không đủ để hiển thị trên giao diện, ta sẽ bọc nó vào 1 cái UiState

## Repository
Dữ liệu có thể đến từ nhiều nguồn như dữ liệu phải gọi api đến backend, dữ liệu lưu ở thiết bị, dữ liệu trong bộ nhớ tạm của tiến trình,...
Giao diện không cần biết nguồn cũng như cách để lấy dữ liệu đó về ứng dụng, nó chỉ cần biết cách đòi dữ liệu đó từ repository là được

Vậy nên cần xây dựng Repository dùng chung cho cả hệ thống

## Navigation
Dùng jetpack compose kết hợp với NavHost là phương án hiện đại nhất của android để điều hướng giữa các màn hình của ứng dụng
Ta cũng chỉ cần làm một cái Navigation dùng chung cho cả ứng dụng là được

Destination là khai báo chung cho một màn hình, do app của mình có cái giao diện thanh điều hướng gồm nút quay lại, title của trang và nút hướng dẫn
nên tạo ra cái này, 1 destination sẽ có cấu hình của giao diện navbar + route để điều hướng
NavHost là nơi khai báo xem màn hình nào ứng với Destination nào
State là nơi cung cấp công cụ điều hướng:1 stack của các trang đã đi qua để biết đường mà quay lại khi ấn nút,...
1 hàm navigate để điều hướng,...




