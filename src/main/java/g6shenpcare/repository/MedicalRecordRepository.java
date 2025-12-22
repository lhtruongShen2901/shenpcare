package g6shenpcare.repository;

import g6shenpcare.dto.DailyReportDTO;
import g6shenpcare.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // QUERY QUAN TRỌNG: Báo cáo cuối ngày cho Admin
    // Tính tổng số ca, tổng thuốc và tổng tiền dựa trên giá bán lẻ hiện tại
    @Query("SELECT new g6shenpcare.dto.DailyReportDTO(" +
           "   m.doctorId, " +
           "   COUNT(DISTINCT m.recordId), " +
           "   COALESCE(SUM(d.quantity), 0), " +
           "   COALESCE(SUM(d.quantity * p.retailPrice), 0) " +
           ") " +
           "FROM MedicalRecord m " +
           "LEFT JOIN m.prescriptionDetails d " +
           "LEFT JOIN d.product p " +
           "WHERE CAST(m.createdAt AS date) = :today " +
           "GROUP BY m.doctorId")
    List<DailyReportDTO> getDailyReportByDate(@Param("today") LocalDate today);
}