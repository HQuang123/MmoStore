fetch('/api/product-list')
    .then(response => response.json())
    .then(products => {
        const tableBody = document.querySelector('#productTable tbody');
        tableBody.innerHTML = '';

        products.forEach(p => {
            const row = document.createElement('tr');
            row.innerHTML = `
                        <td>${p.id}</td>
                        <td>${p.name}</td>
                        <td>${p.price}</td>
                    `;
            tableBody.appendChild(row);
        });
    })
    .catch(err => console.error('Error loading products:', err));