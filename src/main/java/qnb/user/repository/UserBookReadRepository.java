package qnb.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.User;

import java.util.List;

@Repository
public interface UserBookReadRepository extends JpaRepository<UserBookRead, Long> {
    List<UserBookRead> findByUser_UserId(Long userId);
    void deleteByUser_UserIdAndBook_BookId(Long userId, Integer bookId);
    boolean existsByUser_UserIdAndBook_BookId(Long userId, Integer bookId);
}
