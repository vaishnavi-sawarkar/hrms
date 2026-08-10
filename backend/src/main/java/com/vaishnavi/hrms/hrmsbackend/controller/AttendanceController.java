package com.vaishnavi.hrms.hrmsbackend.controller;

import com.vaishnavi.hrms.hrmsbackend.model.Attendance;
import com.vaishnavi.hrms.hrmsbackend.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/checkin/{employeeId}")
    public ResponseEntity<?> checkIn(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.checkIn(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/checkout/{employeeId}")
    public ResponseEntity<?> checkOut(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.checkOut(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<List<Attendance>> getMonthlySummary(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(attendanceService.getMonthlySummary(employeeId, year, month));
    }
}
