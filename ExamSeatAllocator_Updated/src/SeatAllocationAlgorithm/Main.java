package SeatAllocationAlgorithm;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();

        // Division A
        students.add(
                new Student(101, "Rahul", "A")
        );

        students.add(
                new Student(102, "Aditya", "A")
        );

        students.add(
                new Student(103, "Sneha", "A")
        );

        students.add(
                new Student(104, "Neha", "A")
        );

        students.add(
                new Student(105, "Riya", "A")
        );

        students.add(
                new Student(106, "Pooja", "A")
        );

        // Division B
        students.add(
                new Student(201, "Om", "B")
        );

        students.add(
                new Student(202, "Amit", "B")
        );

        students.add(
                new Student(203, "Karan", "B")
        );

        students.add(
                new Student(204, "Akash", "B")
        );

        students.add(
                new Student(205, "Vishal", "B")
        );

        students.add(
                new Student(206, "Sahil", "B")
        );

        // Division C
        students.add(
                new Student(301, "Priya", "C")
        );

        students.add(
                new Student(302, "Kavya", "C")
        );

        students.add(
                new Student(303, "Anjali", "C")
        );

        students.add(
                new Student(304, "Simran", "C")
        );

        students.add(
                new Student(305, "Isha", "C")
        );

        students.add(
                new Student(306, "Tanvi", "C")
        );

        Classroom classroom =
                new Classroom(
                        "101",
                        6,
                        3
                );

        SeatAllocator allocator = new SeatAllocator();

        AllocationResult result = allocator.allocate(
                        students,
                        classroom
                );

        System.out.println(
                result.getMessage()
        );

        if (result.isSuccess()) {
            allocator.printAllocation(
                    result.getClassroom()
            );

            allocator.printDetailedAllocation(
                    result.getClassroom()
            );
        }
    }
}