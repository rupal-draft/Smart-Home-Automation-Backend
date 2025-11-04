package com.shas.smart_home_automation_system.repository;

import com.shas.smart_home_automation_system.entity.Home;
import com.shas.smart_home_automation_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeRepository extends JpaRepository<Home, Long> {

    List<Home> findByUser(User user);

    Optional<Home> findByIdAndUser(Long id, User user);
}
