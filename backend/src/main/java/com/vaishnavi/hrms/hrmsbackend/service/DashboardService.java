package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.dto.DashboardResponse;
import com.vaishnavi.hrms.hrmsbackend.model.Attendance;
import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.repository.AttendanceRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                            LeaveRequestRepository leaveRequestRepository,
                            AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public DashboardResponse getDashboardData() {
        List<Employee> allEmployees = employeeRepository.findAll();

        // Group employees by department name, counting each
        Map<String, Long> employeesByDepartment = allEmployees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment().getName(),
                        Collectors.counting()
                ));

        long pendingLeaves = leaveRequestRepository.findByStatus("PENDING").size();

        // Count how many employees have an attendance record marked PRESENT today
        LocalDate today = LocalDate.now();
        long presentToday = allEmployees.stream()
                .filter(e -> attendanceRepository.findByEmployeeIdAndDate(e.getId(), today)
                        .map(a -> "PRESENT".equals(a.getStatus()))
                        .orElse(false))
                .count();

        // Last 5 joiners, sorted by date descending
        List<Map<String, Object>> recentJoiners = allEmployees.stream()
                .filter(e -> e.getDateOfJoining() != null)
                .sorted(Comparator.comparing(Employee::getDateOfJoining).reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getFirstName() + " " + e.getLastName());
                    map.put("dateOfJoining", e.getDateOfJoining());
                    return map;
                })
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalEmployees(allEmployees.size())
                .employeesByDepartment(employeesByDepartment)
                .pendingLeaveRequests(pendingLeaves)
                .presentTodayCount(presentToday)
                .recentJoiners(recentJoiners)
                .build();
    }
}
