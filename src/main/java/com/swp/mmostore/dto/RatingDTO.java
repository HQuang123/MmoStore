package com.swp.mmostore.dto;

import java.time.LocalDateTime;

public record RatingDTO(Integer ratingId, Integer ratingPoint, String feedback, String username, LocalDateTime createAt) {
}
