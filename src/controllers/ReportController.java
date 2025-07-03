package controllers;

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
 * ReportController handles report generation for the ParkB parking management
 * system. Updated to work with unified parkinginfo table structure
 */

public class ReportController {
//	protected Connection conn;
	public int successFlag;

	public ReportController(String dbname, String pass) {
		DBController.initializeConnection(dbname, pass);
//		conn = DBController.getInstance().getConnection();
	}

	/**
	 * Gets parking reports based on report type
	 * 
	 * @param reportType The type of report to generate ("PARKING_TIME" or
	 *                   "SUBSCRIBER_STATUS")
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
			System.out.println("Unknown report type: " + reportType);
			break;
		}

		return reports;
	}

	/**
	 * Generates monthly reports automatically at the end of each month
	 * 
	 * @param monthYear Format: "YYYY-MM"
	 * @return ArrayList of monthly reports
	 */
	public ArrayList<ParkingReport> generateMonthlyReports(String monthYear) {
		ArrayList<ParkingReport> monthlyReports = new ArrayList<>();

		try {
			// Parse the month-year string
			String[] parts = monthYear.split("-");
			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]);

			LocalDate reportDate = LocalDate.of(year, month, 1);

			// Generate parking time report for the specific month
			ParkingReport parkingTimeReport = generateMonthlyParkingTimeReport(reportDate);
			if (parkingTimeReport != null) {
				monthlyReports.add(parkingTimeReport);
			}

			// Generate subscriber status report for the specific month
			ParkingReport subscriberReport = generateMonthlySubscriberStatusReport(reportDate);
			if (subscriberReport != null) {
				monthlyReports.add(subscriberReport);
			}

			// Store reports in database
			storeMonthlyReports(monthlyReports);

		} catch (Exception e) {
			System.out.println("Error generating monthly reports: " + e.getMessage());
		}

		return monthlyReports;
	}

	/**
	 * Generates a parking time report showing usage patterns, delays, and
	 * extensions
	 */
	private ParkingReport generateParkingTimeReport() {
		ParkingReport report = new ParkingReport("PARKING_TIME", LocalDate.now());
		Connection conn = DBController.getInstance().getConnection();

		String qry = """
				SELECT
				    COUNT(*) as total_parkings,
				    AVG(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, NOW()))) as avg_duration,
				    SUM(CASE WHEN IsLate = 'yes' THEN 1 ELSE 0 END) as late_exits,
				    SUM(CASE WHEN IsExtended = 'yes' THEN 1 ELSE 0 END) as extensions,
				    MIN(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, NOW()))) as min_duration,
				    MAX(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, NOW()))) as max_duration
				FROM parkinginfo
				WHERE statusEnum IN ('active', 'finished')
				AND Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					report.setTotalParkings(rs.getInt("total_parkings"));
					report.setAverageParkingTime(rs.getDouble("avg_duration"));
					report.setLateExits(rs.getInt("late_exits"));
					report.setExtensions(rs.getInt("extensions"));
					report.setMinParkingTime(rs.getInt("min_duration"));
					report.setMaxParkingTime(rs.getInt("max_duration"));
				}
			}
		} catch (SQLException e) {
			System.out.println("Error generating parking time report: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		report.setTotalParkingTimePerDay(getTotalParkingTimePerDay());
		report.setHourlyDistribution(getHourlyDistribution());
		report.setLateExitsByHour(getLateExitsByHour());
		report.setNoExtensions(getNoExtensions());
		report.setLateSubscribers(getLateSubscribers());
		report.setTotalSubscribers(getTotalSubscribers());
		report.setReservations(getUsedReservations() + getCancelledReservations());
		report.setUsedReservations(getUsedReservations());
		report.setCancelledReservations(getCancelledReservations());
		report.getpreOrderReservations();
		report.setpreOrderReservations(getPreOrderedReservations());
		return report;
	}

	/**
	 * Generates a subscriber status report showing subscriber activity and usage
	 * patterns
	 */
	private ParkingReport generateSubscriberStatusReport() {
		ParkingReport report = new ParkingReport("SUBSCRIBER_STATUS", LocalDate.now());
		Connection conn = DBController.getInstance().getConnection();

		// Get active subscribers count
		String activeSubQry = """
				SELECT COUNT(DISTINCT User_ID) as active_subscribers
				FROM parkinginfo
				WHERE Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				""";

		// Get total orders, reservations, and immediate entries
		String ordersQry = """
				SELECT
				    COUNT(*) as total_orders,
				    SUM(CASE WHEN IsOrderedEnum = 'yes' THEN 1 ELSE 0 END) as reservations,
				    SUM(CASE WHEN IsOrderedEnum = 'no' THEN 1 ELSE 0 END) as immediate_entries,
				    AVG(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, NOW()))) as avg_session_duration
				FROM parkinginfo
				WHERE Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				AND statusEnum IN ('active', 'finished')
				""";

		// Get cancelled reservations
		String cancelledQry = """
				SELECT COUNT(*) as cancelled_reservations
				FROM parkinginfo
				WHERE statusEnum = 'cancelled'
				AND Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				""";

		try {
			// Get active subscribers
			try (PreparedStatement stmt = conn.prepareStatement(activeSubQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						report.setActiveSubscribers(rs.getInt("active_subscribers"));
					}
				}
			}

			// Get order statistics
			try (PreparedStatement stmt = conn.prepareStatement(ordersQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						report.setTotalOrders(rs.getInt("total_orders"));
						report.setReservations(rs.getInt("reservations"));
						report.setImmediateEntries(rs.getInt("immediate_entries"));
						report.setAverageSessionDuration(rs.getDouble("avg_session_duration"));
					}
				}
			}

			// Get cancelled reservations
			try (PreparedStatement stmt = conn.prepareStatement(cancelledQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						report.setCancelledReservations(rs.getInt("cancelled_reservations"));
					}
				}
			}

		} catch (SQLException e) {
			System.out.println("Error generating subscriber status report: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		report.setSubscribersPerDay(getSubscribersPerDay());
		report.setTotalSubscribers(getTotalSubscribers());
		report.setLateSubscribers(getLateSubscribers());

		return report;
	}

	/**
	 * Generates a monthly parking time report for a specific month
	 */
	private ParkingReport generateMonthlyParkingTimeReport(LocalDate reportDate) {
		ParkingReport report = new ParkingReport("PARKING_TIME", reportDate);
		Connection conn = DBController.getInstance().getConnection();

		String qry = """
				SELECT
				    COUNT(*) as total_parkings,
				    AVG(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, Estimated_end_time))) as avg_duration,
				    SUM(CASE WHEN IsLate = 'yes' THEN 1 ELSE 0 END) as late_exits,
				    SUM(CASE WHEN IsExtended = 'yes' THEN 1 ELSE 0 END) as extensions,
				    MIN(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, Estimated_end_time))) as min_duration,
				    MAX(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, Estimated_end_time))) as max_duration
				FROM parkinginfo
				WHERE YEAR(Date_Of_Placing_Order) = ? AND MONTH(Date_Of_Placing_Order) = ?
				AND statusEnum IN ('active', 'finished')
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, reportDate.getYear());
			stmt.setInt(2, reportDate.getMonthValue());

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					report.setTotalParkings(rs.getInt("total_parkings"));
					report.setAverageParkingTime(rs.getDouble("avg_duration"));
					report.setLateExits(rs.getInt("late_exits"));
					report.setExtensions(rs.getInt("extensions"));
					report.setMinParkingTime(rs.getInt("min_duration"));
					report.setMaxParkingTime(rs.getInt("max_duration"));

					return report;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error generating monthly parking time report: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return null;
	}

	/**
	 * Generates a monthly subscriber status report for a specific month
	 */
	private ParkingReport generateMonthlySubscriberStatusReport(LocalDate reportDate) {
		ParkingReport report = new ParkingReport("SUBSCRIBER_STATUS", reportDate);
		Connection conn = DBController.getInstance().getConnection();

		// Get active subscribers for the month
		String activeSubQry = """
				SELECT COUNT(DISTINCT User_ID) as active_subscribers
				FROM parkinginfo
				WHERE YEAR(Date_Of_Placing_Order) = ? AND MONTH(Date_Of_Placing_Order) = ?
				""";

		// Get monthly order statistics
		String ordersQry = """
				SELECT
				    COUNT(*) as total_orders,
				    SUM(CASE WHEN IsOrderedEnum = 'yes' THEN 1 ELSE 0 END) as reservations,
				    SUM(CASE WHEN IsOrderedEnum = 'no' THEN 1 ELSE 0 END) as immediate_entries,
				    AVG(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, Estimated_end_time))) as avg_session_duration
				FROM parkinginfo
				WHERE YEAR(Date_Of_Placing_Order) = ? AND MONTH(Date_Of_Placing_Order) = ?
				AND statusEnum IN ('active', 'finished')
				""";

		// Get cancelled reservations for the month
		String cancelledQry = """
				SELECT COUNT(*) as cancelled_reservations
				FROM parkinginfo
				WHERE statusEnum = 'cancelled'
				AND YEAR(Date_Of_Placing_Order) = ? AND MONTH(Date_Of_Placing_Order) = ?
				""";

		try {
			// Get active subscribers
			try (PreparedStatement stmt = conn.prepareStatement(activeSubQry)) {
				stmt.setInt(1, reportDate.getYear());
				stmt.setInt(2, reportDate.getMonthValue());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						report.setActiveSubscribers(rs.getInt("active_subscribers"));
					}
				}
			}

			// Get order statistics
			try (PreparedStatement stmt = conn.prepareStatement(ordersQry)) {
				stmt.setInt(1, reportDate.getYear());
				stmt.setInt(2, reportDate.getMonthValue());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						report.setTotalOrders(rs.getInt("total_orders"));
						report.setReservations(rs.getInt("reservations"));
						report.setImmediateEntries(rs.getInt("immediate_entries"));
						report.setAverageSessionDuration(rs.getDouble("avg_session_duration"));
					}
				}
			}

			// Get cancelled reservations
			try (PreparedStatement stmt = conn.prepareStatement(cancelledQry)) {
				stmt.setInt(1, reportDate.getYear());
				stmt.setInt(2, reportDate.getMonthValue());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						report.setCancelledReservations(rs.getInt("cancelled_reservations"));
					}
				}
			}

			return report;

		} catch (SQLException e) {
			System.out.println("Error generating monthly subscriber status report: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return null;
	}

	/**
	 * Stores monthly reports in the database
	 */
	private void storeMonthlyReports(ArrayList<ParkingReport> reports) {
		String qry = "INSERT INTO reports (Report_Type, Generated_Date, Report_Data) VALUES (?, NOW(), ?)";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			for (ParkingReport report : reports) {
				stmt.setString(1, report.getReportType());
				stmt.setString(2, report.toString()); // Store as JSON or formatted string
				stmt.executeUpdate();
			}
			System.out.println("Monthly reports stored successfully");
		} catch (SQLException e) {
			System.out.println("Error storing monthly reports: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Gets historical reports from the database
	 */
	public ArrayList<ParkingReport> getHistoricalReports(String reportType, LocalDate fromDate, LocalDate toDate) {
		ArrayList<ParkingReport> reports = new ArrayList<>();
		Connection conn = DBController.getInstance().getConnection();

		String qry = """
				SELECT * FROM reports
				WHERE Report_Type = ?
				AND DATE(Generated_Date) BETWEEN ? AND ?
				ORDER BY Generated_Date DESC
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, reportType);
			stmt.setString(2, fromDate.toString());
			stmt.setString(3, toDate.toString());

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					// This would need to be enhanced to parse the stored report data
					// For now, we'll create a basic report object
					ParkingReport report = new ParkingReport();
					report.setReportType(rs.getString("Report_Type"));
					Timestamp genDate = rs.getTimestamp("Generated_Date");
					if (genDate != null) {
						report.setReportDate(genDate.toLocalDateTime().toLocalDate());
					}
					reports.add(report);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting historical reports: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return reports;
	}

	/**
	 * Gets peak usage hours for analysis
	 */
	public ArrayList<String> getPeakUsageHours() {
		ArrayList<String> peakHours = new ArrayList<>();
		Connection conn = DBController.getInstance().getConnection();

		String qry = """
				SELECT
				    HOUR(Actual_start_time) as entry_hour,
				    COUNT(*) as entry_count
				FROM parkinginfo
				WHERE Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				AND Actual_start_time IS NOT NULL
				GROUP BY HOUR(Actual_start_time)
				ORDER BY entry_count DESC
				LIMIT 5
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int hour = rs.getInt("entry_hour");
					int count = rs.getInt("entry_count");
					peakHours.add(String.format("%02d:00 - %d entries", hour, count));
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting peak usage hours: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return peakHours;
	}

	/**
	 * Gets daily parking statistics for the current month
	 */
	public ArrayList<String> getDailyStatistics() {
		ArrayList<String> dailyStats = new ArrayList<>();
		Connection conn = DBController.getInstance().getConnection();

		String qry = """
				SELECT
				    DATE(Date_Of_Placing_Order) as order_date,
				    COUNT(*) as daily_entries,
				    SUM(CASE WHEN IsLate = 'yes' THEN 1 ELSE 0 END) as daily_late_exits,
				    AVG(TIMESTAMPDIFF(MINUTE, Actual_start_time, COALESCE(Actual_end_time, NOW()))) as avg_daily_duration
				FROM parkinginfo
				WHERE YEAR(Date_Of_Placing_Order) = YEAR(CURDATE())
				AND MONTH(Date_Of_Placing_Order) = MONTH(CURDATE())
				AND statusEnum IN ('active', 'finished')
				GROUP BY DATE(Date_Of_Placing_Order)
				ORDER BY order_date DESC
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String date = rs.getDate("order_date").toString();
					int entries = rs.getInt("daily_entries");
					int lateExits = rs.getInt("daily_late_exits");
					double avgDuration = rs.getDouble("avg_daily_duration");

					dailyStats.add(String.format("%s: %d entries, %d late exits, %.1f min avg duration", date, entries,
							lateExits, avgDuration));
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting daily statistics: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return dailyStats;
	}

	private java.util.Map<String, Integer> getTotalParkingTimePerDay() {
		java.util.Map<String, Integer> map = new TreeMap<>();
		String qry = """
				SELECT
				  DATE(Actual_start_time) AS day,
				  CEIL(SUM(TIMESTAMPDIFF(MINUTE, Actual_start_time, Actual_end_time)) / 60) AS total_hours
				FROM parkinginfo
				WHERE statusEnum = 'finished'
				  AND Actual_start_time IS NOT NULL
				  AND Actual_end_time IS NOT NULL
				  AND YEAR(Actual_start_time) = YEAR(CURDATE())
				  AND MONTH(Actual_start_time) = MONTH(CURDATE())
				GROUP BY day
				ORDER BY day;
								""";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String day = rs.getString("day");
					int totalHours = rs.getInt("total_hours");
					map.put(day, totalHours);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return map;
	}

	private java.util.Map<String, Integer> getHourlyDistribution() {
		java.util.Map<String, Integer> map = new TreeMap<>();
		String qry = """
				    SELECT HOUR(Actual_start_time) as hour, COUNT(*) as cnt
				    FROM parkinginfo
				    WHERE statusEnum = 'finished'
				    AND Actual_start_time IS NOT NULL
				    GROUP BY hour
				    ORDER BY hour
				""";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String hour = String.format("%02d:00", rs.getInt("hour"));
					int cnt = rs.getInt("cnt");
					map.put(hour, cnt);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return map;
	}

	private java.util.Map<String, Integer> getLateExitsByHour() {
		java.util.Map<String, Integer> map = new TreeMap<>();
		String qry = """
				    SELECT HOUR(Actual_end_time) as hour, COUNT(*) as cnt
				    FROM parkinginfo
				    WHERE IsLate = 'yes' AND Actual_end_time IS NOT NULL
				    GROUP BY hour
				    ORDER BY hour
				""";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String hour = String.format("%02d:00", rs.getInt("hour"));
					int cnt = rs.getInt("cnt");
					map.put(hour, cnt);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return map;
	}

	private java.util.Map<String, Integer> getSubscribersPerDay() {
		java.util.Map<String, Integer> map = new TreeMap<>();
		String qry = """
				    SELECT DATE(Actual_start_time) as day, COUNT(DISTINCT User_ID) as cnt
				    FROM parkinginfo
				    WHERE statusEnum IN ('active', 'finished')
				    AND Actual_start_time IS NOT NULL
				    GROUP BY day
				    ORDER BY day
				""";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String day = rs.getString("day");
					int cnt = rs.getInt("cnt");
					map.put(day, cnt);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return map;
	}

	private int getNoExtensions() {
		String qry = """
				    SELECT COUNT(*) as noext
				    FROM parkinginfo
				    WHERE IsExtended = 'no'
				    AND statusEnum IN ('active', 'finished')
				""";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("noext");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return 0;
	}

	private int getLateSubscribers() {
		String qry = """
				    SELECT COUNT(DISTINCT User_ID) as cnt
				    FROM parkinginfo
				    WHERE IsLate = 'yes'
				""";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("cnt");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return 0;
	}

	private int getTotalSubscribers() {
		String qry = "SELECT COUNT(*) as cnt FROM users";
		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("cnt");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return 0;
	}

	public int getUsedReservations() {
		int result = 0; // usedReservations
		Connection conn = DBController.getInstance().getConnection();

		String usedReservationsQry = """
				    SELECT COUNT(*) as used_reservations
				    FROM parkinginfo
				    WHERE IsOrderedEnum = 'yes'
				    AND statusEnum = 'finished'
				    AND Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				""";

		try {

			try (PreparedStatement stmt = conn.prepareStatement(usedReservationsQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						result = rs.getInt("used_reservations");
					}
				}
			}

		} catch (SQLException e) {
			System.out.println("Error getting reservations usage data: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return result;
	}

	public int getCancelledReservations() {
		int result = 0; // cancelledReservations
		Connection conn = DBController.getInstance().getConnection();

		String cancelledReservationsQry = """
				    SELECT COUNT(*) as cancelled_reservations
				    FROM parkinginfo
				    WHERE IsOrderedEnum = 'yes'
				    AND statusEnum = 'cancelled'
				    AND Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				""";

		try {

			try (PreparedStatement stmt = conn.prepareStatement(cancelledReservationsQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						result = rs.getInt("cancelled_reservations");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting reservations usage data: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return result;
	}

	public int getPreOrderedReservations() {
		int result = 0; // cancelledReservations
		Connection conn = DBController.getInstance().getConnection();

		String cancelledReservationsQry = """
				    SELECT COUNT(*) as cancelled_reservations
				    FROM parkinginfo
				    WHERE IsOrderedEnum = 'yes'
				    AND statusEnum = 'preorder'
				    AND Date_Of_Placing_Order >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
				""";

		try {

			try (PreparedStatement stmt = conn.prepareStatement(cancelledReservationsQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						result = rs.getInt("cancelled_reservations");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting reservations usage data: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return result;
	}

}