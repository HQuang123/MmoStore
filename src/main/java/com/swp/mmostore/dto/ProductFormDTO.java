package com.swp.mmostore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductFormDTO {

    private Integer id; // null nếu là thêm mới, có giá trị nếu là cập nhật

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String title;

    @NotBlank(message = "Mô tả sản phẩm không được để trống")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "1000.00", message = "Giá tối thiểu là 1.000 VNĐ")
    private BigDecimal price;

    private Integer quantity;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer categoryId;

    // Ảnh sản phẩm (upload file)
    private MultipartFile imageFile;

    // Có thể thêm trường này để hiển thị ảnh cũ khi edit
    private String existingImageUrl;

    private Map<String, Object> fields = new HashMap<>();
}
