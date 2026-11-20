package com.lifeline.emergency.dto;

public class AttendanceLogRequest {
    private String employeeName;
    private String employeeRole;
    private String shift = "MORNING";
    private String status = "PRESENT";

    public AttendanceLogRequest() {}

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeRole() { return employeeRole; }
    public void setEmployeeRole(String employeeRole) { this.employeeRole = employeeRole; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
