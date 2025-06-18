package qnb.user.repository;

import qnb.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserBookReading;
import qnb.user.entity.UserBookWish;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기 (userEmail 필드에 맞춤)
    Optional<User> findByUserEmail(String userEmail);

}
