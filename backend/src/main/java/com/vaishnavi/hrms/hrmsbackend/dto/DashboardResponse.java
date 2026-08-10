package com.vaishnavi.hrms.hrmsbackend.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private long totalEmployees;
    private Map<String, Long> employeesByDepartment; // e.g. {"Engineering": 5, "Sales": 3}
    private long pendingLeaveRequests;
    private long presentTodayCount;
    private List<Map<String, Object>> recentJoiners; // last 5 employees who joined
}
