package g6shenpcare.controller.admin;

import g6shenpcare.entity.FileStorage;
import g6shenpcare.repository.FileStorageRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageRepository fileStorageRepository;

    public FileController(FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Integer id) {
        // 1. Tìm file trong DB
        FileStorage fileStorage = fileStorageRepository.findById(id).orElse(null);

        // 2. Nếu không thấy -> Trả về lỗi 404
        if (fileStorage == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // 3. Nếu thấy -> Trả về dữ liệu ảnh (byte[]) kèm Content-Type đúng
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileStorage.getContentType())) // VD: image/png, image/jpeg
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileStorage.getFileName() + "\"")
                .body(fileStorage.getData());
    }
}