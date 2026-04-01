# Tài liệu lên kế hoạch thiết kế repository cho ứng dụng

## Giới thiệu
Tôi đã tạo khung cơ bản ở [.domain.repository] ta sẽ:
- khai báo hàm lấy dữ liệu ở LoanRepository
- cài đặt hàm đó ở LoanRepositoryImpl
- LoanRepositoryModule sẽ tự động cấp 1 cái instance của LoanRepositoryImpl (đang dùng Hilt nên cần gọi đúng)

## Thống nhất thiết kế
Tôi muốn thống nhất một số quy định khi thiết kế giao diện như sau:
- Ui khi cần quản lý các hành động làm thay đổi trạng thái giao diện thì cần:
  + Tạo viewModel quản lý cho từng hoạt động
  + Tạo UiState gom các biến quản lý trạng thái giao diện
  + Mọi truy vấn dữ liệu phải ở viewModel gọi repository để lấy
- Cách gọi repository trong viewModel 
  + Phải thêm @HiltViewModel vào trên class, thêm @Inject constructor
  + Ví dụ:
  ```
    @HiltViewModel
    class <tên_view_model> @Inject constructor(
    private val repository: LoanRepository
    ) : ViewModel() {}
    ```
  - Cách sử dụng view model khi được khai báo như trên
    Dùng viewModel: <kiểu_hilt_view_model> = hiltViewModel()
## Việc cần làm khi thiết kế 1 Ui và cần lấy dữ liệu
Bước 1: thêm khai báo hàm vào [LoanRepository](../app/src/main/java/com/example/easymoney/domain/repository/LoanRepository.kt)
Bước 2: thêm cài đặt hàm vừa khai báo vào [LoanRepositoryImpl](../app/src/main/java/com/example/easymoney/data/repository/LoanRepositoryImpl.kt)]
(lưu ý: Dù là mock data thì cũng tạo hàm và trả về mock data ở đó)
Bước 3: Thiết lập ViewModel như hướng dẫn ở phần **Thống nhất thiết kế** của tài liệu này

