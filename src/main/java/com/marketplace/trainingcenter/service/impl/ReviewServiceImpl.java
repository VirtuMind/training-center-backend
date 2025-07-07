package com.marketplace.trainingcenter.service.impl;

import com.marketplace.trainingcenter.dto.review.ReviewRequest;
import com.marketplace.trainingcenter.dto.review.ReviewResponse;
import com.marketplace.trainingcenter.exception.BadRequestException;
import com.marketplace.trainingcenter.exception.ResourceAlreadyExistsException;
import com.marketplace.trainingcenter.exception.ResourceNotFoundException;
import com.marketplace.trainingcenter.exception.UnauthorizedException;
import com.marketplace.trainingcenter.model.entity.Course;
import com.marketplace.trainingcenter.model.entity.Review;
import com.marketplace.trainingcenter.model.entity.User;
import com.marketplace.trainingcenter.repository.CourseRepository;
import com.marketplace.trainingcenter.repository.EnrollmentRepository;
import com.marketplace.trainingcenter.repository.ReviewRepository;
import com.marketplace.trainingcenter.repository.UserRepository;
import com.marketplace.trainingcenter.service.EnrollmentService;
import com.marketplace.trainingcenter.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest, Long studentId) {
        // Check if student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        // Check if course exists
        Course course = courseRepository.findById(reviewRequest.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", reviewRequest.getCourseId()));
        
        // Check if student is enrolled in the course
        if (!enrollmentService.isStudentEnrolledInCourse(studentId, reviewRequest.getCourseId())) {
            throw new UnauthorizedException("You must be enrolled in the course to leave a review");
        }
        
        // Check if student has already reviewed this course
        Optional<Review> existingReview = reviewRepository.findByStudentIdAndCourseId(studentId, reviewRequest.getCourseId());
        if (existingReview.isPresent()) {
            throw new ResourceAlreadyExistsException("Review", "courseId", reviewRequest.getCourseId().toString());
        }
        
        // Create review
        Review review = Review.builder()
                .student(student)
                .course(course)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .build();
        
        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        Review review = getReviewEntityById(id);
        return mapToReviewResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByCourseId(Long courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByStudentId(Long studentId) {
        // Check if student exists
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("User", "id", studentId);
        }
        
        List<Review> reviews = reviewRepository.findByStudentId(studentId);
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest reviewRequest) {
        Review review = getReviewEntityById(id);
        
        // Ensure the review is for the same course
        if (!review.getCourse().getId().equals(reviewRequest.getCourseId())) {
            throw new BadRequestException("Cannot change the course for an existing review");
        }
        
        // Update review
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        
        Review updatedReview = reviewRepository.save(review);
        return mapToReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = getReviewEntityById(id);
        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByCourseId(Long courseId) {
        // Check if course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "id", courseId);
        }
        
        return reviewRepository.getAverageRatingByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewEntityById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedCourse(Long studentId, Long courseId) {
        return reviewRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent();
    }
    
    // Helper method to map Entity to DTO
    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .studentName(review.getStudent().getFullName())
                .studentName(review.getStudent().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
