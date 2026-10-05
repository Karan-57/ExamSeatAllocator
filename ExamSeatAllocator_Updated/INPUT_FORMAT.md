# Student Input and Classroom Setup

## 1. Add Student manually

Open **Students → + Add Student**.

Enter:

- Roll Number
- Student Name
- Division

Example:

```text
Roll Number: 107
Student Name: Manmath
Division: A
```

The student is saved to the local persistent database. Closing and reopening the application loads the saved student again.

If the classroom is full, the application shows a capacity warning and does not add another student.

## 2. Classroom capacity

Open **Dashboard → Classroom Setup → Edit Capacity**.

Enter:

- Room Number
- Classroom Capacity

Example:

```text
Room Number: 101
Classroom Capacity: 50
```

The application creates a seating grid for the selected capacity and saves the classroom settings for the next run.

The capacity cannot be set below the current number of students.

## 3. Import Excel

Open **Import Excel** and select an `.xlsx` file.

The first row is treated as the header row. Use these columns:

| Roll Number | Student Name | Division |
|---|---|---|
| 101 | Rahul | A |
| 102 | Aditya | A |
| 201 | Om | B |

Imported students are added to the existing student database. Existing students are not deleted. Duplicate roll numbers are skipped and import stops when classroom capacity is reached.

## 4. Clear Student Data

Open **Students → Clear Student Data** and confirm.

This removes the current student records from the local persistent database.

## 5. Reset Sample Data

Use **Reset Sample Data** to restore the original sample students and the default Room 101, 6 × 3 classroom.
