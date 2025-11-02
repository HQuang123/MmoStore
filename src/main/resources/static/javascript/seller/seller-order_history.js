document.addEventListener("DOMContentLoaded", function() {
    const formatCurrencyInput = (input) => {
        if (input.value) {
            let clean = input.value.replace(/\D/g, '');
            input.value = clean ? Number(clean).toLocaleString('vi-VN') : '';
        }
        input.addEventListener("input", function() {
            let value = this.value.replace(/\D/g, '');
            this.value = value ? Number(value).toLocaleString('vi-VN') : '';
            this.selectionStart = this.selectionEnd = this.value.length;
        });
    };

    const minInput = document.getElementById("minTotal");
    const maxInput = document.getElementById("maxTotal");
    if (minInput) formatCurrencyInput(minInput);
    if (maxInput) formatCurrencyInput(maxInput);

    // Submit form → remove ký tự không phải số
    const form = document.querySelector(".filter-form");
    if (form) {
        form.addEventListener("submit", function() {
            [minInput, maxInput].forEach(input => {
                if (input && input.value) {
                    input.value = input.value.replace(/\D/g, '');
                }
            });
        });
    }

});
document.addEventListener("DOMContentLoaded", function () {
    const exportBtn = document.getElementById("btnExportXLSX");

    if (exportBtn) {
        exportBtn.addEventListener("click", function () {
            // Lấy toàn bộ dữ liệu filter từ form
            const form = document.querySelector(".filter-form");

            // Xử lý xóa dấu chấm cho các input tiền
            const minInput = form.querySelector("#minTotal");
            const maxInput = form.querySelector("#maxTotal");
            [minInput, maxInput].forEach(input => {
                if (input && input.value) {
                    input.value = input.value.replace(/\D/g, ''); // chỉ giữ lại số
                }
            });

            // Tạo query string sạch
            const params = new URLSearchParams(new FormData(form));

            // Gửi request export
            window.location.href = "/seller/orders/export?" + params.toString();
        });
    }
});

