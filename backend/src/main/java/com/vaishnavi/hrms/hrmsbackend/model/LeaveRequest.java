package com.vaishnavi.hrms.hrmsbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String reason;

    @Column(nullable = false)
    private String status; // "PENDING", "APPROVED", "REJECTED"

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy; // the manager/HR who approved/rejected it, null while pending

    private LocalDateTime appliedAt;
    private LocalDateTime decidedAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean emergencyOverride = false;

    private String overrideReason; // mandatory when emergencyOverride is true, explains why capacity was bypassed

    @Version
    private Long version; // for optimistic locking
}