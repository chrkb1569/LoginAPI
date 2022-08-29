package chrkb1569.LoginAPI.repository;

import chrkb1569.LoginAPI.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
