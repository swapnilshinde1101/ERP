package com.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate date;

    private boolean present;
    

    private String remarks; // optional notes

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // e.g., "PRESENT", "ABSENT", "LEAVE", "NOT_MARKED"

    private boolean overtime;  // Assuming you want to store overtime status.
    
    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LEAVE,
        HALF_DAY,
        NOT_MARKED;

        public boolean isCountedAsPresent() {
            return this == PRESENT || this == HALF_DAY;
        }
    }


    public boolean isOvertime() {
        return overtime;
    }
}
