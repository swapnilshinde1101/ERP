package com.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leaves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED,
        HALF_DAY
    }
    public boolean isLeaveApplicable(LocalDate date) {
        return (status == LeaveStatus.APPROVED) && (date != null && !date.isBefore(startDate) && !date.isAfter(endDate));
    }

}
