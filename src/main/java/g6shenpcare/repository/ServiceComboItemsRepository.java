package g6shenpcare.repository;

import g6shenpcare.entity.ServiceComboItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceComboItemsRepository extends JpaRepository<ServiceComboItems, Integer> {
    
    // Tìm tất cả các dòng mà dịch vụ con là ID này
    // (Dùng để tìm cha: Dịch vụ này nằm trong Combo nào?)
    List<ServiceComboItems> findBySingleServiceId(Integer singleServiceId);
    
    // Tìm các món trong 1 combo
    List<ServiceComboItems> findByComboServiceId(Integer comboServiceId);
}