package uz.urspi.allocate.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.urspi.allocate.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT DISTINCT u FROM User u "
            + "LEFT JOIN FETCH u.roles r "
            + "LEFT JOIN FETCH r.permissions "
            + "LEFT JOIN FETCH u.faculty "
            + "LEFT JOIN FETCH u.department d "
            + "LEFT JOIN FETCH d.faculty "
            + "WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u "
            + "LEFT JOIN FETCH u.roles "
            + "LEFT JOIN FETCH u.faculty "
            + "LEFT JOIN FETCH u.department")
    List<User> findAllWithDetails();
}
