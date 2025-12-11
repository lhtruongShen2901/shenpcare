package g6shenpcare.service;

import g6shenpcare.entity.Pets;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepo;
    private final BookingRepository bookingRepo;

    public PetService(PetRepository petRepo, BookingRepository bookingRepo) {
        this.petRepo = petRepo;
        this.bookingRepo = bookingRepo;
    }

    // [FIX] Integer customerId
    public List<Pets> getPetsByCustomer(Integer customerId) {
        return petRepo.findByCustomerId(customerId);
    }

    // [FIX] Integer id
    public Pets getPetById(Integer id) {
        return petRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thú cưng với ID: " + id));
    }

    @Transactional
    public void savePet(Pets pet) {
        if (pet.getPetId() == null) {
            pet.setCreatedAt(LocalDateTime.now());
            if (pet.getPetCode() == null) {
                pet.setPetCode("P" + System.currentTimeMillis());
            }
        }
        if (pet.getName() != null) {
            pet.setName(pet.getName().trim());
        }
        pet.setUpdatedAt(LocalDateTime.now());
        petRepo.save(pet);
    }

    // [FIX] Integer petId
    @Transactional
    public void deletePet(Integer petId) {
        if (bookingRepo.existsByPetId(petId)) {
            throw new IllegalStateException("Không thể xóa: Thú cưng này đã có lịch sử đặt hẹn/khám bệnh.");
        }
        petRepo.deleteById(petId);
    }
    
    // Logic Claim hồ sơ
    @Transactional
    public void claimPetProfile(String petCode, Integer userId) {
        Pets pet = petRepo.findByPetCode(petCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã hồ sơ không tồn tại!"));

        if (pet.getOwnerId() != null && !pet.getOwnerId().equals(userId)) {
             throw new IllegalStateException("Hồ sơ này đã thuộc về người khác!");
        }
        pet.setOwnerId(userId);
        petRepo.save(pet);
    }
}