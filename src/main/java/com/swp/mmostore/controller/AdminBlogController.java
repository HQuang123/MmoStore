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

    @GetMapping("/management-post")
    public String listPosts(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int pageParam,
            Model model
    ) {
        int pageSize = 4;
        int page = pageParam - 1;
        if (page < 0) page = 0;

        // Nếu null, dùng chuỗi rỗng để Thymeleaf không bị null
        title = (title != null) ? title : "";
        category = (category != null) ? category : "";

        Page<BlogPost> postsPage = blogAdminService.getPostsForAdmin(
                title.isEmpty() ? null : title,
                category.isEmpty() ? null : category,
                status,
                page,
                pageSize
        );

        List<BlogCategory> categories = blogAdminCategoryService.getAllCategories();

        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", pageParam);
        model.addAttribute("title", title);
        model.addAttribute("category", category);
        model.addAttribute("categories", categories);
        model.addAttribute("status", status);

        return "blog/management-post";
    }


    @PostMapping("/approve/{id}")
    public String approvePost(@PathVariable int id, RedirectAttributes redirectAttributes) {
        BlogPost post = blogAdminService.approvePost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được duyệt.");

        if (post != null && post.getUser() != null) {
            User postOwner = post.getUser();
            String notifTitle = "Bài viết của bạn đã được duyệt";
            String notifMsg = "Bài viết \"" + post.getTitle() + "\" của bạn đã được admin duyệt.";
            notificationService.createNotificationForUser(postOwner.getUserId(), notifTitle, notifMsg);
        }

        return "redirect:/admin/blog/management-post";
    }


    @PostMapping("/reject/{id}")
    public String rejectPost(
            @PathVariable int id,
            RedirectAttributes redirectAttributes
    ) {
        BlogPost post = blogAdminService.rejectPost(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã bị từ chối.");

        if (post != null && post.getUser() != null) {
            User postOwner = post.getUser();
            String notifTitle = "Bài viết của bạn đã bị từ chối";
            String notifMsg = "Bài viết \"" + post.getTitle() + "\" của bạn đã bị admin từ chối.";
            notificationService.createNotificationForUser(postOwner.getUserId(), notifTitle, notifMsg);
        }

        return "redirect:/admin/blog/management-post";
    }


    @PostMapping("/delete/{id}")
    public String deletePost(
            @PathVariable int id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int currentPage,
            RedirectAttributes redirectAttributes
    ) {
        BlogPost post = blogAdminService.getPostById(id);
        if (post != null && post.getStatus() == 1) {
            post.setStatus(-1);
            blogAdminService.save(post);
            redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được chuyển sang từ chối.");

            if (post.getUser() != null) {
                User postOwner = post.getUser();
                String notifTitle = "Bài viết của bạn bị đánh dấu vi phạm";
                String notifMsg = "Bài viết \"" + post.getTitle() + "\" của bạn đã bị admin đánh dấu vi phạm.";
                notificationService.createNotificationForUser(postOwner.getUserId(), notifTitle, notifMsg);
            }
        }

        return "redirect:/admin/blog/management-post";
    }



}
