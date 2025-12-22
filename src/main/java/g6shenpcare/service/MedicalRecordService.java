package g6shenpcare.service;

import g6shenpcare.dto.ExamSubmissionDTO;
import g6shenpcare.dto.DailyReportDTO;
import g6shenpcare.entity.MedicalRecord;
import g6shenpcare.entity.PrescriptionDetail;
import g6shenpcare.entity.Product;
import g6shenpcare.repository.MedicalRecordRepository;
import g6shenpcare.repository.PrescriptionRepository;
import g6shenpcare.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional // ROLLBACK nếu có lỗi (quan trọng để kho không bị lệch)
public class MedicalRecordService {

    @Autowired private MedicalRecordRepository recordRepo;
    @Autowired private PrescriptionRepository prescriptionRepo;
    @Autowired private ProductRepository productRepo;

    public void saveExamination(ExamSubmissionDTO dto, Long doctorId) {
        // 1. Lưu thông tin chung
        MedicalRecord record = new MedicalRecord();
        record.setBookingId(dto.getBookingId());
        record.setDoctorId(doctorId);
        record.setPetId(dto.getPetId());
        record.setSymptoms(dto.getSymptoms());
        record.setDiagnosis(dto.getDiagnosis());
        record.setDoctorNotes(dto.getDoctorNotes());
        
        MedicalRecord savedRecord = recordRepo.save(record);

        // 2. Xử lý thuốc & Trừ kho
        if (dto.getMedicineIds() != null) {
            for (int i = 0; i < dto.getMedicineIds().size(); i++) {
                Integer prodId = dto.getMedicineIds().get(i);
                Integer qty = dto.getQuantities().get(i);
                String instruct = dto.getInstructions().get(i);

                if (prodId == null || qty == null || qty <= 0) continue;

                Product product = productRepo.findById(prodId)
                        .orElseThrow(() -> new RuntimeException("Lỗi: Thuốc ID " + prodId + " không tồn tại trong kho!"));

                // CHECK TỒN KHO (Logic Admin can thiệp)
                if (product.getStockQuantity() < qty) {
                    throw new RuntimeException("CẢNH BÁO: Thuốc '" + product.getName() + 
                            "' không đủ số lượng tồn kho! (Còn: " + product.getStockQuantity() + ")");
                }

                // Trừ kho thật
                product.setStockQuantity(product.getStockQuantity() - qty);
                productRepo.save(product);

                // Lưu chi tiết đơn
                PrescriptionDetail detail = new PrescriptionDetail();
                detail.setMedicalRecord(savedRecord);
                detail.setProduct(product);
                detail.setQuantity(qty);
                detail.setUsageInstruction(instruct);
                prescriptionRepo.save(detail);
            }
        }
    }

    public List<DailyReportDTO> getDailyStats() {
        return recordRepo.getDailyReportByDate(LocalDate.now());
    }
}