package com.swp.mmostore.controller;

import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.List;

@Controller
public class  SellerController {

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

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
            @RequestParam(defaultValue = "0") int page,
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

        // --- Tạo Pageable với page >= 0 ---
        if (page < 0) page = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createAt").descending());

        // --- Gọi service 1 lần để lấy Page ---
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
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
            pageable = PageRequest.of(page, pageSize, Sort.by("createAt").descending());
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
        model.addAttribute("currentPage", page);
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
                String imageUrl = cloudStorageService.uploadFile(file); // Dịch vụ upload GCS
                currentShop.setShopImageUrl(imageUrl);
            }

            // Lưu lại thông tin vào DB
            shopService.save(currentShop);

            // Thông báo thành công
            redirectAttributes.addFlashAttribute("successMsg", "Update success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Update fail");
        }

        return "redirect:/seller/statistic";
    }




}