const sortDirections = {1: 'asc', 3: 'asc'}; // 1 for Amount, 3 for Date

function sortTable(columnIndex) {
    const table = document.querySelector('table');
    if (!table) return;
    const tbody = table.querySelector('tbody');
    if (!tbody) return;
    const rows = Array.from(tbody.querySelectorAll('tr'));

    // Toggle sort direction
    sortDirections[columnIndex] = sortDirections[columnIndex] === 'asc' ? 'desc' : 'asc';
    const direction = sortDirections[columnIndex];

    // Sort rows
    rows.sort((a, b) => {
        const aText = a.cells[columnIndex].textContent.trim();
        const bText = b.cells[columnIndex].textContent.trim();

        let aVal, bVal;
        if (columnIndex === 1) { // Amount
            aVal = parseInt(aText.replace(/,/g, '')) || 0;
            bVal = parseInt(bText.replace(/,/g, '')) || 0;
        } else if (columnIndex === 3) { // Date
            aVal = new Date(aText);
            bVal = new Date(bText);
        } else {
            aVal = aText;
            bVal = bText;
        }

        if (direction === 'asc') {
            return aVal > bVal ? 1 : aVal < bVal ? -1 : 0;
        } else {
            return aVal < bVal ? 1 : aVal > bVal ? -1 : 0;
        }
    });

    // Re-append sorted rows
    rows.forEach(row => tbody.appendChild(row));

    // Update icon direction (optional, but for visual feedback)
    updateSortIcons(columnIndex, direction);
}

function updateSortIcons(activeColumn, direction) {
    // Reset all icons
    document.querySelectorAll('.sort-icon').forEach(icon => {
        icon.classList.remove('rotate-180');
    });
    // Set active icon
    const ths = document.querySelectorAll('th');
    if (ths[activeColumn]) {
        const icon = ths[activeColumn].querySelector('.sort-icon');
        if (icon && direction === 'desc') {
            icon.classList.add('rotate-180');
        }
    }
}
