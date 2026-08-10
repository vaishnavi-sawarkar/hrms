package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.model.Attendance;
import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.repository.AttendanceRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public Attendance checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        LocalDate today = LocalDate.now();

        // Prevent duplicate check-in for the same day
        attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .ifPresent(a -> {
                    throw new RuntimeException("Already checked in today");
                });

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkInTime(LocalTime.now())
                .status("PRESENT")
                .build();

        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out today");
        }

        attendance.setCheckOutTime(LocalTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getMonthlySummary(Long employeeId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);
    }
}
