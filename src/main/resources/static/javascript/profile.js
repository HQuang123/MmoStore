document.addEventListener("DOMContentLoaded", function () {
    const inputFile = document.getElementById("imageFile");
    const previewImage = document.getElementById("previewImage");

    if (!inputFile || !previewImage) return;

    // Khi người dùng chọn file ảnh mới
    inputFile.addEventListener("change", function (e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (ev) {
                previewImage.src = ev.target.result; // ảnh base64
                previewImage.style.display = "block"; // hiển thị ảnh
            };
            reader.readAsDataURL(file);
        } else {
            // Nếu bỏ chọn file → ẩn preview
            previewImage.src = "#";
            previewImage.style.display = "none";
        }
    });

    // Mỗi lần mở modal, reset preview
    const modal = document.getElementById("editImageModal");
    if (modal) {
        modal.addEventListener("hidden.bs.modal", function () {
            inputFile.value = "";
            previewImage.src = "#";
            previewImage.style.display = "none";
        });
    }
});
document.addEventListener("DOMContentLoaded", function () {
    const deleteBtn = document.querySelector('a.btn-danger[href="/user/delete"]');

    if (deleteBtn) {
        deleteBtn.addEventListener("click", function (e) {
            e.preventDefault(); // chặn hành động mặc định (chuyển trang ngay)

            const confirmDelete = confirm(
                "⚠️ Bạn có chắc chắn muốn xóa tài khoản không?\n\nTài khoản của bạn sẽ bị xóa vĩnh viễn!"
            );

            if (confirmDelete) {
                window.location.href = this.href; // chuyển hướng nếu xác nhận
            }
        });
    }
});
