package g6shenpcare.repository;

import g6shenpcare.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Integer> {

    StaffProfile findByUser_UserId(Integer userId);


}