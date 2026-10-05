package SeatAllocationAlgorithm;

import java.util.*;

public class SeatAllocator {
    public AllocationResult allocate(List<Student> students,Classroom classroom) {
        // Basic validation
        if (students == null || students.isEmpty()) {
            return new AllocationResult(false,"No students provided.",classroom);
        }

        if (classroom == null) {
            return new AllocationResult(false,"Classroom is null.",null);
        }

        // Capacity check

        if (students.size() > classroom.getCapacity()) {

            return new AllocationResult(false,"Insufficient seats. Students = "+ students.size()+ ", Capacity = "+ classroom.getCapacity(),classroom);
        }

        //  Validate students
        String validationError = validateStudents(students);

        if (validationError != null) {
            return new AllocationResult( false,validationError, classroom);
        }

        // Clear classroom
        classroom.clearAllSeats();

        // Group students by division
        Map<String, Queue<Student>> studentsByDivision = groupStudentsByDivision(students);

        //Generate division pattern
        String[][] divisionPattern = generateDivisionPattern(classroom.getRows(), classroom.getColumns(), studentsByDivision);

        if (divisionPattern == null) {
            return new AllocationResult(false, "Could not generate a valid division pattern.", classroom);
        }

        //Put students into their division positions

        boolean assignmentSuccessful = assignStudents(classroom, divisionPattern, studentsByDivision);

        if (!assignmentSuccessful) {
            classroom.clearAllSeats();
            return new AllocationResult(false,"Could not assign all students.",classroom);
        }

        // Final validation
        boolean valid = validateFinalAllocation( classroom, students.size());

        if (!valid) {
            classroom.clearAllSeats();
            return new AllocationResult(false, "Allocation failed final conflict validation.", classroom);
        }

        return new AllocationResult(true, "Allocation generated successfully.", classroom);
    }

    private String validateStudents(List<Student> students) {
        Set<Integer> rollNumbers = new HashSet<>();

        for (Student student : students) {

            if (student == null) {
                return "Student list contains null student.";
            }

            if (student.getName() == null || student.getName().trim().isEmpty()) {
                return "Student name cannot be empty.";
            }

            if (student.getDivision() == null || student.getDivision().trim().isEmpty()) {
                return "Student division cannot be empty.";
            }

            if (!rollNumbers.add(student.getRollNo())) {
                return "Duplicate roll number found: " + student.getRollNo();
            }
        }

        return null;
    }

    private Map<String, Queue<Student>> groupStudentsByDivision(List<Student> students) {
        Map<String, Queue<Student>> map = new HashMap<>();

        for (Student student : students) {
                String division = student.getDivision();

                if (!map.containsKey(division)) {
                map.put(division, new LinkedList<>());
                }

                map.get(division).offer(student);
        }

        return map;
     }

    private String[][] generateDivisionPattern( int rows, int columns, Map<String, Queue<Student>> studentsByDivision) {

        String[][] pattern = new String[rows][columns];
        List<String> divisions = new ArrayList<>( studentsByDivision.keySet());

        return fillPattern(pattern,0,divisions,studentsByDivision)? pattern : null;
    }

    private boolean fillPattern(String[][] pattern, int position, List<String> divisions, Map<String, Queue<Student>> studentsByDivision) {
        int rows = pattern.length;
        int columns = pattern[0].length;

        if (studentsByDivision.values().stream().allMatch(Queue::isEmpty)) {
            return true;
        }

        if (position == rows * columns) {
            return false;
        }

        int row = position / columns;
        int column = position % columns;

        List<String> candidates = new ArrayList<>(divisions);

        candidates.sort((a, b) -> Integer.compare(
                studentsByDivision.get(b).size(),
                studentsByDivision.get(a).size()
            )
        );

        for (String division : candidates) {

            Queue<Student> queue = studentsByDivision.get(division);

            if (queue.isEmpty()) {
                continue;
            }

            if (!isDivisionPlacementValid(pattern, row, column, division)) {
                continue;
            }

            pattern[row][column] = division;

            Student student = queue.poll();

            boolean success = fillPattern(pattern, position + 1, divisions, studentsByDivision);

            if (success) {
                queue.add(student);
                return true;
            }

            pattern[row][column] = null;
            queue.add(student);
        }

        // A larger classroom may have empty seats. Leave this position empty
        // when that is necessary to keep the division-separation rule valid.
        pattern[row][column] = null;
        if (fillPattern(pattern, position + 1, divisions, studentsByDivision)) {
            return true;
        }

        return false;
    }

    private boolean isDivisionPlacementValid(String[][] pattern, int row, int column, String division) {
        // LEFT
        if (column > 0) {
            String left = pattern[row][column - 1];

            if (division.equals(left)) {
                return false;
            }
        }

        // ABOVE
        if (row > 0) {
            String above = pattern[row - 1][column];

            if (division.equals(above)) {
                return false;
            }
        }

        return true;
    }

    private boolean assignStudents(Classroom classroom, String[][] pattern, Map<String, Queue<Student>> studentsByDivision) {
        int rows = classroom.getRows();
        int columns = classroom.getColumns();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                String division = pattern[row][column];

                if (division == null) {
                    continue;
                }

                Queue<Student> queue = studentsByDivision.get(division);

                if (queue == null || queue.isEmpty()) {
                    return false;
                }

                Student student = queue.poll();
                Seat seat = classroom.getSeat(row, column);
                seat.assignStudent(student);
            }
        }

        // Every student should have been consumed
        for (Queue<Student> queue : studentsByDivision.values()) {
            if (!queue.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean isStudentPlacementValid(Classroom classroom, Student student, int row, int column) {
        String division = student.getDivision();

        // LEFT
        if (column > 0) {
            Seat left = classroom.getSeat(row,column - 1);

            if (!left.isEmpty() && left.getStudent().getDivision().equals(division)) {
                return false;
            }
        }

        // RIGHT
        if (column < classroom.getColumns() - 1) {
            Seat right = classroom.getSeat(row, column + 1);

            if (!right.isEmpty() && right.getStudent().getDivision().equals(division)) {
                return false;
            }
        }

        // ABOVE
        if (row > 0) {
            Seat above = classroom.getSeat(row - 1, column);

            if (!above.isEmpty() && above.getStudent().getDivision().equals(division)) {
                return false;
            }
        }

        // BELOW
        if (row < classroom.getRows() - 1) {
            Seat below = classroom.getSeat( row + 1, column);

            if (!below.isEmpty() && below.getStudent().getDivision().equals(division)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateFinalAllocation(Classroom classroom, int expectedStudentCount) {
        Set<Integer> allocatedRollNumbers = new HashSet<>();

        int allocatedCount = 0;

        for (int row = 0; row < classroom.getRows(); row++) {
            for (int column = 0; column < classroom.getColumns(); column++) {
                Seat seat = classroom.getSeat(row, column);

                if (seat.isEmpty()) {
                    continue;
                }

                Student student = seat.getStudent();

                if (!allocatedRollNumbers.add(student.getRollNo())) {
                    return false;
                }

                allocatedCount++;

                if (!isStudentPlacementValid(classroom, student, row, column)) {
                    return false;
                }
            }
        }
        return allocatedCount == expectedStudentCount;
    }

    public void printAllocation(Classroom classroom) {

        System.out.println();
        System.out.println("==============================================");

        System.out.println( "ROOM: " + classroom.getRoomNumber() );

        System.out.println("==============================================");

        for (int row = 0; row < classroom.getRows(); row++) {
            for (int column = 0; column < classroom.getColumns(); column++) {
                Seat seat = classroom.getSeat(row, column);

                if (seat.isEmpty()) {
                    System.out.print( "[ EMPTY ] ");

                } else {
                    Student student = seat.getStudent();

                    System.out.printf( "[%d-%s] ", student.getRollNo(), student.getDivision() );
                }
            }

            System.out.println();
        }

        System.out.println();
    }

    public void printDetailedAllocation(Classroom classroom) {

        System.out.println();
        System.out.println("ROOM " + classroom.getRoomNumber());

        System.out.println("------------------------------------------------");

        System.out.printf( "%-8s %-8s %-20s %-10s%n", "Row", "Seat", "Student", "Division");

        System.out.println("------------------------------------------------");

        for (int row = 0; row < classroom.getRows(); row++) {
            for (int column = 0; column < classroom.getColumns(); column++) {
                Seat seat = classroom.getSeat(row, column);

                if (!seat.isEmpty()) {

                    Student student = seat.getStudent();

                    System.out.printf("%-8d %-8d %-20s %-10s%n", row + 1, column + 1, student.getName(), student.getDivision());
                }
            }
        }

        System.out.println("------------------------------------------------");
    }
}