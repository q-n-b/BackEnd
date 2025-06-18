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

    //나의 책장 조회에서 쓰임
    public interface UserBookReadRepository extends JpaRepository<UserBookRead, Long> {
        List<UserBookRead> findByUser(User user);
    }

    public interface UserBookReadingRepository extends JpaRepository<UserBookReading, Long> {
        List<UserBookReading> findByUser(User user);
    }

    public interface UserBookWishRepository extends JpaRepository<UserBookWish, Long> {
        List<UserBookWish> findByUser(User user);
    }

}
