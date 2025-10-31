document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ edit-shop.js loaded"); // kiểm tra script chạy

    const inputFile = document.getElementById("shopImage");
    const previewImage = document.getElementById("previewImage");

    if (!inputFile || !previewImage) {
        console.error("❌ Không tìm thấy phần tử input hoặc img.");
        return;
    }

    const originalSrc = previewImage.src;

    inputFile.addEventListener("change", function (e) {
        const file = e.target.files[0];
        if (file) {
            console.log("📷 File selected:", file.name);
            const reader = new FileReader();
            reader.onload = function (ev) {
                previewImage.src = ev.target.result;
                previewImage.style.display = "block";
            };
            reader.readAsDataURL(file);
        } else {
            previewImage.src = originalSrc;
        }
    });
});
