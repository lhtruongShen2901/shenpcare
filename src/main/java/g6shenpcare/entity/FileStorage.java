package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FileStorage")
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FileId")
    private Integer fileId;

    @Column(name = "FileName", nullable = false)
    private String fileName;

    @Column(name = "ContentType", nullable = false)
    private String contentType;

    @Column(name = "FileSizeBytes", nullable = false)
    private Long fileSizeBytes;

    @Lob // Báo hiệu đây là dữ liệu lớn (BLOB/VARBINARY)
    @Column(name = "Data", nullable = false)
    private byte[] data;

    @Column(name = "UploadedByUserId")
    private Integer uploadedByUserId;

    @Column(name = "UploadedAt", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // --- GETTERS & SETTERS ---
    public Integer getFileId() { return fileId; }
    public void setFileId(Integer fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public Integer getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(Integer uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}