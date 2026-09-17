package com.campus.service;

import com.campus.dao.ResourceDAO;
import com.campus.exception.InvalidInputException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.model.Resource;
import com.campus.model.enums.ResourceStatus;
import com.campus.model.enums.ResourceType;
import com.campus.util.InputValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ResourceService — Business logic for campus resource management.
 *
 * Validates inputs and delegates persistence to ResourceDAO.
 */
public class ResourceService {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ResourceDAO  resourceDAO;
    private final AuditService auditService;

    public ResourceService(ResourceDAO resourceDAO, AuditService auditService) {
        this.resourceDAO  = resourceDAO;
        this.auditService = auditService;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public Resource addResource(int adminId, String name, String typeStr,
                                String location, String capacityStr, String statusStr) {
        name     = InputValidator.validateNonEmpty(name, "Resource name");
        location = InputValidator.validateNonEmpty(location, "Location");
        int capacity = InputValidator.validateNonNegativeInt(capacityStr, "Capacity");

        ResourceType   type   = InputValidator.validateEnum(typeStr, ResourceType.class, "Resource type");
        ResourceStatus status = InputValidator.validateEnum(statusStr, ResourceStatus.class, "Status");

        String now = LocalDateTime.now().format(DT_FMT);
        Resource res = new Resource(name, type, location, capacity, status, now);
        resourceDAO.insert(res);

        auditService.log(adminId, "RESOURCE_ADD",
                "Added resource: " + name + " [" + type + "] at " + location);
        return res;
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public List<Resource> getAllResources() {
        return resourceDAO.findAll();
    }

    public List<Resource> searchResources(String keyword) {
        keyword = InputValidator.validateNonEmpty(keyword, "Search keyword");
        return resourceDAO.searchByName(keyword);
    }

    public List<Resource> getResourcesByType(String typeStr) {
        ResourceType type = InputValidator.validateEnum(typeStr, ResourceType.class, "Resource type");
        return resourceDAO.findByType(type);
    }

    public Resource getResourceById(int resourceId) {
        return resourceDAO.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No resource found with ID: " + resourceId));
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public Resource updateResource(int adminId, int resourceId, String name,
                                   String typeStr, String location,
                                   String capacityStr, String statusStr) {
        Resource existing = getResourceById(resourceId);

        // Only update fields that are non-empty (blank = keep existing)
        if (!name.isBlank())     existing.setName(name.trim());
        if (!location.isBlank()) existing.setLocation(location.trim());
        if (!capacityStr.isBlank()) {
            existing.setCapacity(InputValidator.validateNonNegativeInt(capacityStr, "Capacity"));
        }
        if (!typeStr.isBlank()) {
            existing.setType(InputValidator.validateEnum(typeStr, ResourceType.class, "Type"));
        }
        if (!statusStr.isBlank()) {
            existing.setStatus(InputValidator.validateEnum(statusStr, ResourceStatus.class, "Status"));
        }

        resourceDAO.update(existing);
        auditService.log(adminId, "RESOURCE_UPDATE",
                "Updated resource ID " + resourceId + ": " + existing.getName());
        return existing;
    }

    public void updateStatus(int adminId, int resourceId, String statusStr) {
        ResourceStatus status = InputValidator.validateEnum(statusStr, ResourceStatus.class, "Status");
        if (!resourceDAO.updateStatus(resourceId, status)) {
            throw new ResourceNotFoundException("Resource ID " + resourceId + " not found.");
        }
        auditService.log(adminId, "RESOURCE_STATUS",
                "Resource #" + resourceId + " status changed to " + status);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public void deleteResource(int adminId, int resourceId) {
        Resource res = getResourceById(resourceId); // throws if not found
        resourceDAO.delete(resourceId);
        auditService.log(adminId, "RESOURCE_DELETE",
                "Deleted resource: " + res.getName() + " (ID " + resourceId + ")");
    }
}
