package com.swp.mmostore.service;

import com.swp.mmostore.entity.Notification;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.NotificationRepository;
import com.swp.mmostore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService  {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    //find the notification for a user
    public Page<Notification> getNotificationsForUser(String email, String status, String search, Pageable pageable) {
        User user = userRepository.findByEmail(email);
        if (StringUtils.hasText(search)) { //neu co search
            if (StringUtils.hasText(status)) { //neu co status
                return notificationRepository.findByUser_EmailAndStatusAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(email, status, search, pageable); //tim notification
            } else {
                return notificationRepository.findByUser_EmailAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(email, search, pageable);//neu ko co status
            }
        } else {
            if (StringUtils.hasText(status)) {
                return notificationRepository.findByUserAndStatusOrderByCreateAtDesc(user, status, pageable);
            } else {
                return notificationRepository.findByUserOrderByCreateAtDesc(user, pageable);
            }
        }
    }

    @Async("notificationExecutor")
    public void createNotificationForUser(Integer userId, String title, String content) {
        User user = null;
        try {
            user = userRepository.findById(userId).orElse(null);
        } catch (Exception ignored) {}
        if (user == null) {
            // can't create notification without user reference; bail silently
            return;
        }
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setContent(content);
        n.setStatus("Unread");
        n.setCreateAt(new Date());
        n.setUpdatedAt(new Date());
        notificationRepository.save(n);
    }

    //tao notification cho tat ca user co role
    @Async("notificationExecutor")
    public void createNotificationForRole(String role, String title, String content) {
        try {
            List<User> users = userRepository.findAllByRoleIgnoreCaseAndIsDeleted(role, false);
            if (users == null || users.isEmpty()) return;
            Date now = new Date();
            for (User user : users) {
                try {
                    Notification n = new Notification();
                    n.setUser(user);
                    n.setTitle(title);
                    n.setContent(content);
                    n.setStatus("Unread");
                    n.setCreateAt(now);
                    n.setUpdatedAt(now);
                    notificationRepository.save(n);
                } catch (Exception ignored) {
                    // continue creating for other users
                }
            }
        } catch (Exception ignored) {
            // best-effort: swallow exceptions to not break caller flow
        }
    }

}
