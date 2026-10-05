package SeatAllocationAlgorithm;

public class Classroom {

    private String roomNumber;
    private int rows;
    private int columns;
    private Seat[][] seats;

    public Classroom(String roomNumber, int rows, int columns) {

        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException(
                    "Rows and columns must be greater than zero."
            );
        }

        this.roomNumber = roomNumber;
        this.rows = rows;
        this.columns = columns;

        seats = new Seat[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                seats[i][j] = new Seat(i, j);
            }
        }
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getCapacity() {
        return rows * columns;
    }

    public Seat getSeat(int row, int column) {
        return seats[row][column];
    }

    public Seat[][] getSeats() {
        return seats;
    }

    public void clearAllSeats() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                seats[i][j].clear();
            }
        }
    }
}