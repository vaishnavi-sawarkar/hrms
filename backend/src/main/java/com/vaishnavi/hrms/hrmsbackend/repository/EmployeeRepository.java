package com.vaishnavi.hrms.hrmsbackend.repository;

import com.vaishnavi.hrms.hrmsbackend.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Search by first or last name containing the given text (case-insensitive), paginated
    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable
    );

    Optional<Employee> findByEmail(String email);
}