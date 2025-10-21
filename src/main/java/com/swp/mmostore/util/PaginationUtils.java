package com.swp.mmostore.util;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

public class PaginationUtils {

    public static <T> void addPaginationToModel(Page<T> pageData, Model model, String attributeName) {
        model.addAttribute(attributeName, pageData.getContent());
        model.addAttribute("currentPage", pageData.getNumber());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("hasNext", pageData.hasNext());
        model.addAttribute("hasPrevious", pageData.hasPrevious());
    }
}

