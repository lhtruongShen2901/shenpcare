package g6shenpcare.dto;

import java.util.List;

public class ExamSubmissionDTO {
    private String bookingId;
    private Long petId;
    private String symptoms;
    private String diagnosis;
    private String doctorNotes;

    // Các list này map với input name="medicineIds[]", name="quantities[]"...
    private List<Integer> medicineIds; 
    private List<Integer> quantities;
    private List<String> instructions;

    // Getters & Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
    public List<Integer> getMedicineIds() { return medicineIds; }
    public void setMedicineIds(List<Integer> medicineIds) { this.medicineIds = medicineIds; }
    public List<Integer> getQuantities() { return quantities; }
    public void setQuantities(List<Integer> quantities) { this.quantities = quantities; }
    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
}