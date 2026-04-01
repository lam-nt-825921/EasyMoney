# Tài liệu này nhằm hướng dẫn sửa lại cấu trúc file của project sao sạch và chuẩn nhất

## Phân chia model

Có 3 layer là data, domain và Ui là có model
cái của data là Dto, domain là Model, Ui là UiState chỉ khi cần thêm dữ kiện quản lý trạng thái (dùng Domain Model + các biến khác quản lý trạng thái giao diện)

### Thực trạng project
hiện tại project chưa phân rõ vị chí của các file model, vẫn còn dùng chồng chéo file model, thậm chí dùng cả parcelable
vấn đề là đang dùng jetpackcompose + navhost trên 1 activity duy nhất nên không cần phải như vậy

### Phương án giải quyết
Domain Model tập trung ở domain.model
Data Model để ở data.model, xóa LoanPackage và OnboardingModels hiện đang sai ở đấy đi, chuyển model đúng ở .data.remote.dto lên
Xóa .data.remote.mock đi

Ui Model thì Ui nào dùng thì tự khai báo ở package của Ui đó



