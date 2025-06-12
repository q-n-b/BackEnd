package qnb.user.repository;

import qnb.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
}
