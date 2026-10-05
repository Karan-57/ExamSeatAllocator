package SeatAllocationAlgorithm;

/**
 * Model representing a single seat allocation record stored in MySQL table 'seat_allocations'.
 */
public class AllocationRecord {
    private final int id;
    private final int rollNo;
    private final String studentName;
    private final String division;
    private final String roomNo;
    private final int rowNo;
    private final int columnNo;

    public AllocationRecord(int id, int rollNo, String studentName, String division, String roomNo, int rowNo, int columnNo) {
        this.id = id;
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.division = division;
        this.roomNo = roomNo;
        this.rowNo = rowNo;
        this.columnNo = columnNo;
    }

    public AllocationRecord(int rollNo, String studentName, String division, String roomNo, int rowNo, int columnNo) {
        this(0, rollNo, studentName, division, roomNo, rowNo, columnNo);
    }

    public int getId() {
        return id;
    }

    public int getRollNo() {
        return rollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getDivision() {
        return division;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public int getRowNo() {
        return rowNo;
    }

    public int getColumnNo() {
        return columnNo;
    }

    @Override
    public String toString() {
        return String.format("Room: %s | Row: %d | Col: %d | Roll: %d | Name: %s | Div: %s",
                roomNo, rowNo, columnNo, rollNo, studentName, division);
    }
}

