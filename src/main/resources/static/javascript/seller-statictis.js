document.addEventListener("DOMContentLoaded", function() {
    const table = document.querySelector(".card-body table");
    if (!table) return;

    const btnExportXLSX = document.getElementById("btnExportXLSX");
    const btnExportCSV = document.getElementById("btnExportCSV");

    const exportTable = (filename, bookType) => {
        // Clone table để giữ nguyên hiển thị trên web
        const tableClone = table.cloneNode(true);

        // Tạo workbook và sheet từ table clone
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.table_to_sheet(tableClone, { raw: true }); // raw:true giữ nguyên giá trị hiển thị

        XLSX.utils.book_append_sheet(wb, ws, "Sheet1");
        XLSX.writeFile(wb, filename, { bookType });
    };

    if (btnExportXLSX) {
        btnExportXLSX.addEventListener("click", function() {
            exportTable("statistic.xlsx", "xlsx");
        });
    }

    if (btnExportCSV) {
        btnExportCSV.addEventListener("click", function() {
            exportTable("statistic.csv", "csv");
        });
    }
});
