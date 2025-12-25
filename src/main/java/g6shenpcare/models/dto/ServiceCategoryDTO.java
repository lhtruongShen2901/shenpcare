package g6shenpcare.models.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryDTO {
    private Integer id;
    private String categoryType;
    private String name;
}
