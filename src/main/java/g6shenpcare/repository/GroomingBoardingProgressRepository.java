package g6shenpcare.repository;

import g6shenpcare.models.entity.GroomingBoardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroomingBoardingProgressRepository extends JpaRepository<GroomingBoardingProgress, Integer> {

    List<GroomingBoardingProgress> findByBooking_BookingIdOrderByUpdatedAtDesc(Integer bookingId);


    /**
     * Đếm số lượng progress records của một booking
     */
    long countByBooking_BookingId(Integer bookingId);

    /**
     * Lấy progress record mới nhất của một booking
     */
    GroomingBoardingProgress findTopByBooking_BookingIdOrderByUpdatedAtDesc(Integer bookingId);
}
