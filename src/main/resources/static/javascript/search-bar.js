// /javascript/search-bar.js
document.addEventListener("DOMContentLoaded", function () {
    const searchForm = document.getElementById("searchForm");
    const searchInput = document.getElementById("searchQuery");

    if (!searchForm) return; // Skip if not found

    searchForm.addEventListener("submit", function (e) {
        e.preventDefault();
        const keyword = searchInput.value.trim();

        if (keyword) {
            // always redirect, no loadProducts() here
            window.location.href = "/search?query=" + encodeURIComponent(keyword);
        } else {
            console.log("Empty search ignored");
        }
    });
});
