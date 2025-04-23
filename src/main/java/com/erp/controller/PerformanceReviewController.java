package com.erp.controller;

import com.erp.dto.PerformanceReviewDto;
import com.erp.entity.PerformanceReview;
import com.erp.service.PerformanceReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance-reviews")
public class PerformanceReviewController {

    @Autowired
    private PerformanceReviewService reviewService;

    @PreAuthorize("hasRole('HR')")
    @PostMapping
    public ResponseEntity<PerformanceReviewDto> create(@RequestBody PerformanceReviewDto dto) {
        PerformanceReview review = reviewService.createPerformanceReview(dto);
        return ResponseEntity.ok().body(
                PerformanceReviewDto.builder()
                        .id(review.getId())
                        .employeeId(review.getEmployee().getId())
                        .reviewDate(review.getReviewDate())
                        .performanceRating(review.getPerformanceRating())
                        .comments(review.getComments())
                        .build()
        );
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping
    public ResponseEntity<List<PerformanceReviewDto>> getAll() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceReviewDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReviewDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(reviewService.getReviewsByEmployee(employeeId));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my-reviews")
    public ResponseEntity<List<PerformanceReviewDto>> getMyReviews(Authentication auth) {
        return ResponseEntity.ok(reviewService.getReviewsByEmployeeEmail(auth.getName()));
    }
}
