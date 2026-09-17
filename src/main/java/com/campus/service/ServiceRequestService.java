package com.campus.service;

import com.campus.dao.ServiceRequestDAO;
import com.campus.exception.InvalidInputException;
import com.campus.exception.ResourceNotFoundException;
import com.campus.model.ServiceRequest;
import com.campus.model.enums.Priority;
import com.campus.model.enums.RequestStatus;
import com.campus.util.InputValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ServiceRequestService — Business logic for maintenance/service requests.
 */
public class ServiceRequestService {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ServiceRequestDAO serviceRequestDAO;
    private final AuditService      auditService;

    public ServiceRequestService(ServiceRequestDAO serviceRequestDAO,
                                 AuditService auditService) {
        this.serviceRequestDAO = serviceRequestDAO;
        this.auditService      = auditService;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public ServiceRequest createRequest(int userId, Integer resourceId,
                                        String description, String category,
                                        String priorityStr) {
        description = InputValidator.validateNonEmpty(description, "Description");
        category    = InputValidator.validateNonEmpty(category, "Category");
        Priority priority = InputValidator.validateEnum(priorityStr, Priority.class, "Priority");

        String now = LocalDateTime.now().format(DT_FMT);
        ServiceRequest req = new ServiceRequest(
            userId, resourceId, description, category.toUpperCase(),
            priority, RequestStatus.OPEN, now, now
        );
        serviceRequestDAO.insert(req);

        auditService.log(userId, "SERVICE_REQUEST_CREATE",
                "User #" + userId + " raised request: " + description.substring(0, Math.min(40, description.length())));
        return req;
    }

    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public List<ServiceRequest> getRequestsForUser(int userId) {
        return serviceRequestDAO.findByUserId(userId);
    }

    public List<ServiceRequest> getAllRequests() {
        return serviceRequestDAO.findAll();
    }

    public List<ServiceRequest> getRequestsByStatus(String statusStr) {
        RequestStatus status = InputValidator.validateEnum(statusStr, RequestStatus.class, "Status");
        return serviceRequestDAO.findByStatus(status);
    }

    public ServiceRequest getRequestById(int requestId) {
        return serviceRequestDAO.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Service request ID " + requestId + " not found."));
    }

    // -------------------------------------------------------------------------
    // UPDATE (admin)
    // -------------------------------------------------------------------------

    public void updateStatus(int adminId, int requestId, String statusStr) {
        RequestStatus status = InputValidator.validateEnum(statusStr, RequestStatus.class, "Status");
        String now = LocalDateTime.now().format(DT_FMT);
        if (!serviceRequestDAO.updateStatus(requestId, status, now)) {
            throw new ResourceNotFoundException("Request ID " + requestId + " not found.");
        }
        auditService.log(adminId, "SERVICE_REQUEST_UPDATE",
                "Request #" + requestId + " set to " + status + " by Admin #" + adminId);
    }

    public void updatePriority(int adminId, int requestId, String priorityStr) {
        Priority priority = InputValidator.validateEnum(priorityStr, Priority.class, "Priority");
        String now = LocalDateTime.now().format(DT_FMT);
        if (!serviceRequestDAO.updatePriority(requestId, priority, now)) {
            throw new ResourceNotFoundException("Request ID " + requestId + " not found.");
        }
        auditService.log(adminId, "SERVICE_REQUEST_PRIORITY",
                "Request #" + requestId + " priority set to " + priority + " by Admin #" + adminId);
    }
}
