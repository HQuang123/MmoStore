package com.swp.mmostore.controller;

import com.swp.mmostore.entity.Category;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.CategoryService;
import com.swp.mmostore.service.ShopService;
import com.swp.mmostore.service.UserService;
import com.swp.mmostore.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @Autowired
    ShopService shopService;

    @Autowired
    CategoryService categoryService;

    @GetMapping
    public String viewAdminPage(){
        return "admin/admin-dashboard";
    }

    @GetMapping("/users")
    public String viewUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String keyword,
                            Model model) {

        Page<User> userPage = userService.findPaginatedAndFiltered(page, 10, role, status, keyword);

        // prevent invalid page index
        if (userPage.getTotalPages() > 0 && page >= userPage.getTotalPages()) {
            return "redirect:/admin/users?page=0";
        }

        PaginationUtils.addPaginationToModel(userPage, model, "users");

        // keep filter values for the form
        model.addAttribute("role", role == null ? "" : role);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        return "/admin/user-list";
    }


    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users?page=" + page;
    }

    @GetMapping("/shops")
    public String viewShops(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String status,
                            Model model) {

        // Convert status string to Boolean
        Boolean isDeleted = null;
        if ("inactive".equalsIgnoreCase(status)) isDeleted = true;
        else if ("active".equalsIgnoreCase(status)) isDeleted = false;

        Page<Shop> shopPage = shopService.findPaginatedAndFiltered(page, 10, keyword, isDeleted);

        // Prevent crash when no result
        if (shopPage.getTotalPages() > 0 && page > shopPage.getTotalPages()) {
            return "redirect:/admin/shops?page=0";
        }

        model.addAttribute("shops", shopPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", shopPage.getTotalPages());
        model.addAttribute("hasPrevious", shopPage.hasPrevious());
        model.addAttribute("hasNext", shopPage.hasNext());

        // Keep filter values
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("status", status == null ? "" : status);

        return "/admin/shop-list";
    }


    @PostMapping("/shops/{id}/toggle-status")
    public String toggleShopStatus(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page) {
        shopService.toggleShopStatus(id);
        return "redirect:/admin/shops?page=" + page;
    }

    @GetMapping("/categories")
    public String viewCategories(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String status,
                                 Model model) {

        // Convert status string to Boolean (active/inactive)
        Boolean isDeleted = null;
        if ("inactive".equalsIgnoreCase(status)) isDeleted = true;
        else if ("active".equalsIgnoreCase(status)) isDeleted = false;

        Page<Category> categoryPage = categoryService.findPaginatedAndFiltered(page, 10, keyword, isDeleted);

        // Prevent crash if no results
        if (categoryPage.getTotalPages() > 0 && page > categoryPage.getTotalPages()) {
            return "redirect:/admin/categories?page=0";
        }

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("hasPrevious", categoryPage.hasPrevious());
        model.addAttribute("hasNext", categoryPage.hasNext());

        // Preserve filter/search inputs
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("status", status == null ? "" : status);

        return "/admin/category-list";
    }

    @PostMapping("/categories/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Integer id,
                                       @RequestParam(defaultValue = "0") int page) {
        categoryService.toggleCategoryStatus(id);
        return "redirect:/admin/categories?page=" + page;
    }

    // ✅ Show Add Category form
    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        return "category-form";
    }

    // ✅ Handle Add Category form submission (supports image upload)
    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute("category") Category category,
                              BindingResult result,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            return "/admin/category-form";
        }

        categoryService.saveCategory(category, imageFile);
        redirectAttributes.addFlashAttribute("success", "Category added successfully!");
        return "redirect:/admin/categories";
    }

    // ✅ Show Edit Category form
    @GetMapping("/categories/edit/{id}")
    public String showEditCategoryForm(@PathVariable Integer id, Model model) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);
        return "/admin/category-form";
    }

    // ✅ Handle Edit Category form submission (supports changing image)
    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Integer id,
                               @ModelAttribute("category") Category category,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            return "category-form";
        }

        category.setCategoryId(id);
        categoryService.saveCategory(category, imageFile);
        redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
        return "redirect:/admin/categories";
    }
}
