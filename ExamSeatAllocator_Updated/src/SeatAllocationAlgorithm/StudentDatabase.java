package SeatAllocationAlgorithm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Small file-based local database for student records.
 * Data is stored outside the compiled classes so it remains after restart.
 */
public final class StudentDatabase {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path STUDENT_FILE = DATA_DIR.resolve("students.db");

    private StudentDatabase() {
    }

    /** Returns null when the database file has never been created. */
    public static List<Student> loadStudents() throws IOException {
        if (!Files.exists(STUDENT_FILE)) {
            return null;
        }

        List<Student> students = new ArrayList<>();
        for (String line : Files.readAllLines(STUDENT_FILE, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);
            if (parts.length < 3) {
                continue;
            }

            try {
                int roll = Integer.parseInt(parts[0].trim());
                String name = unescape(parts[1]);
                String division = unescape(parts[2]).toUpperCase();

                if (!name.trim().isEmpty() && !division.trim().isEmpty()) {
                    students.add(new Student(roll, name, division));
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid database rows.
            }
        }

        return students;
    }

    public static void saveStudents(List<Student> students) throws IOException {
        Files.createDirectories(DATA_DIR);

        List<String> lines = new ArrayList<>();
        for (Student student : students) {
            lines.add(
                    student.getRollNo()
                            + "|"
                            + escape(student.getName())
                            + "|"
                            + escape(student.getDivision())
            );
        }

        Files.write(STUDENT_FILE, lines, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (char c : value.toCharArray()) {
            if (escaped) {
                if (c == 'n') {
                    result.append('\n');
                } else if (c == 'r') {
                    result.append('\r');
                } else {
                    result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                result.append(c);
            }
        }

        if (escaped) {
            result.append('\\');
        }

        return result.toString();
    }
}
