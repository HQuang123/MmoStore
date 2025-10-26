document.addEventListener("DOMContentLoaded", function() {

    // --- Modal Elements ---
    const modal = document.getElementById("qrModal");
    const modalSpinner = document.getElementById("modalSpinner");
    const modalImage = document.getElementById("qrCodeImage");
    const modalError = document.getElementById("modalError");
    const btnCancelModal = document.getElementById("btnCancelModal");
    const btnMarkAsPaid = document.getElementById("btnMarkAsPaid");

    // --- State Variable ---
    let currentWithdrawalId = null;

    // --- Event Listeners ---

    // 1. "Approve" button click
    document.querySelectorAll(".btn-show-qr-modal").forEach(button => {
        button.addEventListener("click", function() {
            const id = this.getAttribute("data-id");
            currentWithdrawalId = id; // Save the ID
            showModalForId(id);
        });
    });

    // 2. "Mark as Paid" button click
    btnMarkAsPaid.addEventListener("click", function() {
        if (currentWithdrawalId) {
            confirmPaymentApproval(currentWithdrawalId);
        }
    });

    // 3. "Cancel" button click
    btnCancelModal.addEventListener("click", function() {
        hideModal();
    });

    // 4. Close modal on outside click
    window.onclick = function(event) {
        if (event.target == modal) {
            hideModal();
        }
    }

    // --- Functions ---

    /**
     * NEW: Fetches the QR URL from the backend
     */
    function showModalForId(id) {
        // 1. Reset the modal
        modal.style.display = "flex";
        modalImage.style.display = "none";
        modalError.style.display = "none";
        modalSpinner.style.display = "block";
        btnMarkAsPaid.classList.remove("loading");
        btnMarkAsPaid.disabled = false;
        btnMarkAsPaid.textContent = "Mark as Paid";

        // 2. Fetch the URL from the new @GetMapping
        fetch(`/admin/withdraw/generate-qr/${id}`)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.error || "Unknown server error"); });
                }
                return response.json();
            })
            .then(data => {
                // 3. We got the URL. Set the image source.
                modalImage.src = data.vietQrUrl;

                // 4. Wait for the image to load
                modalImage.onload = () => {
                    modalSpinner.style.display = "none";
                    modalImage.style.display = "block";
                };

                modalImage.onerror = () => {
                    modalSpinner.style.display = "none";
                    modalError.textContent = "Error: Could not load QR image.";
                    modalError.style.display = "block";
                };
            })
            .catch(error => {
                // 5. Handle fetch error
                console.error("Fetch Error:", error);
                modalSpinner.style.display = "none";
                modalError.textContent = "Error: " + error.message;
                modalError.style.display = "block";
            });
    }

    /**
     * REMAINS THE SAME: Calls the @PostMapping to confirm payment
     */
    function confirmPaymentApproval(id) {
        btnMarkAsPaid.classList.add("loading");
        btnMarkAsPaid.disabled = true;
        btnMarkAsPaid.textContent = "Processing...";
        modalError.style.display = "none";

        fetch(`/admin/withdraw/confirm-approval/${id}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" }
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.error || "Unknown server error"); });
                }
                return response.json();
            })
            .then(data => {
                hideModal();
                updateTableRow(id, data.newStatus);
            })
            .catch(error => {
                console.error("Fetch Error:", error);
                modalError.textContent = "Error: " + error.message;
                modalError.style.display = "block";

                btnMarkAsPaid.classList.remove("loading");
                btnMarkAsPaid.disabled = false;
                btnMarkAsPaid.textContent = "Mark as Paid";
            });
    }

    /**
     * REMAINS THE SAME: Updates the table row dynamically
     */
    function updateTableRow(id, newStatus) {
        const button = document.querySelector(`.btn-show-qr-modal[data-id='${id}']`);
        if (!button) return;

        const row = button.closest("tr");
        if (!row) return;

        const statusCell = row.cells[3]; // Adjust index if needed
        if (statusCell) {
            statusCell.innerHTML = `<span class="status-approved">${newStatus}</span>`;
        }

        const actionCell = row.cells[row.cells.length - 1]; // Last cell
        if (actionCell) {
            actionCell.innerHTML = ""; // Remove buttons
        }
    }

    /**
     * REMAINS THE SAME: Hides the modal
     */
    function hideModal() {
        modal.style.display = "none";
        modalImage.src = ""; // Clear image
        currentWithdrawalId = null; // Clear state
    }
});