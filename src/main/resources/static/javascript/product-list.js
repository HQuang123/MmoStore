var filter = {
    categories: [],
    sortBy: null,
    sortOrder: null,
    page: 0,
    pageSize: 8
};

document.addEventListener("DOMContentLoaded", function () {
    const searchBtn = document.getElementById("buttonFilter");
    const container = document.getElementById("productContainer");
    const pagination = document.querySelector(".pagination");
    searchBtn.addEventListener("click", function () {

        //get list checked category
        filter.categories = Array.from(document.querySelectorAll("input[name='category']:checked"))
            .map(cb => cb.value);
        //reset page before filter
        filter.page = 0;
        loadProducts();
        console.log(filter);

    });

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
                console.log("Filtered products:", products);
                // render product list dynamically here
                const products = data.content || data;
                renderProducts(products);
                if (data.totalPages) renderPagination(data.totalPages, data.number);
            })
            .catch(err => console.error("Error:", err));
    }

    function renderProducts(products) {
        container.innerHTML = "";
        //if no product after return
        if (products.length === 0) {
            container.innerHTML = "<p class='text-center text-muted'>Không có sản phẩm nào</p>";
            return;
        }
        products.forEach(product => {
            const card = document.createElement("div");
            card.className = "card card-product p-3 mb-3";
            card.innerHTML = `
                <div class="row g-3 align-items-center">
                      <div class="col-md-2">
                            <img src="#" alt="product-img">
                      </div>
                      <div class="col-md-10">
                            <h5>${product.title}</h5>
                            <p class="text-muted mb-1">Người bán: ${product.shopName}</p>
                            <p class="mb-1">${product.description}</p>
                            <span class="text-warning">${product.avgRating}/10</span>
                            <p class="fw-bold text-primary mt-2">${product.price}</p>
                      </div>
                </div>
            `;
            container.appendChild(card);
        });
    }

    function renderPagination(totalPages, currentPage) {
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

        // Add click listeners
        pagination.querySelectorAll("a.page-link").forEach(link => {
            link.addEventListener("click", e => {
                e.preventDefault();
                const newPage = parseInt(e.target.dataset.page);
                if (newPage >= 0 && newPage < totalPages) {
                    filter.page = newPage;
                    loadProducts();
                }
            });
        });
    }
});
