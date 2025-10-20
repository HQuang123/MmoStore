package com.swp.mmostore.dto;

import java.util.List;

public record FilterDTO(
        List<String> categories,
        String sortBy,
        String sortOrder,
        int page,
        int pageSize,
        String keyword
) {
    public FilterDTO {
        if (pageSize == 0) pageSize = 8;
    }

    public FilterDTO() {
        this(null, null, null, 0, 8, null);
    }
}