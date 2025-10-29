document.addEventListener("DOMContentLoaded", function () {
    const inputFile = document.getElementById("shopImage");
    const previewImage = document.getElementById("previewImage");

    if (inputFile) {
        inputFile.addEventListener("change", function (e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (ev) {
                    previewImage.src = ev.target.result;
                    previewImage.style.display = "block";
                };
                reader.readAsDataURL(file);
            }
        });
    }
});
