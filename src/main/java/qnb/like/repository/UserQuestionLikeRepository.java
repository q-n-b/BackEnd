package qnb.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qnb.like.entity.UserQuestionLike;

public interface UserQuestionLikeRepository extends JpaRepository<UserQuestionLike, Long> {

    boolean existsByUser_UserIdAndQuestion_QuestionId(Long userId, Integer questionId);

    void deleteByUser_UserIdAndQuestion_QuestionId(Long userId, Integer questionId);

}
