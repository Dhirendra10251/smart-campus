package com.campus.model;

import com.campus.model.enums.ResourceStatus;
import com.campus.model.enums.ResourceType;

/**
 * Resource — Represents a bookable campus resource (lab, hall, court, equipment).
 *
 * Unit 2 OOP:
 * - class, objects, encapsulation
 * - constructor overloading (new vs. loaded from DB)
 * - implements Reportable interface (Unit 2 — interface demonstration)
 */
public class Resource implements Reportable {

    private int            resourceId;
    private String         name;
    private ResourceType   type;
    private String         location;
    private int            capacity;
    private ResourceStatus status;
    private String         createdAt;

    // Constructor for creating a new resource (no ID yet)
    public Resource(String name, ResourceType type, String location,
                    int capacity, ResourceStatus status, String createdAt) {
        this.name      = name;
        this.type      = type;
        this.location  = location;
        this.capacity  = capacity;
        this.status    = status;
        this.createdAt = createdAt;
    }

    // Constructor for loading from DB (includes resourceId)
    public Resource(int resourceId, String name, ResourceType type, String location,
                    int capacity, ResourceStatus status, String createdAt) {
        this(name, type, location, capacity, status, createdAt);
        this.resourceId = resourceId;
    }

    // -------------------------------------------------------------------------
    // Reportable interface implementation
    // -------------------------------------------------------------------------

    @Override
    public String toReportLine() {
        return String.format("[%d] %-25s | %-18s | %-25s | Cap: %3d | %s",
                resourceId, name, type.getDisplayName(), location, capacity, status);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public int            getResourceId() { return resourceId; }
    public String         getName()       { return name; }
    public ResourceType   getType()       { return type; }
    public String         getLocation()  { return location; }
    public int            getCapacity()  { return capacity; }
    public ResourceStatus getStatus()    { return status; }
    public String         getCreatedAt() { return createdAt; }

    public void setResourceId(int id)         { this.resourceId = id; }
    public void setName(String name)          { this.name = name; }
    public void setType(ResourceType type)    { this.type = type; }
    public void setLocation(String location)  { this.location = location; }
    public void setCapacity(int capacity)     { this.capacity = capacity; }
    public void setStatus(ResourceStatus s)   { this.status = s; }

    public boolean isAvailable() {
        return this.status == ResourceStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return toReportLine();
    }
}
