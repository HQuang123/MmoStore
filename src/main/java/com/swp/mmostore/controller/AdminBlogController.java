package com.swp.mmostore.controller;

import com.swp.mmostore.entity.BlogCategory;
import com.swp.mmostore.entity.BlogPost;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.BlogCategoryService;
import com.swp.mmostore.service.BlogPostService;
import com.swp.mmostore.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/blog")
public class AdminBlogController {

    @Autowired
    private BlogPostService blogAdminService;

    @Autowired
    private BlogCategoryService blogAdminCategoryService;

    @Autowired
    private NotificationService notificationService;

    // Trang danh sách bài viết
    @GetMapping("/management-post")
    public String listPosts(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int pageParam, // 1-based cho người dùng
            Model model
    ) {
        int pageSize = 4;

        // Chuyển từ page người dùng (1-based) sang page backend (0-based)
        int page = pageParam - 1;

        // Nếu người dùng nhập page < 1 ⇒ quay về trang đầu
        if (pageParam < 1) {
            page = 0;
            pageParam = 1;
        }

        // Lấy dữ liệu trang hiện tại
        Page<BlogPost> postsPage = blogAdminService.getPostsForAdmin(title, category, status, page, pageSize);

        // Nếu vượt quá tổng số trang thì về trang cuối
        if (postsPage.getTotalPages() > 0 && page >= postsPage.getTotalPages()) {
            page = postsPage.getTotalPages() - 1;
            pageParam = postsPage.getTotalPages(); // hiển thị đúng số người dùng thấy
            postsPage = blogAdminService.getPostsForAdmin(title, category, status, page, pageSize);
        }

        List<BlogCategory> categories = blogAdminCategoryService.getAllCategories();


        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", pageParam); // hiển thị 1-based
        model.addAttribute("title", title);
        model.addAttribute("category", category);
        model.addAttribute("categories", categories);
        model.addAttribute("status", status);

        return "blog/management-post";
    }


    // Duyệt bài viết
    @PostMapping("/approve/{id}")
    public String approvePost(
            @PathVariable int id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            RedirectAttributes redirectAttributes
    ) {
        blogAdminService.approvePost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được duyệt.");

        BlogPost post = blogAdminService.approvePost(id);
        if (post != null) {
            User postOwner = post.getUser();
            if (postOwner != null) {
                String notifTitle = "Bài viết của bạn đã được duyệt";
                String notifMsg = "Bài viết \"" + post.getTitle() + "\" của bạn đã được admin duyệt.";
                notificationService.createNotificationForUser(postOwner.getUserId(), notifTitle, notifMsg);
            }
        }

        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/blog/management-post?page=" + page);
        if (title != null && !title.isBlank()) redirectUrl.append("&title=").append(title);
        if (category != null && !category.isBlank()) redirectUrl.append("&category=").append(category);
        if (status != null) redirectUrl.append("&status=").append(status);

        return redirectUrl.toString();
    }

    @PostMapping("/reject/{id}")
    public String rejectPost(
            @PathVariable int id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            RedirectAttributes redirectAttributes
    ) {
        BlogPost post = blogAdminService.rejectPost(id); // Service setStatus(-1) và trả về post
        redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã bị từ chối.");

        // --- Tạo thông báo cho người đăng bài ---
        User postOwner = post.getUser();
        if (postOwner != null) {
            String notifTitle = "Bài viết của bạn đã bị từ chối";
            String notifMsg = "Bài viết \"" + post.getTitle() + "\" của bạn đã bị admin từ chối.";
            notificationService.createNotificationForUser(postOwner.getUserId(), notifTitle, notifMsg);
        }

        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/blog/management-post?page=" + page);
        if (title != null && !title.isBlank()) redirectUrl.append("&title=").append(title);
        if (category != null && !category.isBlank()) redirectUrl.append("&category=").append(category);
        if (status != null) redirectUrl.append("&status=").append(status);

        return redirectUrl.toString();
    }

    @PostMapping("/delete/{id}")
    public String deletePost(
            @PathVariable int id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            RedirectAttributes redirectAttributes
    ) {
        BlogPost post = blogAdminService.getPostById(id);
        if (post != null && post.getStatus() == 1) {
            post.setStatus(-1); // chuyển sang từ chối / vi phạm
            blogAdminService.save(post);
            redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được chuyển sang từ chối.");

            // --- TẠO NOTIFICATION CHO NGƯỜI ĐĂNG --- //
            User postOwner = post.getUser();
            if (postOwner != null) {
                String notifTitle = "Bài viết của bạn bị đánh dấu vi phạm";
                String notifMsg = "Bài viết \"" + post.getTitle() + "\" của bạn đã bị admin đánh dấu vi phạm.";
                notificationService.createNotificationForUser(postOwner.getUserId(), notifTitle, notifMsg);
            }
        }

        // redirect giữ filter và page
        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/blog/management-post?page=" + page);
        if (title != null && !title.isBlank()) redirectUrl.append("&title=").append(title);
        if (category != null && !category.isBlank()) redirectUrl.append("&category=").append(category);
        if (status != null) redirectUrl.append("&status=").append(status);

        return redirectUrl.toString();
    }


}
