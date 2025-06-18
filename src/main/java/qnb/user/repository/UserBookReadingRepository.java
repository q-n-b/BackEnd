package qnb.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserBookReading;
import qnb.user.entity.User;

import java.util.List;

@Repository
public interface UserBookReadingRepository extends JpaRepository<UserBookReading, Long> {
    List<UserBookReading> findByUser_UserId(Long userId);
}
