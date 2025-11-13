document.addEventListener("DOMContentLoaded", () => {
    // ====== SELECT ALL CHECKBOX FUNCTIONALITY ======
    const selectAll = document.getElementById("selectAll");
    const checkboxes = document.querySelectorAll('input[name="selectedIds"]');

    if (selectAll) {
        selectAll.addEventListener("change", function () {
            checkboxes.forEach(cb => {
                cb.checked = this.checked;
                toggleHighlight(cb);
            });
        });
    }

    // ====== HIGHLIGHT ROW WHEN CHECKED ======
    checkboxes.forEach(cb => {
        cb.addEventListener("change", () => toggleHighlight(cb));
    });

    function toggleHighlight(checkbox) {
        const card = checkbox.closest(".notification-card");
        if (!card) return;

        if (checkbox.checked) {
            card.classList.add("selected");
        } else {
            card.classList.remove("selected");
        }
    }

    // ====== CLICK ANYWHERE TO TOGGLE CHECKBOX ======
    const cards = document.querySelectorAll(".notification-card");

    cards.forEach(card => {
        card.addEventListener("click", function (e) {
            // Prevent double toggle if user actually clicked on a button or checkbox
            if (e.target.tagName === "BUTTON" || e.target.tagName === "FORM" || e.target.type === "checkbox") {
                return;
            }

            const checkbox = card.querySelector('input[name="selectedIds"]');
            if (checkbox) {
                checkbox.checked = !checkbox.checked;
                toggleHighlight(checkbox);
            }
        });
    });

    // ====== BULK ACTION SUBMISSION FIX ======
    const bulkForm = document.querySelector('form[action$="/notifications/bulk-action"]');
    if (bulkForm) {
        bulkForm.addEventListener("submit", function (e) {
            // Remove previously added hidden inputs (avoid duplicates)
            bulkForm.querySelectorAll('input[name="selectedIds"][type="hidden"]').forEach(el => el.remove());

            // Collect checked notification IDs
            const selected = Array.from(checkboxes)
                .filter(cb => cb.checked)
                .map(cb => cb.value);

            // Validate before submit
            if (selected.length === 0) {
                e.preventDefault();
                alert("⚠️ Vui lòng chọn ít nhất một thông báo!");
                return;
            }

            // Add hidden inputs for each selected ID
            selected.forEach(id => {
                const hidden = document.createElement("input");
                hidden.type = "hidden";
                hidden.name = "selectedIds";
                hidden.value = id;
                bulkForm.appendChild(hidden);
            });
        });
    }
});
