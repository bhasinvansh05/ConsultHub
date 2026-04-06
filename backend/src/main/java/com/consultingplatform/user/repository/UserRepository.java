package com.consultingplatform.user.repository;

import com.consultingplatform.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
        long countByRole(String role);
	/** Admin user ids (single-table inheritance discriminator). Used for broadcast notifications to admins. */
	@Query(value = "SELECT id FROM app_users WHERE role = 'ADMIN'", nativeQuery = true)
	List<Long> findAllAdminIds();
}
