package g6shenpcare.repository;

import g6shenpcare.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Integer> {
    Optional<LeavePolicy> findByRoleName(String roleName);
}