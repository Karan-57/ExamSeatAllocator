# Exam Seat Allocator

Java Swing microproject for automatic examination seat allocation.

## What was updated

1. **Persistent student storage**
   - Student records are now saved in a local file-based database at `data/students.db`.
   - Students added with **+ Add Student** remain available after closing and reopening the application.
   - New students are added to the existing list instead of replacing it.
   - Duplicate roll numbers are blocked.
   - **Clear Student Data** also updates the persistent student database.

2. **Editable classroom capacity**
   - Dashboard now has **Classroom Setup → Edit Capacity**.
   - Room number and classroom capacity can be changed.
   - The system converts the selected capacity into a rectangular row/column seating grid.
   - Classroom settings are saved in `data/classroom.properties` and loaded on the next run.
   - Capacity cannot be reduced below the current number of students.
   - A student cannot be added when the classroom is full.

3. **Excel import improvement**
   - Existing `.xlsx` import is preserved.
   - Imported students are added to the existing student list.
   - Duplicate roll numbers are skipped instead of replacing existing students.
   - Import stops when classroom capacity is reached.

4. **Allocation improvement for larger classrooms**
   - The allocation algorithm now supports empty seats when classroom capacity is larger than the number of students.
   - Example: 19 students can be allocated in a 50-seat classroom while keeping the division-separation rule.

5. **Existing functionality retained**
   - Dashboard
   - Students table
   - Add Student
   - Import Excel
   - Allocate Seats
   - Seat Map
   - Search Student
   - Detailed Result
   - Reset Sample Data

## Student data format

Manual input fields:
- Roll Number
- Student Name
- Division

Excel input expects `.xlsx` data with:
- Roll Number
- Student Name
- Division

The first Excel row is treated as the header row.

## Persistent data files

The application automatically creates a `data` folder when needed:

```text
data\students.db
 data\classroom.properties
```

These files are local project data files; no external database server is required.

## Main classes

- `SeatAllocationAlgorithm.Student` – stores student details.
- `SeatAllocationAlgorithm.StudentDatabase` – saves and loads persistent student records.
- `SeatAllocationAlgorithm.Seat` – represents one examination seat.
- `SeatAllocationAlgorithm.Classroom` – creates and manages the seating grid.
- `SeatAllocationAlgorithm.SeatAllocator` – validates students and generates the division-aware seating pattern.
- `SeatAllocationAlgorithm.AllocationResult` – returns allocation status and message.
- `SeatAllocationAlgorithm.Main` – console demonstration.
- `Member3.ExamSeatGUI` – Swing graphical user interface.

## Run from command line

From the project root:

```text
run.bat
```

Or manually:

```text
mkdir bin
javac -d bin src/SeatAllocationAlgorithm/*.java src/Member3/ExamSeatAllocator.java src/Member3/ExamSeatGUI.java
java -cp bin Member3.ExamSeatGUI
```

## Example workflow

```text
Set classroom capacity to 50
        ↓
Add first 15 students
        ↓
Close application
        ↓
Open application again
        ↓
The same 15 students are loaded
        ↓
Add another student
        ↓
Total = 16 students
        ↓
Allocate Seats
```
