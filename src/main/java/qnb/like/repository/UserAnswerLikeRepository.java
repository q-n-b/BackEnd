package qnb.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qnb.like.entity.UserAnswerLike;

public interface UserAnswerLikeRepository extends JpaRepository<UserAnswerLike, Long> {

    boolean existsByUser_UserIdAndAnswer_AnswerId(Long userId, Integer answerId);

    void deleteByUser_UserIdAndAnswer_AnswerId(Long userId, Integer answerId);

}
