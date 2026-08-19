package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.model.LeaveRequest;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.LeaveRequestRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class LeaveRequestService {

    private static final int MAX_OVERLAPPING_LEAVE_PER_DEPARTMENT = 2;
    private static final int MAX_EMERGENCY_OVERRIDES_PER_MONTH = 2;

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                               EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    public LeaveRequest applyForLeave(Long employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        // Capacity check: block if too many people in the same department are already
        // approved for overlapping leave in this date range.
        if (employee.getDepartment() != null) {
            long overlapping = leaveRequestRepository.countOverlappingApprovedLeaveInDepartment(
                    employee.getDepartment().getId(), startDate, endDate);

            if (overlapping >= MAX_OVERLAPPING_LEAVE_PER_DEPARTMENT) {
                throw new RuntimeException(
                        "Leave capacity reached for your department during these dates (" +
                                overlapping + " team member(s) already on approved leave). " +
                                "Please choose different dates, or contact HR about an emergency override."
                );
            }
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .status("PENDING")
                .appliedAt(LocalDateTime.now())
                .build();

        return leaveRequestRepository.save(leaveRequest);
    }

    // Admin/HR-only: bypasses the capacity check above, but enforces a monthly limit
    // per employee so it can't become a routine workaround.
    public LeaveRequest applyEmergencyOverride(Long employeeId, LocalDate startDate, LocalDate endDate,
                                               String reason, String overrideReason, Long grantedByApproverId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        if (overrideReason == null || overrideReason.isBlank()) {
            throw new RuntimeException("An override reason is required for emergency leave overrides.");
        }

        LocalDateTime startOfMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();

        long usedThisMonth = leaveRequestRepository.countEmergencyOverridesSince(employeeId, startOfMonth);

        if (usedThisMonth >= MAX_EMERGENCY_OVERRIDES_PER_MONTH) {
            throw new RuntimeException(
                    "This employee has already used their emergency override limit (" +
                            MAX_EMERGENCY_OVERRIDES_PER_MONTH + " per month). No further overrides can be granted until next month."
            );
        }

        Employee approver = employeeRepository.findById(grantedByApproverId)
                .orElseThrow(() -> new RuntimeException("Approver not found: " + grantedByApproverId));

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .status("APPROVED") // emergency overrides are auto-approved by the admin granting them
                .appliedAt(LocalDateTime.now())
                .decidedAt(LocalDateTime.now())
                .approvedBy(approver)
                .emergencyOverride(true)
                .overrideReason(overrideReason)
                .build();

        return leaveRequestRepository.save(leaveRequest);
    }

    public LeaveRequest decideLeave(Long leaveRequestId, Long approverId, boolean approve, Long expectedVersion) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found: " + leaveRequestId));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new RuntimeException("This leave request has already been decided");
        }

        if (!leaveRequest.getVersion().equals(expectedVersion)) {
            throw new ObjectOptimisticLockingFailureException(LeaveRequest.class, leaveRequestId);
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found: " + approverId));

        leaveRequest.setStatus(approve ? "APPROVED" : "REJECTED");
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setDecidedAt(LocalDateTime.now());

        return leaveRequestRepository.save(leaveRequest);
    }

    public List<LeaveRequest> getLeaveHistory(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepository.findByStatus("PENDING");
    }
}