function openItemModal(button) {
    const productId = button.getAttribute("data-product-id");
    const container = document.getElementById("itemTableContainer");
    container.innerHTML = '<p class="text-center">Đang tải dữ liệu...</p>';

    fetch(`/seller/products/${productId}/items`)
        .then(res => {
            if (!res.ok) throw new Error("Không thể tải dữ liệu");
            return res.json();
        })
        .then(data => {
            if (!Array.isArray(data) || data.length === 0) {
                container.innerHTML = '<p class="text-center">Không có item nào</p>';
                return;
            }

            const keys = Object.keys(data[0]); // Các key trong value JSON
            let html = `
        <table class="table table-bordered table-hover align-middle">
          <thead class="table-light">
            <tr>${keys.map(k => `<th>${k}</th>`).join('')}</tr>
          </thead>
          <tbody>
            ${data.map(item =>
                `<tr>${keys.map(k => `<td>${item[k] ?? ''}</td>`).join('')}</tr>`
            ).join('')}
          </tbody>
        </table>
      `;
            container.innerHTML = html;
        })
        .catch(err => {
            container.innerHTML = `<p class="text-danger text-center">${err.message}</p>`;
        });

    const modal = new bootstrap.Modal(document.getElementById("itemModal"));
    modal.show();
}
