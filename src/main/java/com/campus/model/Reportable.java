package com.campus.model;

/**
 * Reportable — Interface for entities that can produce a single-line report entry.
 *
 * Unit 2 OOP:
 * - interface demonstration
 * - Implemented by Resource, Booking, ServiceRequest
 */
public interface Reportable {
    /**
     * Returns a formatted single-line string suitable for inclusion in reports.
     */
    String toReportLine();
}
