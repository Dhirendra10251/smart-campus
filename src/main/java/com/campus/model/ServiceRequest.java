package com.campus.model;

import com.campus.model.enums.Priority;
import com.campus.model.enums.RequestStatus;

/**
 * ServiceRequest — Represents a maintenance/service request raised by a student.
 *
 * Unit 2 OOP: class, encapsulation, constructor overloading, implements Reportable.
 */
public class ServiceRequest implements Reportable {

    private int           requestId;
    private int           userId;
    private Integer       resourceId;   // nullable — request may not reference a resource
    private String        description;
    private String        category;
    private Priority      priority;
    private RequestStatus status;
    private String        createdAt;
    private String        updatedAt;

    // Optional denormalized fields
    private String userName;
    private String resourceName;

    // Constructor for new request
    public ServiceRequest(int userId, Integer resourceId, String description,
                          String category, Priority priority,
                          RequestStatus status, String createdAt, String updatedAt) {
        this.userId      = userId;
        this.resourceId  = resourceId;
        this.description = description;
        this.category    = category;
        this.priority    = priority;
        this.status      = status;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // Constructor for loading from DB
    public ServiceRequest(int requestId, int userId, Integer resourceId,
                          String description, String category,
                          Priority priority, RequestStatus status,
                          String createdAt, String updatedAt) {
        this(userId, resourceId, description, category, priority, status, createdAt, updatedAt);
        this.requestId = requestId;
    }

    // -------------------------------------------------------------------------
    // Reportable interface
    // -------------------------------------------------------------------------

    @Override
    public String toReportLine() {
        String res  = (resourceName != null) ? resourceName : (resourceId != null ? "Res #" + resourceId : "N/A");
        String user = (userName != null) ? userName : "User #" + userId;
        return String.format("[%d] %-10s | %-12s | %-13s | %s [%s] (by %s)",
                requestId, status, priority, category, description, res, user);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public int           getRequestId()   { return requestId; }
    public int           getUserId()      { return userId; }
    public Integer       getResourceId()  { return resourceId; }
    public String        getDescription() { return description; }
    public String        getCategory()    { return category; }
    public Priority      getPriority()    { return priority; }
    public RequestStatus getStatus()      { return status; }
    public String        getCreatedAt()   { return createdAt; }
    public String        getUpdatedAt()   { return updatedAt; }
    public String        getUserName()    { return userName; }
    public String        getResourceName(){ return resourceName; }

    public void setRequestId(int id)         { this.requestId = id; }
    public void setStatus(RequestStatus s)   { this.status = s; }
    public void setPriority(Priority p)      { this.priority = p; }
    public void setUpdatedAt(String t)       { this.updatedAt = t; }
    public void setUserName(String name)     { this.userName = name; }
    public void setResourceName(String name) { this.resourceName = name; }

    @Override
    public String toString() { return toReportLine(); }
}
