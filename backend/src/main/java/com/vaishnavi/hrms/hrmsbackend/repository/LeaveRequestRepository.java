package com.vaishnavi.hrms.hrmsbackend.repository;

import com.vaishnavi.hrms.hrmsbackend.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(String status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND lr.employee.department.id = :departmentId AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    long countOverlappingApprovedLeaveInDepartment(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.emergencyOverride = true AND lr.appliedAt >= :sinceDate")
    long countEmergencyOverridesSince(
            @Param("employeeId") Long employeeId,
            @Param("sinceDate") LocalDateTime sinceDate
    );
}