package com.swp.mmostore.controller;

import com.swp.mmostore.entity.BlogCategory;
import com.swp.mmostore.entity.BlogComment;
import com.swp.mmostore.entity.BlogPost;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BlogController {
    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private BlogCategoryService blogCategoryService;

    @Autowired
    private BlogCommentService blogCommentService;

    @Autowired
    private CloudStorageService cloudStorageService;


    @GetMapping("/blog/blog-view")
    public String viewBlog(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "1") int page, // 1-based
            Model model
    ) {
        int pageSize = 2;

        title = (title == null || title.isBlank()) ? null : title;
        category = (category == null || category.isBlank()) ? null : category;

        // Lấy tổng số bài viết để tính số trang
        long totalPosts = blogPostService.countActivePosts(title, category);
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);

        // Giới hạn page từ 1 đến totalPages
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;

        int currentPage = page - 1; // Spring Page index từ 0
        Page<BlogPost> blogPostsPage = blogPostService.searchActivePosts(title, category, currentPage, pageSize);
        List<BlogPost> posts = blogPostsPage.getContent();

        model.addAttribute("blogPosts", posts);
        model.addAttribute("blogPostsPage", blogPostsPage);

        Map<Integer, List<?>> postCommentsMap = new HashMap<>();
        for (BlogPost post : posts) {
            List<?> comments = blogCommentService.getParentCommentsByPost(post);
            postCommentsMap.put(post.getId(), comments != null ? comments : new ArrayList<>());
        }
        model.addAttribute("postCommentsMap", postCommentsMap);
        model.addAttribute("categories", blogCategoryService.getAllCategories());
        model.addAttribute("title", title != null ? title : "");
        model.addAttribute("category", category != null ? category : "");
        model.addAttribute("currentPage", page); // page 1-based cho view

        return "blog/blog-view";
    }




    @GetMapping("/blog/create")
    public String showCreateBlogForm(Model model) {
        // Tạo object rỗng để binding form
        model.addAttribute("blogPost", new BlogPost());

        // Lấy current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !"anonymousUser".equals(auth.getName())) {
            User user = userService.getUserByEmail(auth.getName());
            model.addAttribute("currentLoggedInUserDetails", user);
        }

        // Load category list
        List<BlogCategory> categories = blogCategoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "blog/blog-create"; // Thymeleaf template: templates/blog/create.html
    }

    @PostMapping("/blog/create")
    public String createBlog(@ModelAttribute BlogPost blogPost,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !"anonymousUser".equals(auth.getName())) {
            User user = userService.getUserByEmail(auth.getName());
            blogPost.setUser(user);
            blogPost.setStatus(0); // 0 = chờ duyệt

            // Upload ảnh bài viết (nếu có)
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imageUrl = cloudStorageService.uploadFile(imageFile);
                    blogPost.setImageUrl(imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải ảnh lên!");
                    return "redirect:/blog/blog-view";
                }
            }

            // Gán danh mục mặc định nếu chưa có
            if (blogPost.getCategory() == null) {
                blogPost.setCategory(blogCategoryService.getCategoryByName("Question"));
            }

            blogPostService.saveBlogPost(blogPost);

            // Gửi thông báo sau khi gửi bài
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Bài viết của bạn đã được gửi. Quản trị viên sẽ duyệt trong vòng 12 giờ tới."
            );
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để đăng bài.");
        }

        return "redirect:/blog/blog-view";
    }



    @PostMapping("/blog/comment/{postId}")
    public String addComment(@PathVariable Integer postId,
                             @RequestParam String content,
                             @RequestParam(required = false) Integer parentId,
                             Authentication auth) {

        // Lấy email user đang đăng nhập
        String userEmail = auth.getName();

        // Gọi Service thêm comment/reply
        blogCommentService.addComment(postId, content, userEmail, parentId);

        // Redirect để reload trang và hiển thị comment
        return "redirect:/blog/blog-view";
    }



    @GetMapping("/blog/my-posts")
    public String viewMyPosts(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "1") int page, // default page=1 (1-based)
            @RequestParam(value = "editPostId", required = false) Integer editPostId,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User user = userService.getUserByEmail(auth.getName());

        int pageSize = 2;
        title = (title == null || title.isBlank()) ? null : title;
        category = (category == null || category.isBlank()) ? null : category;

        // Đếm tổng bài viết
        long totalPosts = blogPostService.countActivePostsByUser(user, title, category);
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);

        // Giới hạn page nhập vào
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        int pageIndex = page - 1; // Spring Page index từ 0
        Page<BlogPost> postsPage = blogPostService.searchActivePostsByUser(user, title, category, pageIndex, pageSize);
        List<BlogPost> myPosts = postsPage.getContent();

        model.addAttribute("myPosts", myPosts);
        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page); // 1-based
        model.addAttribute("totalPages", totalPages);

        Map<Integer, List<?>> postCommentsMap = new HashMap<>();
        for (BlogPost post : myPosts) {
            List<?> comments = blogCommentService.getParentCommentsByPost(post);
            postCommentsMap.put(post.getId(), comments != null ? comments : new ArrayList<>());
        }
        model.addAttribute("postCommentsMap", postCommentsMap);
        model.addAttribute("categories", blogCategoryService.getAllCategories());
        model.addAttribute("title", title != null ? title : "");
        model.addAttribute("category", category != null ? category : "");
        model.addAttribute("currentLoggedInUserDetails", user);

        if (editPostId != null) {
            BlogPost postToEdit = blogPostService.findById(editPostId);
            model.addAttribute("postToEdit", postToEdit);
            model.addAttribute("showEditForm", true);
        } else {
            model.addAttribute("postToEdit", null);
            model.addAttribute("showEditForm", false);
        }

        return "blog/my-posts";
    }

    @PostMapping("/blog/delete/{postId}")
    public String deletePost(@PathVariable("postId") Integer postId,
                             RedirectAttributes redirectAttributes) {
        BlogPost post = blogPostService.findById(postId);
        if (post != null) {
            post.setIsActive(false); // hoặc 0 nếu là Integer
            blogPostService.save(post);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bài viết thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bài viết không tồn tại!");
        }
        return "redirect:/blog/my-posts";
    }

    // Hiển thị form edit bài viết
    @GetMapping("/blog/edit/{id}")
    public String editPostForm(@PathVariable int id, Model model) {
        BlogPost post = blogPostService.getPostById(id); // lấy bài viết theo id
        List<BlogCategory> categories = blogCategoryService.getAllCategories(); // load danh sách category
        model.addAttribute("postToEdit", post);
        model.addAttribute("categories", categories);

        return "blog/edit-post"; // tên template Thymeleaf
    }

    // Xử lý submit form edit bài viết
    @PostMapping("/blog/edit/{id}")
    public String updatePost(
            @PathVariable int id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam int categoryId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            blogPostService.updatePost(id, title, content, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bài viết thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật bài viết thất bại: " + e.getMessage());
        }
        return "redirect:/blog/my-posts";
    }



}
