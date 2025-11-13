package com.swp.mmostore.controller;

import com.swp.mmostore.dto.*;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.CategoryRepository;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.service.*;
import com.swp.mmostore.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class  SellerController {

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CloudStorageService cloudStorageService;

    @GetMapping("/seller/statistic")
    public String viewDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        Shop shop = shopService.findByUserId(user.getUserId());
        ShopStatisticDTO dashboard = shopService.getStatisticdData(shop.getShopId());

        Integer shopId = shop.getShopId();
        int pageSize = 1;

        // --- Validate page >= 0 ---
        if (page < 0) page = 0;

        // --- Tạo Pageable tạm để lấy tổng số trang ---
        //Pageable tempPageable = PageRequest.of(0, pageSize);
        Page<ProductSalesDTO> tempPage = shopService.getSoldProductsByShop(shopId, 0, pageSize);
        int totalPages = tempPage.getTotalPages();

        // --- Nếu page vượt quá tổng số trang, set về last page ---
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }

        //Pageable pageable = PageRequest.of(page, pageSize);
        Page<ProductSalesDTO> reportPage = shopService.getSoldProductsByShop(shopId, page, pageSize);

        // --- Add model attributes ---
        model.addAttribute("shop", shop);
        model.addAttribute("reportList", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("seller", user);

        return "seller/statistic";
    }



    @GetMapping("/seller/statistic/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        // Lấy toàn bộ dữ liệu không phân trang
        List<ProductSalesDTO> reportList = shopService.getAllSoldProductsByShop(shop.getShopId());

        // Thiết lập header cho response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "ShopStatistic_" + shop.getName().replaceAll("\\s+", "_") + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Ghi dữ liệu ra file Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Top Selling Products");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Product ID", "Product Name", "Price (VNĐ)", "Quantity Sold", "Total Revenue (VNĐ)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Dữ liệu
            int rowNum = 1;
            for (ProductSalesDTO item : reportList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getProductId());
                row.createCell(1).setCellValue(item.getTitle());
                row.createCell(2).setCellValue(item.getPrice().doubleValue());
                row.createCell(3).setCellValue(item.getTotalQuantitySold());
                row.createCell(4).setCellValue(item.getTotalRevenue().doubleValue());
            }

            // Auto-size cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/seller/orders")
    public String viewShopOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(value = "minQuantity", required = false) Integer minQuantity,
            @RequestParam(value = "minTotal", required = false) BigDecimal minTotal,
            @RequestParam(value = "maxTotal", required = false) BigDecimal maxTotal,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", required = false) String status,
            Model model
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        // --- Chuẩn hóa input ---
        status = (status != null && !status.isBlank()) ? status : null;
        minQuantity = (minQuantity != null && minQuantity > 0) ? minQuantity : null;
        minTotal = (minTotal != null && minTotal.compareTo(BigDecimal.ZERO) > 0) ? minTotal : null;
        maxTotal = (maxTotal != null && maxTotal.compareTo(BigDecimal.ZERO) > 0) ? maxTotal : null;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        // --- Tạo Pageable (Spring index từ 0 nên trừ đi 1) ---
        int pageSize = 2;
        int currentPageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(currentPageIndex, pageSize, Sort.by("createAt").descending());

        // --- Gọi service ---
        Page<ShopOrderHistoryDTO> orderPage = orderService.getFilteredOrders(
                shop.getShopId(),
                minQuantity,
                minTotal,
                maxTotal,
                startDateTime,
                endDateTime,
                status,
                pageable
        );

        int totalPages = orderPage.getTotalPages();

        // --- Nếu page vượt quá totalPages, reset về last page ---
        if (currentPageIndex >= totalPages && totalPages > 0) {
            currentPageIndex = totalPages - 1;
            page = totalPages;
            pageable = PageRequest.of(currentPageIndex, pageSize, Sort.by("createAt").descending());
            orderPage = orderService.getFilteredOrders(
                    shop.getShopId(),
                    minQuantity,
                    minTotal,
                    maxTotal,
                    startDateTime,
                    endDateTime,
                    status,
                    pageable
            );
        }

        // --- Add model attributes ---
        model.addAttribute("shop", shop);
        model.addAttribute("orderList", orderPage.getContent());
        model.addAttribute("currentPage", page); // ⚙️ 1-based
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("minQuantity", minQuantity);
        model.addAttribute("minTotal", minTotal);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);

        model.addAttribute("statuses", DepositStatus.values());
        model.addAttribute("selectedStatus", status);

        return "seller/orders";
    }

    @GetMapping("/seller/products")
    public String viewShopProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createAt").descending());

        // Gọi service để lấy danh sách sản phẩm với điều kiện lọc
        Page<ProductSummaryDTO> productPage = productService.getFilteredProductsByShop(
                shop.getShopId(),
                keyword,
                category,
                minPrice,
                maxPrice,
                pageable
        );

        // Thêm dữ liệu cho view
        model.addAttribute("shop", shop);
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        // Giữ lại giá trị filter
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "seller/products";

    }

    @GetMapping("/seller/products/add")
    public String showAddProductForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());
        model.addAttribute("shop", shop);

        model.addAttribute("productForm", new ProductFormDTO());
        model.addAttribute("categories", categoryRepository.findAll());
        return "seller/create-product-form";
    }

    @GetMapping("/seller/products/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());


        Product product = productRepository.findById(id).orElse(null);
        if (product == null || !product.getShop().getShopId().equals(shop.getShopId())) {
            return "redirect:/seller/products"; // Prevent editing others' products
        }

        ProductFormDTO form = new ProductFormDTO();
        form.setId(product.getProductId());
        form.setTitle(product.getTitle());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setExistingImageUrl(product.getProductImageUrl());
        form.setCategoryId(product.getCategory().getCategoryId());
//        form.setFields(productService.getConvertFields(product.getFields()));
        form.setFields(product.getFields());

        model.addAttribute("shop", shop);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("productForm", form);

        return "seller/create-product-form";
    }

    @PostMapping("/seller/products/save")
    public String saveProduct( @ModelAttribute("productForm") ProductFormDTO form,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ!");
            return "seller/create-product-form";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByEmail(auth.getName());
        Shop shop = shopService.findByUserId(user.getUserId());

        productService.saveProduct(form, shop);

        redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/items/create")
    public String showCreateItemForm(Model model, Principal principal) {

        User user = userService.getUserByEmail(principal.getName());
        Shop shop = shopService.findByUserId(user.getUserId());
        List<Product> products = productService.getProductsBySeller(shop);
        model.addAttribute("products", products);
        return "seller/create-item-form";
    }

    @PostMapping("/seller/items/save")
    public String saveItem(@RequestParam("productId") Integer productId,
                           @RequestParam Map<String, String> params,
                           RedirectAttributes redirectAttributes) {
        Product product = productService.findById(productId);

        // Extract only dynamic fields
        Map<String, Object> valueMap = new HashMap<>();
        for (String key : product.getFields().keySet()) {
            if (params.containsKey(key)) {
                valueMap.put(key, params.get(key));
            }
        }

//        itemService.createItem(product, valueMap);
        redirectAttributes.addFlashAttribute("success", "Item created successfully!");
        return "redirect:/seller/items/create";
    }

    @GetMapping("/seller/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/orders/export")
    public void exportOrdersToExcel(
            @RequestParam(value = "minQuantity", required = false) Integer minQuantity,
            @RequestParam(value = "minTotal", required = false) BigDecimal minTotal,
            @RequestParam(value = "maxTotal", required = false) BigDecimal maxTotal,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", required = false) String status,
            HttpServletResponse response
    ) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        // --- Chuẩn hóa input ---
        status = (status != null && !status.isBlank()) ? status : null;
        minQuantity = (minQuantity != null && minQuantity > 0) ? minQuantity : null;
        minTotal = (minTotal != null && minTotal.compareTo(BigDecimal.ZERO) > 0) ? minTotal : null;
        maxTotal = (maxTotal != null && maxTotal.compareTo(BigDecimal.ZERO) > 0) ? maxTotal : null;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        // --- Lấy toàn bộ dữ liệu không phân trang ---
        List<ShopOrderHistoryDTO> orders = orderService.getFilteredOrdersNoPaging(
                shop.getShopId(),
                minQuantity,
                minTotal,
                maxTotal,
                startDateTime,
                endDateTime,
                status
        );

        // --- Tạo Excel ---
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");
        int rowIdx = 0;

        // Header
        Row header = sheet.createRow(rowIdx++);
        String[] headers = {"Order ID", "Product ID", "Product Name", "Quantity", "Unit Price", "Total", "Order Date", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Data
        for (ShopOrderHistoryDTO dto : orders) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(dto.orderId());
            row.createCell(1).setCellValue(dto.productId());
            row.createCell(2).setCellValue(dto.productTitle());
            row.createCell(3).setCellValue(dto.quantity());
            row.createCell(4).setCellValue(dto.price().toString());
            row.createCell(5).setCellValue(dto.totalPrice().toString());
            row.createCell(6).setCellValue(dto.createAt().toString());
            row.createCell(7).setCellValue(dto.status());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // --- Response ---
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    //CHỈNH SỬA THÔNG TIN SHOP
    @GetMapping("/seller/shop/edit")
    public String editShopForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        Shop shop = shopService.findByUserId(user.getUserId());
        model.addAttribute("shop", shop);
        model.addAttribute("seller", user);
        return "seller/edit-shop";
    }

    @PostMapping("/seller/shop/update")
    public String updateShopInfo(@ModelAttribute Shop updatedShop,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            // Lấy thông tin người dùng hiện tại
            String email = principal.getName();
            User user = userService.getUserByEmail(email);

            // Lấy shop hiện tại của người dùng
            Shop currentShop = shopService.findByUserId(user.getUserId());
            if (currentShop == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy shop của bạn!");
                return "redirect:/seller/statistic";
            }

            // Cập nhật các trường text
            currentShop.setName(updatedShop.getName());
            currentShop.setDescription(updatedShop.getDescription());

            // Nếu có upload ảnh mới thì upload lên GCS
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudStorageService.uploadFile(file);
                currentShop.setShopImageUrl(imageUrl);
            }

            // Lưu lại thông tin vào DB — có thể gây ConstraintViolationException
            shopService.save(currentShop);

            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thành công!");
        }
        catch (jakarta.validation.ConstraintViolationException e) {
            // Lấy tất cả thông báo lỗi validation
            String errorMsg = e.getConstraintViolations().stream()
                    .map(v -> v.getMessage())
                    .collect(Collectors.joining(", "));

            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            return "seller/edit-shop"; // hoặc trang hiển thị form update
        }
        catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Cập nhật thất bại, vui lòng thử lại!");
        }

        return "redirect:/seller/statistic";
    }

    @GetMapping("/seller/products/{id}/items")
    @ResponseBody
    public List<Map<String, Object>> getProductItems(@PathVariable Integer id) {
        // Giả sử product.getItemsJson() trả về JSON dạng List<Map<String, Object>>
        return productService.getItemsAvaiableForSeller(id);
    }



}