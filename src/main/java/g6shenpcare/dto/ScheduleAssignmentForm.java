package g6shenpcare.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ScheduleAssignmentForm {
    private List<Integer> staffIds;
    private List<Integer> daysOfWeek;
    private Integer templateId;
    private LocalDate fromDate; 
    private LocalDate toDate;   
    private LocalTime customStartTime;
    private LocalTime customEndTime;


    public List<Integer> getStaffIds() {
        return staffIds;
    }

    public void setStaffIds(List<Integer> staffIds) {
        this.staffIds = staffIds;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public LocalTime getCustomStartTime() {
        return customStartTime;
    }

    public void setCustomStartTime(LocalTime customStartTime) {
        this.customStartTime = customStartTime;
    }

    public LocalTime getCustomEndTime() {
        return customEndTime;
    }

    public void setCustomEndTime(LocalTime customEndTime) {
        this.customEndTime = customEndTime;
    }
}
