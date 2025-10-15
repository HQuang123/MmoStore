document.addEventListener("DOMContentLoaded", function () {
    // Select all product on the page
    const productDiv = document.querySelectorAll(".product");

    productDiv.forEach(product => {
        product.style.cursor = "pointer"; // show hand cursor
        let productName = product.getAttribute("name");
        product.addEventListener("click", function () {
            window.location.href = "/product?" + "id=" + productName.split("-")[1];
        });
    });

    document.querySelector(".shop-register").onclick = function () {
        window.location.href = "/product";
    }
});

const formattedInput = document.getElementById('formattedAmount');
const hiddenInput = document.getElementById('amount');
const form = document.getElementById('topupForm');

// Format input as user types
formattedInput.addEventListener('input', () => {
    let value = formattedInput.value.replace(/,/g, '').replace(/[^\d]/g, '');
    if (value) {
        formattedInput.value = Number(value).toLocaleString('en-US');
    } else {
        formattedInput.value = '';
    }
});

// Before submitting: remove commas and set hidden numeric value
form.addEventListener('submit', (e) => {
    let rawValue = formattedInput.value.replace(/,/g, '').trim();
    if (!rawValue || isNaN(rawValue)) {
        e.preventDefault();
        alert('Please enter a valid amount.');
        return;
    }
    hiddenInput.value = rawValue;
});