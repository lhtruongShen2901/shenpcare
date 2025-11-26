package g6shenpcare.repository;

import g6shenpcare.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByStatusIgnoreCase(String status);
}
