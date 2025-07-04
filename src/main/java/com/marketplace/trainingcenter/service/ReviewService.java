package com.marketplace.trainingcenter.service;

import com.marketplace.trainingcenter.dto.review.ReviewRequest;
import com.marketplace.trainingcenter.dto.review.ReviewResponse;
import com.marketplace.trainingcenter.model.entity.Review;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(ReviewRequest reviewRequest, Long studentId);
    
    ReviewResponse getReviewById(Long id);
    
    List<ReviewResponse> getReviewsByCourseId(Long courseId);
    
    List<ReviewResponse> getReviewsByStudentId(Long studentId);
    
    ReviewResponse updateReview(Long id, ReviewRequest reviewRequest);
    
    void deleteReview(Long id);
    
    Double getAverageRatingByCourseId(Long courseId);
    
    Review getReviewEntityById(Long id);
    
    boolean hasUserReviewedCourse(Long studentId, Long courseId);
}
