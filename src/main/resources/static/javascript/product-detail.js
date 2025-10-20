function openConfirm() {
    const quantity = parseInt(document.getElementById("quantity").value);
    const singlePrice = parseFloat(document.getElementById("price").value);
    const totalPrice = quantity * singlePrice;
    document.getElementById("totalValue").textContent = totalPrice.toLocaleString('vi-VN') + 'đ';
    // // Event when click 'yes'
    const confirmBtn = document.getElementById("confirmOrderBtn");
    confirmBtn.onclick = function() {
        createOrder(quantity, totalPrice);
    };

    const modalConfirm = new bootstrap.Modal(document.getElementById("confirmModal"));
    modalConfirm.show();
}

//create order
function createOrder(quantity, totalPrice) {
    fetch("/user/api/order/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            quantity: quantity,
            totalPrice: totalPrice
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data) {
                const modalConfirm = bootstrap.Modal.getInstance(document.getElementById("confirmModal"));
                modalConfirm.hide();
                document.getElementById("orderQuantity").textContent = data.quantity;
                document.getElementById("orderTotal").textContent = data.totalPrice;
                const modalSuccess = new bootstrap.Modal(document.getElementById("successModal"));
                modalSuccess.show();
            } else {
                alert("Lỗi hệ thống. Vui lòng thử lại sau.")
            }

        })
        .catch(err => console.error("Error:", err));
}