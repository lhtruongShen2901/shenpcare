package g6shenpcare.repository;

import g6shenpcare.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    // Long = StaffId = UserId
}
