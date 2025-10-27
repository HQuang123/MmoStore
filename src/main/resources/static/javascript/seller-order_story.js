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
