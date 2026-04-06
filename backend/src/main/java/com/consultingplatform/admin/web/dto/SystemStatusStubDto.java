package com.consultingplatform.admin.web.dto;

/**
 * STUB: to be implemented — replace with real metrics from actuator or infrastructure probes.
 */
public class SystemStatusStubDto {

    private String status;
    private boolean databaseReachable;
    private String message;

    public SystemStatusStubDto() {
    }

    public SystemStatusStubDto(String status, boolean databaseReachable, String message) {
        this.status = status;
        this.databaseReachable = databaseReachable;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDatabaseReachable() {
        return databaseReachable;
    }

    public void setDatabaseReachable(boolean databaseReachable) {
        this.databaseReachable = databaseReachable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
