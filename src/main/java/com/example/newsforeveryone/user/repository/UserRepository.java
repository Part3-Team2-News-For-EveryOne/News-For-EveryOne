package com.example.newsforeveryone.user.repository;

import com.example.newsforeveryone.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmailAndDeletedAtIsNull(String email);
  Optional<User> findByIdAndDeletedAtIsNull(Long id);
  boolean existsByEmailAndDeletedAtIsNull(String email);
  List<User> findAllByDeletedAtIsNull();
}
