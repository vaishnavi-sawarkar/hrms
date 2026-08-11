package com.vaishnavi.hrms.hrmsbackend.controller;

import com.vaishnavi.hrms.hrmsbackend.dto.EmployeeRequest;
import com.vaishnavi.hrms.hrmsbackend.dto.EmployeeResponse;
import com.vaishnavi.hrms.hrmsbackend.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.createEmployee(request));
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(employeeService.getEmployeeByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @RequestParam String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}