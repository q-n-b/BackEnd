package qnb.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserBookWish;

import java.util.List;

@Repository
public interface UserBookWishRepository extends JpaRepository<UserBookWish, Long> {
    List<UserBookWish> findByUser_UserId(Long userId);
    void deleteByUser_UserIdAndBook_BookId(Long userId, Integer bookId);
    boolean existsByUser_UserIdAndBook_BookId(Long userId, Integer bookId);
}
