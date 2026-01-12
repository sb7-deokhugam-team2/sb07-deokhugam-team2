package com.deokhugam.domain.review. repository;

import com.deokhugam.domain.review. entity.Review;
import org.springframework.data.jpa. repository.JpaRepository;
import java.util. UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {



}
