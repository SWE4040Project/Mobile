package mobiledev.unb.clockin;

/**
 * Created by Brent on 2017-02-26.
 */

import java.sql.Timestamp;
import java.sql.Time;

public class Shift {

    private int id;
    private int employee_id;
    private int location_id;
    private int company_id;
    private Timestamp scheduled_start_time;
    private Timestamp scheduled_end_time;
    private Timestamp real_start_time;
    private Timestamp real_end_time;
    private Timestamp approved_start_time;
    private Timestamp approved_end_time;
    private boolean available;
    private String worked_notes;

    public Shift(){

    }

    public Shift(int id, int employee_id, int location_id, int company_id, Timestamp scheduled_start_time, Timestamp scheduled_end_time, Timestamp real_start_time, Timestamp real_end_time, Timestamp approved_start_time, Timestamp approved_end_time, boolean available, String worked_notes) {
        this.id = id;
        this.employee_id = employee_id;
        this.location_id = location_id;
        this.company_id = company_id;
        this.scheduled_start_time = scheduled_start_time;
        this.scheduled_end_time = scheduled_end_time;
        this.real_start_time = real_start_time;
        this.real_end_time = real_end_time;
        this.approved_start_time = approved_start_time;
        this.approved_end_time = approved_end_time;
        this.available = available;
        this.worked_notes = worked_notes;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setScheduled_start_time(Timestamp scheduled_start_time) {
        this.scheduled_start_time = scheduled_start_time;
    }

    public void setScheduled_end_time(Timestamp scheduled_end_time) {
        this.scheduled_end_time = scheduled_end_time;
    }

    public void setReal_start_time(Timestamp real_start_time) {
        this.real_start_time = real_start_time;
    }

    public void setReal_end_time(Timestamp real_end_time) {
        this.real_end_time = real_end_time;
    }

    public void setApproved_start_time(Timestamp approved_start_time) {
        this.approved_start_time = approved_start_time;
    }

    public void setApproved_end_time(Timestamp approved_end_time) {
        this.approved_end_time = approved_end_time;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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

    public Timestamp getScheduled_start_time() {
        return scheduled_start_time;
    }

    public Timestamp getScheduled_end_time() {
        return scheduled_end_time;
    }

    public Timestamp getReal_start_time() {
        return real_start_time;
    }

    public Timestamp getReal_end_time() {
        return real_end_time;
    }

    public Timestamp getApproved_start_time() {
        return approved_start_time;
    }

    public Timestamp getApproved_end_time() {
        return approved_end_time;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getWorked_notes() {
        return worked_notes;
    }
}
