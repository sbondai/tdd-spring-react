package com.hoaxify.hoaxify.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hoaxify.hoaxify.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	Page<User> findByUsernameNot(String username, Pageable page);

	/*
	 * @Query(value = "Select * from user", nativeQuery = true) Page<UserProjection>
	 * getAllUsersProjection(Pageable pageable);
	 */

}
