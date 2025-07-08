package controllers;

import java.util.ArrayList;
import java.util.TreeMap;

import entities.ParkingReport;
import server.DBController;
import services.ReportService;

/**
 * ReportController handles report generation coordination.
 * Refactored to follow Single Responsibility Principle by delegating to ReportService.
 */
public class ReportController {
    
    public int successFlag;

    /**
     * Initializes the report controller with database connection.
     * @param dbname Database name
     * @param pass Database password
     */
    public ReportController(String dbname, String pass) {
        DBController.initializeConnection(dbname, pass);
        successFlag = 1;
    }

    /**
     * Gets parking reports based on report type.
     * @param reportType The type of report to generate
     * @return ArrayList of ParkingReport objects
     */
    public ArrayList<ParkingReport> getParkingReports(String reportType) {
        return ReportService.getInstance().getParkingReports(reportType);
    }
    
    /**
     * Generates parking time analysis report.
     * @return ParkingReport with parking time statistics
     */
    public ParkingReport generateParkingTimeReport() {
        return ReportService.getInstance().generateParkingTimeReport();
    }
    
    /**
     * Generates subscriber status analysis report.
     * @return ParkingReport with subscriber statistics
     */
    public ParkingReport generateSubscriberStatusReport() {
        return ReportService.getInstance().generateSubscriberStatusReport();
    }
    
    /**
     * Gets reservation usage statistics.
     * @return TreeMap with reservation statistics
     */
    public TreeMap<String, Integer> getReservationUsageStats() {
        return ReportService.getInstance().getReservationUsageStats();
    }
    
    /**
     * Gets peak hour analysis data.
     * @return TreeMap with hourly parking counts
     */
    public TreeMap<String, Integer> getPeakHourAnalysis() {
        return ReportService.getInstance().getPeakHourAnalysis();
    }
    
    /**
     * Generates monthly reports for specified month/year.
     * @param monthYear Month and year string (e.g., "2024-01")
     * @return ArrayList of monthly parking reports
     */
    public ArrayList<ParkingReport> generateMonthlyReports(String monthYear) {
        return ReportService.getInstance().generateMonthlyReports(monthYear);
    }
}