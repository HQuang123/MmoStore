// Handle form submission
document.getElementById("itemForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const productId = document.getElementById("productSelect").value;
    if (!productId) {
        showMessage("Please select a product first.", "danger");
        return;
    }

    const inputs = document.querySelectorAll("#fieldsContainer input");
    const fields = {};
    inputs.forEach(i => fields[i.name] = i.value);

    const payload = {
        productId: productId,
        fields: fields
    };

    try {
        const res = await fetch("/api/product-list/seller/items/save", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const result = await res.json();
        if (res.ok) {
            showMessage(result.message || "Item created successfully!", "success");
            document.getElementById("itemForm").reset();
            document.getElementById("fieldsContainer").innerHTML = "<p class='text-muted'>Please select a product to load custom fields.</p>";
        } else {
            showMessage(result.error || "Error saving item.", "danger");
        }
    } catch (err) {
        console.error(err);
        showMessage("Unexpected error occurred.", "danger");
    }
});

function showMessage(text, type) {
    const box = document.getElementById("messageBox");
    box.className = `alert alert-${type}`;
    box.textContent = text;
    box.classList.remove("d-none");
}