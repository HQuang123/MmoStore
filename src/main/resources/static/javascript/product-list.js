// ✅ Initialize global filter object
const params = new URLSearchParams(window.location.search);
const keywordFromUrl = params.get("query"); // get ?query= keyword from URL
const categoryFromUrl = params.get("category"); // get ?category= from URL

var filter = {
    categories: categoryFromUrl ? [categoryFromUrl] : [],
    sortBy: null,
    sortOrder: null,
    page: 0,
    pageSize: 8,
    keyword: keywordFromUrl || null // include keyword in filter
};

document.addEventListener("DOMContentLoaded", function () {
    const searchBtn = document.getElementById("buttonFilter");
    const container = document.getElementById("productContainer");
    const pagination = document.querySelector(".pagination");

    // ✅ 1. Load initial products (only if not from /search)

    loadProducts();
    document.querySelectorAll('input[name="category"]').forEach(checkbox => {
        if (categoryFromUrl && categoryFromUrl.includes(checkbox.value)) {
            checkbox.checked = true;
        }
    });

    // ✅ 2. Category filter button click
    if (searchBtn) {
        searchBtn.addEventListener("click", function () {
            filter.categories = Array.from(document.querySelectorAll("input[name='category']:checked"))
                .map(cb => cb.value);
            filter.page = 0;
            loadProducts();
        });
    }

    const searchInput = document.getElementById("searchInput");
    const searchButton = document.getElementById("searchBtn");

    if (searchButton && searchInput) {
        searchButton.addEventListener("click", function() {
            filter.keyword = searchInput.value.trim();
            filter.page = 0;
            loadProducts();
        });

        // Optional: Search on Enter key
        searchInput.addEventListener("keypress", function(e) {
            if (e.key === "Enter") {
                e.preventDefault();
                filter.keyword = searchInput.value.trim();
                filter.page = 0;
                loadProducts();
            }
        });
    }

    // ✅ 3. Load products function
    function loadProducts() {
        fetch("/api/product-list/filter", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(filter)
        })
            .then(response => response.json())
            .then(data => {

                console.log(filter);
                const products = data.content || data;
                renderProducts(products);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Error:", err));
    }

    // ✅ 4. Render product cards
    function renderProducts(products) {
        container.innerHTML = "";
        if (!products || products.length === 0) {
            container.innerHTML = "<p class='text-center text-muted'>Không có sản phẩm nào</p>";
            return;
        }

        products.forEach(product => {
            const card = document.createElement("div");
            card.className = "card card-product p-3 mb-3";
            card.innerHTML = `
                <div class="row g-3 align-items-center" style="cursor: pointer;"
                         onclick="window.location.href=\'/product/${product.id}'">
                      <div class="col-md-2">
                            <img src="${product.productImageUrl}" alt="product-img">
                      </div>
                      <div class="col-md-10">
                            <h5>${product.title}</h5>
                            <p class="text-muted mb-1">Người bán: ${product.shopName}</p>
                            <p class="mb-1">Mô tả: ${product.description}</p>
                            <span class="">Đánh giá: ${product.avgRating}/10</span>
                            <p class="fw-bold text-primary mt-2">Giá sản phẩm: ${formatPrice(product.price)}</p>
                      </div>
                </div>
            `;
            container.appendChild(card);
        });
    }

// ✅ 5. Render pagination
    function renderPagination(totalPages, currentPage) {
        console.log("📄 Rendering pagination - Total Pages:", totalPages, "Current:", currentPage);

        pagination.innerHTML = "";

        const prevDisabled = currentPage === 0 ? "disabled" : "";
        const nextDisabled = currentPage === totalPages - 1 ? "disabled" : "";

        pagination.innerHTML += `
          <li class="page-item ${prevDisabled}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">Trước</a>
          </li>
        `;

        for (let i = 0; i < totalPages; i++) {
            pagination.innerHTML += `
                <li class="page-item ${i === currentPage ? "active" : ""}">
                  <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
                </li>
              `;
        }

        pagination.innerHTML += `
          <li class="page-item ${nextDisabled}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">Tiếp</a>
          </li>
        `;

        // 🧠 Keep keyword when paginating
        pagination.querySelectorAll("a.page-link").forEach(link => {
            link.addEventListener("click", e => {
                e.preventDefault();
                const newPage = parseInt(e.target.dataset.page);
                if (newPage >= 0 && newPage < totalPages) {
                    filter.page = newPage;
                    if (keywordFromUrl && !filter.keyword) {
                        filter.keyword = keywordFromUrl;
                    }
                    loadProducts();
                }
            });
        });
    }

    // ✅ 6. Handle sort buttons
    document.querySelectorAll("button[data-sort-by]").forEach(btn => {
        btn.addEventListener("click", () => {
            const sortBy = btn.getAttribute("data-sort-by");
            const sortOrder = btn.getAttribute("data-sort-order");

            filter.sortBy = sortBy;
            filter.sortOrder = sortOrder;
            filter.page = 0;

            // 🧠 Keep keyword when sorting
            if (keywordFromUrl && !filter.keyword) {
                filter.keyword = keywordFromUrl;
            }

            document.querySelectorAll("button[data-sort-by]").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            loadProducts();
        });
    });

    // ✅ 7. Expose loadProducts globally (for reuse)
    window.loadProducts = loadProducts;

    function formatPrice(price) {
        const hasDecimals = price % 1 !== 0;
        return price.toLocaleString('vi-VN', {
            minimumFractionDigits: hasDecimals ? 2 : 0,
            maximumFractionDigits: hasDecimals ? 2 : 0
        }) + 'đ';
    }
});
