package Member3;


import java.util.*;
import SeatAllocationAlgorithm.*;


public class ExamSeatAllocator {

    static Scanner sc = new Scanner(System.in);

    static List<Student> students = new ArrayList<>();

    static Classroom classroom;
    static SeatAllocator allocator = new SeatAllocator();
    static AllocationResult result;
    static AllocationDAO allocationDAO = new AllocationDAO();

    public static void main(String[] args) {

        addSampleStudents();

        classroom = new Classroom("101", 6, 3);

        int choice;

        do {
            System.out.println("\n==============================================");
            System.out.println("           EXAM SEAT ALLOCATOR");
            System.out.println("==============================================");
            System.out.println("1. View Students");
            System.out.println("2. Allocate Seats");
            System.out.println("3. View Seat Allocation");
            System.out.println("4. Search Student");
            System.out.println("5. Print Detailed Allocation");
            System.out.println("6. Exit");
            System.out.println("==============================================");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewStudents();
                    break;

                case 2:
                    allocateSeats();
                    break;

                case 3:
                    viewAllocation();
                    break;

                case 4:
                    searchStudent();
                    break;

                case 5:
                    printDetailedAllocation();
                    break;

                case 6:
                    System.out.println("\nThank you for using Exam Seat Allocator!");
                    break;

                default:
                    System.out.println("\nInvalid choice! Please try again.");
            }

        } while (choice != 6);
    }

    static void addSampleStudents() {

        students.add(new Student(101, "Rahul", "A"));
        students.add(new Student(102, "Aditya", "A"));
        students.add(new Student(103, "Sneha", "A"));
        students.add(new Student(104, "Neha", "A"));
        students.add(new Student(105, "Riya", "A"));
        students.add(new Student(106, "Pooja", "A"));

        students.add(new Student(201, "Om", "B"));
        students.add(new Student(202, "Amit", "B"));
        students.add(new Student(203, "Karan", "B"));
        students.add(new Student(204, "Akash", "B"));
        students.add(new Student(205, "Vishal", "B"));
        students.add(new Student(206, "Sahil", "B"));

        students.add(new Student(301, "Priya", "C"));
        students.add(new Student(302, "Kavya", "C"));
        students.add(new Student(303, "Anjali", "C"));
        students.add(new Student(304, "Simran", "C"));
        students.add(new Student(305, "Isha", "C"));
        students.add(new Student(306, "Tanvi", "C"));
    }

    static void viewStudents() {

        System.out.println("\n--------------- STUDENT LIST ---------------");

        System.out.printf("%-10s %-15s %-10s%n",
                "Roll No", "Name", "Division");

        System.out.println("---------------------------------------------");

        for (Student student : students) {

            System.out.printf("%-10d %-15s %-10s%n",
                    student.getRollNo(),
                    student.getName(),
                    student.getDivision());
        }
    }

    static void allocateSeats() {

        System.out.println("\nAllocating seats...");

        result = allocator.allocate(students, classroom);

        System.out.println("\n" + result.getMessage());

        if (result.isSuccess()) {
            System.out.println("Seats allocated successfully!");

            // Save to MySQL database
            try {
                allocationDAO.saveAllocation(classroom);
            } catch (Exception e) {
                System.err.println("Failed to save allocation to database: " + e.getMessage());
            }
        }
    }

    static void viewAllocation() {

        if (result == null || !result.isSuccess()) {

            System.out.println("\nPlease allocate seats first.");
            return;
        }

        allocator.printAllocation(result.getClassroom());
    }

    static void searchStudent() {

        if (result == null || !result.isSuccess()) {

            System.out.println("\nPlease allocate seats first.");
            return;
        }

        System.out.print("\nEnter Roll Number: ");
        int roll = sc.nextInt();

        boolean found = false;

        for (int row = 0; row < classroom.getRows(); row++) {

            for (int column = 0; column < classroom.getColumns(); column++) {

                Seat seat = classroom.getSeat(row, column);

                if (!seat.isEmpty()) {

                    Student student = seat.getStudent();

                    if (student.getRollNo() == roll) {

                        System.out.println("\n================================");
                        System.out.println("       STUDENT DETAILS");
                        System.out.println("================================");

                        System.out.println("Roll No   : " + student.getRollNo());
                        System.out.println("Name      : " + student.getName());
                        System.out.println("Division  : " + student.getDivision());
                        System.out.println("Room No   : " + classroom.getRoomNumber());
                        System.out.println("Row       : " + (row + 1));
                        System.out.println("Seat      : " + (column + 1));

                        System.out.println("================================");

                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                break;
            }
        }

        if (!found) {
            System.out.println("\nStudent not found!");
        }
    }

    static void printDetailedAllocation() {

        if (result == null || !result.isSuccess()) {

            System.out.println("\nPlease allocate seats first.");
            return;
        }

        allocator.printDetailedAllocation(result.getClassroom());
    }
}