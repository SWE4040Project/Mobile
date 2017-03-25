package mobiledev.unb.clockin;

/**
 * Created by Brent on 2017-02-26.
 */

public class Shift {

    private int id;
    private String title;
    private int employee_id;
    private int location_id;
    private int company_id;
    private String day;
    private String month;
    private String scheduled_start_time;
    private String scheduled_end_time;
    private String real_start_time;
    private String real_end_time;
    private String worked_notes;

    public Shift(){

    }

    public Shift(int id, int employee_id, int location_id, int company_id, String scheduled_start_time, String scheduled_end_time, String real_start_time, String real_end_time, String worked_notes) {
        this.id = id;
        this.employee_id = employee_id;
        this.location_id = location_id;
        this.company_id = company_id;
        this.scheduled_start_time = scheduled_start_time;
        this.scheduled_end_time = scheduled_end_time;
        this.real_start_time = real_start_time;
        this.real_end_time = real_end_time;
        this.worked_notes = worked_notes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setEmployee_id(int employee_id) {
        this.employee_id = employee_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String date) {
        this.day = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setScheduled_start_time(String scheduled_start_time) {
        this.scheduled_start_time = scheduled_start_time;
    }

    public void setScheduled_end_time(String scheduled_end_time) {
        this.scheduled_end_time = scheduled_end_time;
    }

    public void setReal_start_time(String real_start_time) {
        this.real_start_time = real_start_time;
    }

    public void setReal_end_time(String real_end_time) {
        this.real_end_time = real_end_time;
    }

    public void setWorked_notes(String worked_notes) {
        this.worked_notes = worked_notes;
    }

    public int getId() {
        return id;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public int getLocation_id() {
        return location_id;
    }

    public int getCompany_id() {
        return company_id;
    }

    public String getScheduled_start_time() {
        return scheduled_start_time;
    }

    public String getScheduled_end_time() {
        return scheduled_end_time;
    }

    public String getReal_start_time() {
        return real_start_time;
    }

    public String getReal_end_time() {
        return real_end_time;
    }

    public String getWorked_notes() {
        return worked_notes;
    }
}
