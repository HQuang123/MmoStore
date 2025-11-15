package com.swp.mmostore.controller;
import com.swp.mmostore.entity.Notification;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.NotificationService;
import com.swp.mmostore.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import java.util.List;

@ControllerAdvice
public class GlobalHeaderController {

    private final NotificationService notificationService;
    private final UserService userService;

    public GlobalHeaderController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @ModelAttribute
    public void addHeaderNotifications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Ensure user is logged in and not anonymous
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            User user = userService.getUserByEmail(email);

            if (user != null) {
                List<Notification> notifications = notificationService.findTop5ByUserOrderByCreateAtDesc(user);
                long unreadCount = notificationService.countUnreadByUser(user);

                model.addAttribute("notificationHeader", notifications);
                model.addAttribute("unreadCount", unreadCount);
            }
        } else {
            // If no user logged in, still prevent null issues
            model.addAttribute("notificationHeader", List.of());
            model.addAttribute("unreadCount", 0);
        }
    }
}
