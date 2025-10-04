document.addEventListener("DOMContentLoaded", function () {
    // Select all product on the page
    const productDiv = document.querySelectorAll(".product");

    productDiv.forEach(product => {
        product.style.cursor = "pointer"; // show hand cursor
        product.addEventListener("click", function () {
            window.location.href = "/product?" + "id=" + product.name.split("-")[1];
        });
    });

    document.querySelector(".shop-register").onclick = function () {
        alert("convit");
        window.location.href = "#";
    }
});