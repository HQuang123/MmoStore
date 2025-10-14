document.addEventListener("DOMContentLoaded", function () {
    // Select all product on the page
    // const productDiv = document.querySelectorAll(".product");
    //
    // productDiv.forEach(product => {
    //     product.style.cursor = "pointer"; // show hand cursor
    //     let productName = product.getAttribute("name");
    //     product.addEventListener("click", function () {
    //         window.location.href = "/product?" + "id=" + productName.split("-")[1];
    //     });
    // });

    document.querySelector(".shop-register").onclick = function () {
        window.location.href = "/product";
    }
});