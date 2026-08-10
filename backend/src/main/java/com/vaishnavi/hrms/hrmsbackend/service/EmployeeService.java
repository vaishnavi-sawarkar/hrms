package com.vaishnavi.hrms.hrmsbackend.service;

import com.vaishnavi.hrms.hrmsbackend.dto.EmployeeRequest;
import com.vaishnavi.hrms.hrmsbackend.dto.EmployeeResponse;
import com.vaishnavi.hrms.hrmsbackend.model.Department;
import com.vaishnavi.hrms.hrmsbackend.model.Designation;
import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import com.vaishnavi.hrms.hrmsbackend.repository.DepartmentRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.DesignationRepository;
import com.vaishnavi.hrms.hrmsbackend.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           DesignationRepository designationRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = mapToEntity(request);
        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
        return mapToResponse(employee);
    }

    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<EmployeeResponse> searchEmployees(String keyword, Pageable pageable) {
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::mapToResponse);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPhoneNumber(request.getPhoneNumber());
        existing.setDateOfJoining(request.getDateOfJoining());
        existing.setSalary(request.getSalary());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            existing.setDepartment(dept);
        }

        if (request.getDesignationId() != null) {
            Designation desig = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new RuntimeException("Designation not found"));
            existing.setDesignation(desig);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            existing.setManager(manager);
        }

        Employee updated = employeeRepository.save(existing);
        return mapToResponse(updated);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    // --- Helper mapping methods ---

    private Employee mapToEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setSalary(request.getSalary());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employee.setDepartment(dept);
        }

        if (request.getDesignationId() != null) {
            Designation desig = designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new RuntimeException("Designation not found"));
            employee.setDesignation(desig);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            employee.setManager(manager);
        }

        return employee;
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .dateOfJoining(employee.getDateOfJoining())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .designationName(employee.getDesignation() != null ? employee.getDesignation().getTitle() : null)
                .managerName(employee.getManager() != null
                        ? employee.getManager().getFirstName() + " " + employee.getManager().getLastName()
                        : null)
                .salary(employee.getSalary())
                .build();
    }
}
