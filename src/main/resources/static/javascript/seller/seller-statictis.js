document.addEventListener("DOMContentLoaded", function () {
    const btn = document.getElementById("btnExportXLSX");
    if (btn) {
        btn.addEventListener("click", function () {
            window.location.href = "/seller/statistic/export";
        });
    }
});
