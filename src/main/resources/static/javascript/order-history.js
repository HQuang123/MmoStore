document.addEventListener("DOMContentLoaded", function() {
    const formatCurrencyInput = (input) => {
        input.addEventListener("input", function() {
            // Chỉ giữ lại ký tự số
            let value = this.value.replace(/\D/g, '');
            if (value) {
                // Định dạng dấu phẩy ngăn cách nghìn
                this.value = Number(value).toLocaleString('vi-VN');
            } else {
                this.value = '';
            }
        });
    };

    // Tìm input theo id
    const minInput = document.getElementById("minTotal");
    const maxInput = document.getElementById("maxTotal");
    if (minInput) formatCurrencyInput(minInput);
    if (maxInput) formatCurrencyInput(maxInput);

    // Trước khi submit form → loại bỏ dấu phẩy
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
