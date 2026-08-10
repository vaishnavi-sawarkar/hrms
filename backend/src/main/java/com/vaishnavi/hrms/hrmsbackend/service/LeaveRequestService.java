package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.model.LeaveRequest;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.LeaveRequestRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveRequestService {

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

    public LeaveRequest decideLeave(Long leaveRequestId, Long approverId, boolean approve, Long expectedVersion) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found: " + leaveRequestId));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new RuntimeException("This leave request has already been decided");
        }

        // Manually check the version before saving - this is what @Version protects against
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
