package com.shas.smart_home_automation_system.repository;

import com.shas.smart_home_automation_system.entity.Home;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeRepository extends JpaRepository<Home, Long> {

    List<Home> findByUserId(Long userId);

    Optional<Home> findByIdAndUserId(Long id, Long userId);
}
