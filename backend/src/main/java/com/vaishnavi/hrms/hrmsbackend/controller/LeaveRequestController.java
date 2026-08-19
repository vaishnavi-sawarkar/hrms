package com.vaishnavi.hrms.hrmsbackend.controller;

import com.vaishnavi.hrms.hrmsbackend.model.LeaveRequest;
import com.vaishnavi.hrms.hrmsbackend.service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(
            @RequestParam Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String reason
    ) {
        try {
            LeaveRequest leaveRequest = leaveRequestService.applyForLeave(employeeId, startDate, endDate, reason);
            return ResponseEntity.ok(leaveRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ADMIN/HR only — grants leave that bypasses the department capacity block,
    // limited to 2 uses per employee per month (enforced in the service layer).
    @PostMapping("/emergency-override")
    public ResponseEntity<?> applyEmergencyOverride(
            @RequestParam Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String reason,
            @RequestParam String overrideReason,
            @RequestParam Long grantedByApproverId
    ) {
        try {
            LeaveRequest leaveRequest = leaveRequestService.applyEmergencyOverride(
                    employeeId, startDate, endDate, reason, overrideReason, grantedByApproverId);
            return ResponseEntity.ok(leaveRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/decide")
    public ResponseEntity<?> decideLeave(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam boolean approve,
            @RequestParam Long expectedVersion
    ) {
        try {
            LeaveRequest updated = leaveRequestService.decideLeave(id, approverId, approve, expectedVersion);
            return ResponseEntity.ok(updated);
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This leave request was already updated by someone else. Please refresh and try again.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getLeaveHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.getLeaveHistory(employeeId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingRequests() {
        return ResponseEntity.ok(leaveRequestService.getPendingRequests());
    }
}