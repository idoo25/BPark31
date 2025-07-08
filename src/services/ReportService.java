package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TreeMap;

import entities.ParkingReport;
import server.DBController;

/**
 * ReportService handles all reporting operations following Single Responsibility Principle.
 * Manages parking reports, statistics, and analytical data generation.
 */
public class ReportService {
    
    private static ReportService instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private ReportService() {}
    
    /**
     * Returns singleton instance of ReportService.
     * @return ReportService instance
     */
    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }
    
    /**
     * Gets parking reports based on report type.
     * @param reportType The type of report to generate
     * @return ArrayList of ParkingReport objects
     */
    public ArrayList<ParkingReport> getParkingReports(String reportType) {
        ArrayList<ParkingReport> reports = new ArrayList<>();

        switch (reportType.toUpperCase()) {
            case "PARKING_TIME":
                reports.add(generateParkingTimeReport());
                break;
            case "SUBSCRIBER_STATUS":
                reports.add(generateSubscriberStatusReport());
                break;
            case "ALL":
                reports.add(generateParkingTimeReport());
                reports.add(generateSubscriberStatusReport());
                break;
            default:
                System.err.println("Unknown report type: " + reportType);
        }

        return reports;
    }
    
    /**
     * Generates parking time analysis report.
     * @return ParkingReport with parking time statistics
     */
    public ParkingReport generateParkingTimeReport() {
        ParkingReport report = new ParkingReport();
        report.setReportType("Parking Time Analysis");
        report.setReportDate(LocalDate.now());
        
        Connection conn = DBController.getInstance().getConnection();
        TreeMap<String, Integer> hourlyData = new TreeMap<>();
        
        try {
            // Get total parking time per day
            String totalTimeQuery = """
                    SELECT DATE(Entry_time) as parking_date, 
                           SUM(TIMESTAMPDIFF(HOUR, Entry_time, IFNULL(Actual_end_time, NOW()))) as total_hours
                    FROM parkinginfo 
                    WHERE Entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                    GROUP BY DATE(Entry_time)
                    ORDER BY parking_date DESC
                    """;
            
            try (PreparedStatement stmt = conn.prepareStatement(totalTimeQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String date = rs.getString("parking_date");
                        int hours = rs.getInt("total_hours");
                        hourlyData.put(date, hours);
                    }
                }
            }
            
            report.setTotalParkingTimePerDay(hourlyData);
            
            // Get extension statistics
            String extensionQuery = """
                    SELECT 
                        COUNT(*) as total_sessions,
                        SUM(CASE WHEN IsExtended = 'yes' THEN 1 ELSE 0 END) as extended_sessions
                    FROM parkinginfo 
                    WHERE Entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                    """;
            
            try (PreparedStatement stmt = conn.prepareStatement(extensionQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int totalSessions = rs.getInt("total_sessions");
                        int extendedSessions = rs.getInt("extended_sessions");
                        
                        report.setExtensions(extendedSessions);
                        report.setTotalParkings(totalSessions);
                        report.setNoExtensions(totalSessions - extendedSessions);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating parking time report: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return report;
    }
    
    /**
     * Generates subscriber status analysis report.
     * @return ParkingReport with subscriber statistics
     */
    public ParkingReport generateSubscriberStatusReport() {
        ParkingReport report = new ParkingReport();
        report.setReportType("Subscriber Status Analysis");
        report.setReportDate(LocalDate.now());
        
        Connection conn = DBController.getInstance().getConnection();
        
        try {
            // Get subscriber counts by type
            String subscriberQuery = """
                    SELECT UserTypeEnum, COUNT(*) as count 
                    FROM users 
                    GROUP BY UserTypeEnum
                    """;
            
            TreeMap<String, Integer> subscriberData = new TreeMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(subscriberQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String userType = rs.getString("UserTypeEnum");
                        int count = rs.getInt("count");
                        subscriberData.put(userType, count);
                    }
                }
            }
            
            // Note: setSubscriberTypeData method not available in entity, data stored in subscriberData
            
            // Get active vs inactive subscribers
            String activityQuery = """
                    SELECT 
                        COUNT(DISTINCT u.User_ID) as total_subscribers,
                        COUNT(DISTINCT CASE WHEN pi.Entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) 
                                           THEN u.User_ID END) as active_subscribers
                    FROM users u
                    LEFT JOIN parkinginfo pi ON u.User_ID = pi.User_ID
                    WHERE u.UserTypeEnum = 'sub'
                    """;
            
            try (PreparedStatement stmt = conn.prepareStatement(activityQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int totalSubscribers = rs.getInt("total_subscribers");
                        int activeSubscribers = rs.getInt("active_subscribers");
                        
                        report.setTotalSubscribers(totalSubscribers);
                        report.setActiveSubscribers(activeSubscribers);
                    }
                }
            }
            
            // Get late pickup statistics
            String latePickupQuery = """
                    SELECT 
                        COUNT(*) as total_exits,
                        COUNT(CASE WHEN Actual_end_time > Estimated_end_time THEN 1 END) as late_exits
                    FROM parkinginfo 
                    WHERE Actual_end_time IS NOT NULL 
                    AND Entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                    """;
            
            try (PreparedStatement stmt = conn.prepareStatement(latePickupQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int totalExits = rs.getInt("total_exits");
                        int lateExits = rs.getInt("late_exits");
                        
                        report.setTotalOrders(totalExits);
                        report.setLateExits(lateExits);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating subscriber status report: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return report;
    }
    
    /**
     * Gets reservation usage statistics.
     * @return TreeMap with reservation statistics
     */
    public TreeMap<String, Integer> getReservationUsageStats() {
        TreeMap<String, Integer> stats = new TreeMap<>();
        Connection conn = DBController.getInstance().getConnection();
        
        String query = """
                SELECT 
                    ReservationType,
                    statusEnum,
                    COUNT(*) as count
                FROM parkinginfo 
                WHERE Entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY ReservationType, statusEnum
                ORDER BY ReservationType, statusEnum
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("ReservationType");
                    String status = rs.getString("statusEnum");
                    int count = rs.getInt("count");
                    
                    String key = type + "_" + status;
                    stats.put(key, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservation usage stats: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return stats;
    }
    
    /**
     * Gets peak hour analysis data.
     * @return TreeMap with hourly parking counts
     */
    public TreeMap<String, Integer> getPeakHourAnalysis() {
        TreeMap<String, Integer> hourlyData = new TreeMap<>();
        Connection conn = DBController.getInstance().getConnection();
        
        String query = """
                SELECT 
                    HOUR(Entry_time) as hour_of_day,
                    COUNT(*) as parking_count
                FROM parkinginfo 
                WHERE Entry_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY HOUR(Entry_time)
                ORDER BY hour_of_day
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("hour_of_day");
                    int count = rs.getInt("parking_count");
                    
                    String hourStr = String.format("%02d:00", hour);
                    hourlyData.put(hourStr, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting peak hour analysis: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return hourlyData;
    }
    
    /**
     * Generates monthly reports for specified month/year.
     * @param monthYear Month and year string (e.g., "2024-01")
     * @return ArrayList of monthly parking reports
     */
    public ArrayList<ParkingReport> generateMonthlyReports(String monthYear) {
        ArrayList<ParkingReport> monthlyReports = new ArrayList<>();
        Connection conn = DBController.getInstance().getConnection();
        
        // Parse month-year and create date range
        String[] parts = monthYear.split("-");
        if (parts.length != 2) {
            System.err.println("Invalid month-year format. Expected: YYYY-MM");
            return monthlyReports;
        }
        
        String year = parts[0];
        String month = parts[1];
        
        String query = """
                SELECT 
                    DAY(Entry_time) as day_of_month,
                    COUNT(*) as total_parkings,
                    COUNT(CASE WHEN ReservationType = 'preorder' THEN 1 END) as reservations,
                    COUNT(CASE WHEN ReservationType = 'spontaneous' THEN 1 END) as spontaneous,
                    AVG(TIMESTAMPDIFF(HOUR, Entry_time, Actual_end_time)) as avg_duration_hours
                FROM parkinginfo 
                WHERE YEAR(Entry_time) = ? AND MONTH(Entry_time) = ?
                AND Entry_time IS NOT NULL
                GROUP BY DAY(Entry_time)
                ORDER BY day_of_month
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, year);
            stmt.setString(2, month);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ParkingReport report = new ParkingReport();
                    
                    int dayOfMonth = rs.getInt("day_of_month");
                    int totalParkings = rs.getInt("total_parkings");
                    int reservations = rs.getInt("reservations");
                    int spontaneous = rs.getInt("spontaneous");
                    double avgDuration = rs.getDouble("avg_duration_hours");
                    
                    String reportDate = String.format("%s-%s-%02d", year, month, dayOfMonth);
                    
                    report.setReportTitle("Daily Report for " + reportDate);
                    report.setReportDate(LocalDate.parse(reportDate));
                    report.setTotalOrders(totalParkings);
                    report.setReservationCount(reservations);
                    report.setSpontaneousCount(spontaneous);
                    report.setAverageParkingDuration(avgDuration);
                    
                    monthlyReports.add(report);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating monthly reports: " + e.getMessage());
        } finally {
            DBController.getInstance().releaseConnection(conn);
        }
        
        return monthlyReports;
    }
}