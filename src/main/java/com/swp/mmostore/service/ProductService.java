package com.swp.mmostore.service;

import com.swp.mmostore.dto.*;
import com.swp.mmostore.entity.Item;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.entity.Rating;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CloudStorageService cloudStorageService;
    @Autowired
    private ItemRepository itemRepository;

    public List<Product> loadAllProduct() {
        return productRepository.findAll();
    }

    public List<Product> findByCategoryId(List<String> categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Page<ProductSummaryDTO> findFilteredProduct(FilterDTO filterDTO) {
        List<ProductSummaryDTO> productList;
        Sort sort = Sort.unsorted();
        // If sort filterd
        if (filterDTO.sortBy() != null) {
            if ("des".equals(filterDTO.sortOrder())) {
                sort = Sort.by(Sort.Direction.DESC, filterDTO.sortBy());
            } else {
                sort = Sort.by(Sort.Direction.ASC, filterDTO.sortBy());
            }
        }

        Pageable pageable = PageRequest.of(filterDTO.page(), filterDTO.pageSize(), sort);
        long total;

        boolean isSearching = filterDTO.keyword() != null && !filterDTO.keyword().trim().isEmpty();

        System.out.println("categories" + filterDTO.categories() + ",keyword" + filterDTO.keyword());
        if (isSearching) {
            List<String> categoryIds = filterDTO.categories() == null || filterDTO.categories().isEmpty() ? null : filterDTO.categories();
            productList = productRepository.findProductByTitle(filterDTO.keyword(), categoryIds, pageable);
            System.out.println("convit" + productList);
            total = productRepository.countByKeywordAndCategories(filterDTO.keyword(), filterDTO.categories());
        } else if (filterDTO.categories() == null || filterDTO.categories().isEmpty()) {
            // ⬅️ Case: no category filter → get all
            productList = productRepository.findAllProduct(pageable);
            total = productRepository.countAllProducts();
        } else {
            // ⬅️ Case: filter by categories
            productList = productRepository.findAllAndFilterProduct(filterDTO.categories(), pageable);
            total = productRepository.countFilteredProducts(filterDTO.categories());
        }

        // No sort
        return new PageImpl<>(productList, pageable, total);
    }

    public Page<ProductSummaryDTO> searchProductByTitle(FilterDTO filterDTO) {
        Sort sort = Sort.unsorted();

        if (filterDTO.sortBy() != null && !filterDTO.sortBy().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.sortOrder())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, filterDTO.sortBy());
        }

        Pageable pageable = PageRequest.of(filterDTO.page(), filterDTO.pageSize(), sort);

        List<ProductSummaryDTO> productList =
                productRepository.findProductByTitle(filterDTO.keyword(), filterDTO.categories(), pageable);

        long total = productRepository.countByKeywordAndCategories(filterDTO.keyword(), filterDTO.categories());

        return new PageImpl<>(productList, pageable, total);
    }

    public Product findById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public List<ProductSummaryDTO> findRelated(Integer id) {
        // Ví dụ: lấy 4 sản phẩm đầu tiên khác sản phẩm hiện tại
        return productRepository.findAllProduct(Pageable.unpaged())
                .stream()
                .filter(p -> !p.id().equals(id))
                .limit(4)
                .toList();
    }

    public ProductDetailDTO findProductDetailById(Integer id) {
        return productRepository.findProductById(id);
    }

    public ShopSummaryDTO findShopSummaryById(Integer id) {
        return shopRepository.findShopId(id);
    }

    public List<RatingDTO> getRatingsByProduct(Integer productId) {
        return ratingRepository.findAllByProductId(productId);
    }

    public Page<ProductSummaryDTO> getFilteredProductsByShop(
            Integer shopId,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        return productRepository.findFilteredProductsByShop(shopId, keyword, category, minPrice, maxPrice, pageable);
    }

    public void saveProduct(ProductFormDTO form, Shop shop) {
        Product product = (form.getId() != null)
                ? productRepository.findById(form.getId()).orElse(new Product())
                : new Product();

        product.setTitle(form.getTitle());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setQuantity(0);
        product.setCategory(categoryRepository.findById(form.getCategoryId()).orElse(null));
        product.setShop(shop);



        // 🔹 Handle dynamic fields (custom attributes)
        if (form.getFields() != null && !form.getFields().isEmpty()) {
            // Clean up: remove empty keys or values
            form.getFields().entrySet().removeIf(e ->
                    (e.getKey() == null || e.getKey().trim().isEmpty()) &&
                            (e.getValue() == null || e.getValue().toString().trim().isEmpty())
            );
            product.setFields(form.getFields());
        } else {
            product.setFields(null);
        }


        // Xử lý upload ảnh
        String shopImageUrl = "https://storage.googleapis.com/mmostore/default-shop.jpg"; // default image;
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            try {
                shopImageUrl = cloudStorageService.uploadFile(form.getImageFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (form.getExistingImageUrl() != null && !form.getExistingImageUrl().isEmpty()) {
            // Giữ lại ảnh cũ nếu không có ảnh mới
            shopImageUrl = form.getExistingImageUrl();
        }

        product.setProductImageUrl(shopImageUrl);
        productRepository.save(product);
    }

    public List<Product> getProductsBySeller(Shop shop) {
        // Find the shop linked to this user
        if (shop == null) {
            return List.of();
        }
        return productRepository.findByShop(shop);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public Map<String, Object> getConvertFields(Map<String, Object> fields) {
        Map<String, Object> formattedFields = new LinkedHashMap<>();

        if (fields != null) {
            for (int i = 0; i < fields.size() / 2; i++) {
                String key = (String) fields.get("newKey" + i);
                String value = (String) fields.get("newValue" + i);

                if (key != null && !key.isBlank()) {
                    formattedFields.put(key, value != null ? value : "");
                }
            }
        }
        return formattedFields;
    }

    public Page<ProductSummaryDTO> findProductsByShopId(Integer shopId, Pageable pageable) {

        List<ProductSummaryDTO> products = productRepository.findProductsByShopId(shopId, pageable);

        long totalProducts = productRepository.countProductsByShopId(shopId);

        return new PageImpl<>(products, pageable, totalProducts);
    }

    public int getNumberOfAvailable(Integer id) {
        return itemRepository.countItemsByProductId(id);
    }

    public List<Map<String, Object>> getItemsAvaiableForSeller(Integer productId) {
        Pageable pageable = Pageable.unpaged();
        List<Item> items = itemRepository.findUnsoldItem(productId, pageable);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Item item : items) {
            Map<String, Object> flat = new LinkedHashMap<>(item.getValue());
            flat.put("Item ID", item.getItemId());
            result.add(flat);
        }
        return result;
    }
}
