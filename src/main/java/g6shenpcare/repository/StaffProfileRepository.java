package g6shenpcare.repository;

import g6shenpcare.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// [QUAN TRỌNG] Integer
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Integer> {
    // Không cần viết lại findById hay deleteById, JpaRepository tự lo
}