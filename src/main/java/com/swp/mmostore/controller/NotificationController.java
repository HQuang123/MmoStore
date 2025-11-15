package com.swp.mmostore.controller;

import com.swp.mmostore.entity.Notification;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.NotificationService;
import com.swp.mmostore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/mark-all-read")
    public String markAllAsRead(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        notificationService.markAllAsRead(email);
        redirectAttributes.addFlashAttribute("success", "Tất cả thông báo đã được đánh dấu là đã đọc!");
        return "redirect:/notifications";
    }

    @GetMapping
    public String viewAllNotifications(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        if (page < 0) {
            return "redirect:/notifications";
        }

        Pageable pageable = PageRequest.of(page, 7); // 7 per page
        Page<Notification> notificationPage = notificationService.findByUserOrderByCreateAtDesc(user, pageable);

        if (notificationPage.getTotalPages() > 0 && page >= notificationPage.getTotalPages()) {
            return "redirect:/notifications";
        }

        if (StringUtils.hasText(status)) {
            notificationPage = notificationService.getNotificationsForUser(email, status, null, pageable);
        } else {
            notificationPage = notificationService.getNotificationsForUser(email, null, null, pageable);
        }

        model.addAttribute("notifications", notificationPage.getContent());
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notificationPage.getTotalPages());
        return "user/notifications-list";
    }

    @PostMapping("/mark-read/{id}")
    public String markRead(@PathVariable("id") Long id,
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        String email = authentication.getName();
        boolean updated = notificationService.markAsRead(email, id);

        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Thông báo đã được đánh dấu là đã đọc!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật thông báo!");
        }

        return "redirect:/notifications";
    }

    @PostMapping("/mark-unread/{id}")
    public String markUnread(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        String email = authentication.getName();
        boolean updated = notificationService.markAsUnread(email, id);

        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Thông báo đã được đánh dấu là chưa đọc!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật thông báo!");
        }

        return "redirect:/notifications";
    }

    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam("selectedIds") List<Long> ids,
                             @RequestParam("action") String action,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        String email = authentication.getName();

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một thông báo!");
            return "redirect:/notifications";
        }

        switch (action) {
            case "mark-read":
                notificationService.markAsReadBulk(email, ids);
                redirectAttributes.addFlashAttribute("success", "Các thông báo đã được đánh dấu là đã đọc!");
                break;
            case "mark-unread":
                notificationService.markAsUnreadBulk(email, ids);
                redirectAttributes.addFlashAttribute("success", "Các thông báo đã được đánh dấu là chưa đọc!");
                break;
            default:
                redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ!");
        }

        return "redirect:/notifications";
    }


}
