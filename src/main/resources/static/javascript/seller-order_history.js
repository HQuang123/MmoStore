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

    // ===================== EXPORT XLSX =====================
    const btnExportXLSX = document.getElementById("btnExportXLSX");
    if (btnExportXLSX) {
        btnExportXLSX.addEventListener("click", function() {
            const table = document.querySelector("table");
            if (!table) {
                alert("Không tìm thấy bảng để xuất!");
                return;
            }

            // Clone bảng để giữ nguyên dữ liệu hiển thị
            const tableClone = table.cloneNode(true);

            // Tạo workbook và sheet từ bảng clone
            const wb = XLSX.utils.book_new();
            const ws = XLSX.utils.table_to_sheet(tableClone, { raw: true }); // raw: true giữ nguyên nội dung hiển thị
            XLSX.utils.book_append_sheet(wb, ws, "Sheet1");

            // Xuất file
            XLSX.writeFile(wb, "export.xlsx");
        });
    }
});
