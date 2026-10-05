package SeatAllocationAlgorithm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing seat allocations in MySQL.
 */
public class AllocationDAO {

    public AllocationDAO() {
    }

    /**
     * Ensures that the 'seat_allocations' table exists in the database.
     */
    public void ensureTableExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS seat_allocations ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "roll_no INT NOT NULL, "
                + "student_name VARCHAR(100) NOT NULL, "
                + "division VARCHAR(20) NOT NULL, "
                + "room_no VARCHAR(20) NOT NULL, "
                + "row_no INT NOT NULL, "
                + "column_no INT NOT NULL"
                + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Saves the generated seating allocation from Classroom into MySQL 'seat_allocations'.
     * Removes previous allocations before inserting the new complete arrangement.
     */
    public void saveAllocation(Classroom classroom) throws SQLException {
        if (classroom == null) {
            throw new IllegalArgumentException("Classroom cannot be null when saving allocation.");
        }

        ensureTableExists();

        String deleteSql = "DELETE FROM seat_allocations";
        String insertSql = "INSERT INTO seat_allocations (roll_no, student_name, division, room_no, row_no, column_no) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Clear previous allocations
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.executeUpdate();
                }

                // Insert each allocated seat using PreparedStatement
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    String roomNo = classroom.getRoomNumber();
                    int rows = classroom.getRows();
                    int cols = classroom.getColumns();

                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            Seat seat = classroom.getSeat(r, c);
                            if (seat != null && !seat.isEmpty()) {
                                Student student = seat.getStudent();
                                insertStmt.setInt(1, student.getRollNo());
                                insertStmt.setString(2, student.getName());
                                insertStmt.setString(3, student.getDivision());
                                insertStmt.setString(4, roomNo);
                                insertStmt.setInt(5, r + 1); // 1-based row number
                                insertStmt.setInt(6, c + 1); // 1-based column number
                                insertStmt.addBatch();
                            }
                        }
                    }

                    insertStmt.executeBatch();
                }

                conn.commit();
                System.out.println("Allocation saved to database successfully.");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Failed to save allocation to database: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves all saved seat allocations from MySQL ordered by room_no, row_no, column_no.
     */
    public List<AllocationRecord> getAllAllocations() throws SQLException {
        ensureTableExists();

        String query = "SELECT id, roll_no, student_name, division, room_no, row_no, column_no "
                + "FROM seat_allocations "
                + "ORDER BY room_no, row_no, column_no";

        List<AllocationRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int rollNo = rs.getInt("roll_no");
                String name = rs.getString("student_name");
                String division = rs.getString("division");
                String roomNo = rs.getString("room_no");
                int rowNo = rs.getInt("row_no");
                int columnNo = rs.getInt("column_no");

                list.add(new AllocationRecord(id, rollNo, name, division, roomNo, rowNo, columnNo));
            }
        }

        return list;
    }
}

