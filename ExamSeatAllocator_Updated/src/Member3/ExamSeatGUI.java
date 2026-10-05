package Member3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import SeatAllocationAlgorithm.AllocationResult;
import SeatAllocationAlgorithm.Classroom;
import SeatAllocationAlgorithm.Seat;
import SeatAllocationAlgorithm.SeatAllocator;
import SeatAllocationAlgorithm.Student;
import SeatAllocationAlgorithm.StudentDatabase;

public class ExamSeatGUI extends JFrame {

    // =========================================================
    // COLORS
    // =========================================================

    private final Color SIDEBAR = new Color(44, 62, 80);
private final Color SIDEBAR_HOVER = new Color(63, 81, 99);

private final Color BACKGROUND = new Color(245, 247, 250);
private final Color WHITE = Color.WHITE;

private final Color TEXT = new Color(45, 45, 45);
private final Color MUTED = new Color(105, 105, 105);

private final Color BLUE = new Color(52, 101, 164);
private final Color GREEN = new Color(76, 145, 95);
private final Color ORANGE = new Color(218, 145, 60);
private final Color RED = new Color(200, 70, 70);

    // =========================================================
    // DATA
    // =========================================================

    private final List<Student> students =
            new ArrayList<>();

    private Classroom classroom;

    private final SeatAllocator allocator =
            new SeatAllocator();

    private AllocationResult result;

    // =========================================================
    // GUI
    // =========================================================

    private JPanel contentPanel;

    private JLabel statusLabel;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ExamSeatGUI() {

        initializePersistentData();

        setTitle("Exam Seat Allocator");

        setSize(
                1250,
                750
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        createGUI();
    }

    // =========================================================
    // PERSISTENT DATA
    // =========================================================

    private void initializePersistentData() {

        try {
            List<Student> saved = StudentDatabase.loadStudents();

            if (saved == null) {
                loadSampleStudents();
                StudentDatabase.saveStudents(students);
            } else {
                students.clear();
                students.addAll(saved);
            }
        } catch (Exception e) {
            students.clear();
            loadSampleStudents();
        }

        classroom = loadSavedClassroom();
        saveClassroom();
    }

    private Classroom loadSavedClassroom() {

        java.nio.file.Path file =
                java.nio.file.Paths.get("data", "classroom.properties");

        try {
            if (java.nio.file.Files.exists(file)) {
                java.util.Properties properties = new java.util.Properties();

                try (java.io.InputStream input =
                             java.nio.file.Files.newInputStream(file)) {
                    properties.load(input);
                }

                String room = properties.getProperty("room", "101");
                int rows = Integer.parseInt(properties.getProperty("rows", "6"));
                int columns = Integer.parseInt(properties.getProperty("columns", "3"));

                return new Classroom(room, rows, columns);
            }
        } catch (Exception ignored) {
            // Fall back to the default classroom below.
        }

        return new Classroom("101", 6, 3);
    }

    private void saveClassroom() {

        try {
            java.nio.file.Path directory =
                    java.nio.file.Paths.get("data");

            java.nio.file.Files.createDirectories(directory);

            java.util.Properties properties = new java.util.Properties();
            properties.setProperty("room", classroom.getRoomNumber());
            properties.setProperty("rows", String.valueOf(classroom.getRows()));
            properties.setProperty("columns", String.valueOf(classroom.getColumns()));

            try (java.io.OutputStream output =
                         java.nio.file.Files.newOutputStream(
                                 directory.resolve("classroom.properties"))) {
                properties.store(output, "Exam Seat Allocator classroom settings");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save classroom settings.\n\n" + e.getMessage(),
                    "Storage Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void saveStudentData() {

        try {
            StudentDatabase.saveStudents(students);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Student was added for this session, but could not be saved permanently.\n\n"
                            + e.getMessage(),
                    "Storage Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Classroom createClassroomForCapacity(int capacity) {

        int rows = (int) Math.sqrt(capacity);

        while (rows > 1 && capacity % rows != 0) {
            rows--;
        }

        int columns = capacity / rows;

        return new Classroom("101", rows, columns);
    }

    private void configureClassroom() {

        JTextField roomField =
                new JTextField(classroom.getRoomNumber());

        JTextField capacityField =
                new JTextField(String.valueOf(classroom.getCapacity()));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Room Number:"));
        panel.add(roomField);
        panel.add(new JLabel("Classroom Capacity:"));
        panel.add(capacityField);

        int option = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Classroom Setup",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String room = roomField.getText().trim();
        int capacity;

        try {
            capacity = Integer.parseInt(capacityField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Classroom capacity must be a valid positive integer.",
                    "Invalid Capacity",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (room.isEmpty() || capacity <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid room number and capacity greater than zero.",
                    "Invalid Classroom Setup",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (students.size() > capacity) {
            JOptionPane.showMessageDialog(
                    this,
                    "Capacity cannot be less than the current student count ("
                            + students.size() + ").",
                    "Capacity Too Small",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int rows = (int) Math.sqrt(capacity);
        while (rows > 1 && capacity % rows != 0) {
            rows--;
        }
        int columns = capacity / rows;

        classroom = new Classroom(room, rows, columns);
        result = null;
        saveClassroom();

        statusLabel.setText(
                "Classroom updated: " + room + " • Capacity " + capacity
        );

        showDashboard();
    }

    // =========================================================
    // SAMPLE STUDENTS
    // =========================================================

    private void loadSampleStudents() {

        students.clear();

        // Division A

        students.add(
                new Student(
                        101,
                        "Rahul",
                        "A"
                )
        );

        students.add(
                new Student(
                        102,
                        "Aditya",
                        "A"
                )
        );

        students.add(
                new Student(
                        103,
                        "Sneha",
                        "A"
                )
        );

        students.add(
                new Student(
                        104,
                        "Neha",
                        "A"
                )
        );

        students.add(
                new Student(
                        105,
                        "Riya",
                        "A"
                )
        );

        students.add(
                new Student(
                        106,
                        "Pooja",
                        "A"
                )
        );

        // Division B

        students.add(
                new Student(
                        201,
                        "Om",
                        "B"
                )
        );

        students.add(
                new Student(
                        202,
                        "Amit",
                        "B"
                )
        );

        students.add(
                new Student(
                        203,
                        "Karan",
                        "B"
                )
        );

        students.add(
                new Student(
                        204,
                        "Akash",
                        "B"
                )
        );

        students.add(
                new Student(
                        205,
                        "Vishal",
                        "B"
                )
        );

        students.add(
                new Student(
                        206,
                        "Sahil",
                        "B"
                )
        );

        // Division C

        students.add(
                new Student(
                        301,
                        "Priya",
                        "C"
                )
        );

        students.add(
                new Student(
                        302,
                        "Kavya",
                        "C"
                )
        );

        students.add(
                new Student(
                        303,
                        "Anjali",
                        "C"
                )
        );

        students.add(
                new Student(
                        304,
                        "Simran",
                        "C"
                )
        );

        students.add(
                new Student(
                        305,
                        "Isha",
                        "C"
                )
        );

        students.add(
                new Student(
                        306,
                        "Tanvi",
                        "C"
                )
        );
    }

    // =========================================================
    // MAIN GUI
    // =========================================================

    private void createGUI() {

        getContentPane().setBackground(
                BACKGROUND
        );

        setLayout(
                new BorderLayout()
        );

        // =====================================================
        // SIDEBAR
        // =====================================================

        JPanel sidebar =
                new JPanel(
                        new BorderLayout()
                );

        sidebar.setBackground(
                SIDEBAR
        );

        sidebar.setPreferredSize(
                new Dimension(
                        245,
                        0
                )
        );

        // -----------------------------------------------------
        // LOGO
        // -----------------------------------------------------

        JPanel logoPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1
                        )
                );

        logoPanel.setBackground(
                SIDEBAR
        );

        logoPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        20,
                        20,
                        20
                )
        );

        JLabel logo =
                new JLabel(
                        "◈  EXAM ALLOCATOR"
                );

        logo.setForeground(
                WHITE
        );

        logo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        19
                )
        );

        JLabel subtitle =
                new JLabel(
                        "Smart Seating Management"
                );

        subtitle.setForeground(
                new Color(
                        150,
                        158,
                        172
                )
        );

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        logoPanel.add(logo);
        logoPanel.add(subtitle);

        sidebar.add(
                logoPanel,
                BorderLayout.NORTH
        );

        // =====================================================
        // MENU
        // =====================================================

        JPanel menu =
                new JPanel(
                        new GridLayout(
                                9,
                                1,
                                0,
                                7
                        )
                );

        menu.setBackground(
                SIDEBAR
        );

        menu.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        14,
                        10,
                        14
                )
        );

        JButton dashboard =
                createMenuButton(
                        "⌂   Dashboard"
                );

        JButton studentsButton =
                createMenuButton(
                        "♙   Students"
                );

        JButton excelButton =
                createMenuButton(
                        "⇩   Import Excel"
                );

        JButton allocateButton =
                createMenuButton(
                        "⚙   Allocate Seats"
                );

        JButton seatMapButton =
                createMenuButton(
                        "▦   Seat Map"
                );

        JButton searchButton =
                createMenuButton(
                        "⌕   Search Student"
                );

        JButton detailedButton =
                createMenuButton(
                        "☷   Detailed Result"
                );

        JButton sampleButton =
                createMenuButton(
                        "↻   Reset Sample Data"
                );

        JButton exitButton =
                createMenuButton(
                        "×   Exit"
                );

        menu.add(dashboard);
        menu.add(studentsButton);
        menu.add(excelButton);
        menu.add(allocateButton);
        menu.add(seatMapButton);
        menu.add(searchButton);
        menu.add(detailedButton);
        menu.add(sampleButton);
        menu.add(exitButton);

        sidebar.add(
                menu,
                BorderLayout.CENTER
        );

        // -----------------------------------------------------
        // FOOTER
        // -----------------------------------------------------

        JLabel footer =
                new JLabel(
                       
                );

        footer.setForeground(
                new Color(
                        130,
                        138,
                        150
                )
        );

        footer.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        footer.setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        20,
                        15,
                        10
                )
        );

        sidebar.add(
                footer,
                BorderLayout.SOUTH
        );

        add(
                sidebar,
                BorderLayout.WEST
        );

        // =====================================================
        // CONTENT
        // =====================================================

        contentPanel =
                new JPanel(
                        new BorderLayout()
                );

        contentPanel.setBackground(
                BACKGROUND
        );

        add(
                contentPanel,
                BorderLayout.CENTER
        );

        // =====================================================
        // STATUS
        // =====================================================

        statusLabel =
                new JLabel(
                        "Ready"
                );

        statusLabel.setForeground(
                MUTED
        );

        statusLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        15,
                        8,
                        15
                )
        );

        add(
                statusLabel,
                BorderLayout.SOUTH
        );

        // =====================================================
        // EVENTS
        // =====================================================

        dashboard.addActionListener(
                e -> showDashboard()
        );

        studentsButton.addActionListener(
                e -> showStudents()
        );

        excelButton.addActionListener(
                e -> importExcel()
        );

        allocateButton.addActionListener(
                e -> allocateSeats()
        );

        seatMapButton.addActionListener(
                e -> showAllocation()
        );

        searchButton.addActionListener(
                e -> searchStudent()
        );

        detailedButton.addActionListener(
                e -> showDetailedAllocation()
        );

        sampleButton.addActionListener(
                e -> resetSampleData()
        );

        exitButton.addActionListener(
                e -> exitApplication()
        );

        showDashboard();
    }

    // =========================================================
    // MENU BUTTON
    // =========================================================

    private JButton createMenuButton(
            String text) {

        JButton button =
                new JButton(text);

        button.setForeground(
                new Color(
                        220,
                        224,
                        232
                )
        );

        button.setBackground(
                SIDEBAR
        );

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setOpaque(true);

        button.setMargin(
                new Insets(
                        12,
                        15,
                        12,
                        10
                )
        );

        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private JPanel createHeader(
            String title,
            String subtitle) {

        JPanel header =
                new JPanel(
                        new GridLayout(
                                2,
                                1
                        )
                );

        header.setBackground(
                BACKGROUND
        );

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        30,
                        15,
                        30
                )
        );

        JLabel titleLabel =
                new JLabel(
                        title
                );

        titleLabel.setForeground(
                TEXT
        );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        28
                )
        );

        JLabel subLabel =
                new JLabel(
                        subtitle
                );

        subLabel.setForeground(
                MUTED
        );

        subLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        header.add(titleLabel);
        header.add(subLabel);

        return header;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(
                BACKGROUND
        );

        main.add(
                createHeader(
                        "Dashboard",
                        "Examination seating overview"
                ),
                BorderLayout.NORTH
        );

        // =====================================================
        // STAT CARDS
        // =====================================================

        JPanel cards =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                18,
                                0
                        )
                );

        cards.setBackground(
                BACKGROUND
        );

        cards.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        30,
                        25,
                        30
                )
        );

        String allocationText =
                result != null
                        && result.isSuccess()
                        ? "ALLOCATED"
                        : "PENDING";

        String allocationSub =
                result != null
                        && result.isSuccess()
                        ? students.size()
                                + " seats assigned"
                        : "Not allocated yet";

        cards.add(
                createCard(
                        "TOTAL STUDENTS",
                        String.valueOf(
                                students.size()
                        ),
                        "Registered candidates"
                )
        );

        cards.add(
                createCard(
                        "EXAM ROOM",
                        classroom.getRoomNumber(),
                        "Current examination room"
                )
        );

        cards.add(
                createCard(
                        "ROOM CAPACITY",
                        String.valueOf(
                                classroom.getCapacity()
                        ),
                        "Maximum seats"
                )
        );

        cards.add(
                createCard(
                        "ALLOCATION STATUS",
                        allocationText,
                        allocationSub
                )
        );

        // =====================================================
        // QUICK ACTIONS
        // =====================================================

        JPanel lower =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                20,
                                0
                        )
                );

        lower.setBackground(
                BACKGROUND
        );

        lower.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        30,
                        25,
                        30
                )
        );

        // Workflow card

        JPanel workflow =
                createWhitePanel();

        JLabel workflowTitle =
                new JLabel(
                        "EXAM WORKFLOW"
                );

        workflowTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        workflowTitle.setForeground(
                TEXT
        );

        JLabel workflowText =
                new JLabel(
                        "<html>"
                                + "① Import or load students"
                                + "<br><br>"
                                + "② Allocate examination seats"
                                + "<br><br>"
                                + "③ View seat map"
                                + "<br><br>"
                                + "④ Search and verify students"
                                + "</html>"
                );

        workflowText.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        workflowText.setForeground(
                MUTED
        );

        workflow.add(
                workflowTitle,
                BorderLayout.NORTH
        );

        workflow.add(
                workflowText,
                BorderLayout.CENTER
        );

        // Current status card

        JPanel current =
                createWhitePanel();

        JLabel currentTitle =
                new JLabel(
                        "CURRENT STATUS"
                );

        currentTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        currentTitle.setForeground(
                TEXT
        );

        String currentMessage;

        if (result != null
                && result.isSuccess()) {

            currentMessage =
                    "<html>"
                            + "<h2>✓ Seats Allocated</h2>"
                            + "<p>All available students have been "
                            + "processed by the allocation algorithm.</p>"
                            + "<p><b>Room:</b> "
                            + classroom.getRoomNumber()
                            + "</p>"
                            + "<p><b>Students:</b> "
                            + students.size()
                            + "</p>"
                            + "</html>";

        } else {

            currentMessage =
                    "<html>"
                            + "<h2>○ Allocation Pending</h2>"
                            + "<p>Students are loaded and ready.</p>"
                            + "<p>Click <b>Allocate Seats</b> to "
                            + "generate the examination seating plan.</p>"
                            + "</html>";
        }

        JLabel currentText =
                new JLabel(
                        currentMessage
                );

        currentText.setForeground(
                MUTED
        );

        currentText.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        current.add(
                currentTitle,
                BorderLayout.NORTH
        );

        current.add(
                currentText,
                BorderLayout.CENTER
        );

        JPanel classroomSetup = createWhitePanel();

        JLabel setupTitle = new JLabel("CLASSROOM SETUP");
        setupTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        setupTitle.setForeground(TEXT);

        JLabel setupText = new JLabel(
                "<html><b>Room:</b> " + classroom.getRoomNumber()
                        + "<br><b>Capacity:</b> " + classroom.getCapacity()
                        + "<br><br>Change the classroom capacity before allocation.</html>"
        );
        setupText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setupText.setForeground(MUTED);

        JButton setupButton = new JButton("Edit Capacity");
        setupButton.setFocusPainted(false);
        setupButton.addActionListener(e -> configureClassroom());

        classroomSetup.add(setupTitle, BorderLayout.NORTH);
        classroomSetup.add(setupText, BorderLayout.CENTER);
        classroomSetup.add(setupButton, BorderLayout.SOUTH);

        lower.add(workflow);
        lower.add(current);
        lower.add(classroomSetup);

        main.add(
                cards,
                BorderLayout.CENTER
        );

        main.add(
                lower,
                BorderLayout.SOUTH
        );

        contentPanel.add(main);

        statusLabel.setText(
                "Dashboard • "
                        + students.size()
                        + " students"
        );

        refresh();
    }

    // =========================================================
    // CARD
    // =========================================================

    private JPanel createCard(
            String title,
            String value,
            String subtitle) {

        JPanel card =
                new JPanel(
                        new BorderLayout()
                );

        card.setBackground(
                WHITE
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        225,
                                        228,
                                        235
                                )
                        ),
                        BorderFactory.createEmptyBorder(
                                18,
                                20,
                                18,
                                20
                        )
                )
        );

        JLabel titleLabel =
                new JLabel(
                        title
                );

        titleLabel.setForeground(
                MUTED
        );

        titleLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        JLabel valueLabel =
                new JLabel(
                        value
                );

        valueLabel.setForeground(
                TEXT
        );

        valueLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        26
                )
        );

        JLabel subLabel =
                new JLabel(
                        subtitle
                );

        subLabel.setForeground(
                MUTED
        );

        subLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        JPanel center =
                new JPanel(
                        new GridLayout(
                                2,
                                1
                        )
                );

        center.setBackground(
                WHITE
        );

        center.add(valueLabel);
        center.add(subLabel);

        card.add(
                titleLabel,
                BorderLayout.NORTH
        );

        card.add(
                center,
                BorderLayout.CENTER
        );

        return card;
    }

    // =========================================================
    // WHITE PANEL
    // =========================================================

    private JPanel createWhitePanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(
                WHITE
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        225,
                                        228,
                                        235
                                )
                        ),
                        BorderFactory.createEmptyBorder(
                                20,
                                22,
                                20,
                                22
                        )
                )
        );

        return panel;
    }

    // =========================================================
    // STUDENTS
    // =========================================================

    private void showStudents() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(
                BACKGROUND
        );

        JPanel topPanel =
                new JPanel(
                        new BorderLayout()
                );

        topPanel.setBackground(BACKGROUND);

        topPanel.add(
                createHeader(
                        "Students",
                        "Registered examination candidates"
                ),
                BorderLayout.CENTER
        );

        JPanel inputToolbar =
                new JPanel(
                        new FlowLayout(FlowLayout.RIGHT, 10, 8)
                );

        inputToolbar.setBackground(BACKGROUND);

        JButton addStudentButton =
                new JButton("+ Add Student");
        addStudentButton.setFocusPainted(false);
        addStudentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addStudentButton.addActionListener(e -> addStudentManually());

        JButton clearStudentsButton =
                new JButton("Clear Student Data");
        clearStudentsButton.setFocusPainted(false);
        clearStudentsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearStudentsButton.addActionListener(e -> clearStudentData());

        inputToolbar.add(addStudentButton);
        inputToolbar.add(clearStudentsButton);

        topPanel.add(
                inputToolbar,
                BorderLayout.SOUTH
        );

        main.add(
                topPanel,
                BorderLayout.NORTH
        );

        String[] columns = {
                "Roll Number",
                "Student Name",
                "Division"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        for (Student student : students) {

            model.addRow(
                    new Object[] {
                            student.getRollNo(),
                            student.getName(),
                            student.getDivision()
                    }
            );
        }

        JTable table =
                new JTable(model);

        table.setRowHeight(36);

        table.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        table.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        table.setShowGrid(false);

        JScrollPane scroll =
                new JScrollPane(table);

        JPanel wrapper =
                new JPanel(
                        new BorderLayout()
                );

        wrapper.setBackground(
                WHITE
        );

        wrapper.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        30,
                        25,
                        30
                )
        );

        wrapper.add(
                scroll,
                BorderLayout.CENTER
        );

        main.add(
                wrapper,
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        statusLabel.setText(
                students.size()
                        + " students displayed"
        );

        refresh();
    }

    // =========================================================
    // MANUAL STUDENT INPUT
    // =========================================================

    private void addStudentManually() {

        JTextField rollField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField divisionField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Roll Number:"));
        panel.add(rollField);
        panel.add(new JLabel("Student Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Division:"));
        panel.add(divisionField);

        int option = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Student",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String division = divisionField.getText().trim().toUpperCase();

        if (rollField.getText().trim().isEmpty()
                || name.isEmpty()
                || division.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Roll number, name and division are required.",
                    "Invalid Student Data",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int rollNo;
        try {
            rollNo = Integer.parseInt(rollField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Roll number must be a valid integer.",
                    "Invalid Student Data",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        for (Student student : students) {
            if (student.getRollNo() == rollNo) {
                JOptionPane.showMessageDialog(
                        this,
                        "Roll number " + rollNo + " already exists.",
                        "Duplicate Roll Number",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }

        if (students.size() >= classroom.getCapacity()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Classroom capacity is full. Maximum "
                            + classroom.getCapacity() + " students can be added.",
                    "Classroom Full",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        students.add(new Student(rollNo, name, division));
        saveStudentData();
        result = null;

        statusLabel.setText(
                "Student added successfully: " + name
        );

        showStudents();
    }

    private void clearStudentData() {

        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No student data is available to clear.",
                    "Student Data",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Clear all current student data?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        students.clear();
        saveStudentData();
        result = null;

        statusLabel.setText("Student data cleared and saved");
        showStudents();
    }

    // =========================================================
    // IMPORT EXCEL
    // =========================================================

    private void importExcel() {

        JFileChooser chooser =
                new JFileChooser();

        chooser.setDialogTitle(
                "Select Student Excel File"
        );

        int option =
                chooser.showOpenDialog(this);

        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file =
                chooser.getSelectedFile();

        if (!file.getName()
                .toLowerCase()
                .endsWith(".xlsx")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an .xlsx file.",
                    "Invalid File",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        try {

            List<Student> imported =
                    readExcelFile(file);

            if (imported.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No valid student records found.",
                        "Import Result",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int added = 0;
            int duplicates = 0;

            for (Student importedStudent : imported) {

                boolean duplicate = false;
                for (Student existing : students) {
                    if (existing.getRollNo() == importedStudent.getRollNo()) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    duplicates++;
                    continue;
                }

                if (students.size() >= classroom.getCapacity()) {
                    break;
                }

                students.add(importedStudent);
                added++;
            }

            saveStudentData();
            result = null;

            JOptionPane.showMessageDialog(
                    this,
                    added + " new students imported successfully!\n"
                            + duplicates + " duplicate roll numbers skipped.\n"
                            + "Total students: " + students.size()
                            + " / " + classroom.getCapacity(),
                    "Excel Import",
                    JOptionPane.INFORMATION_MESSAGE
            );

            statusLabel.setText(
                    added + " new students imported from Excel"
            );

            showStudents();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to read Excel file.\n\n"
                            + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // READ EXCEL
    // =========================================================

    private List<Student> readExcelFile(
            File file) throws Exception {

        List<Student> list =
                new ArrayList<>();

        try (ZipFile zip =
                     new ZipFile(file)) {

            Map<String, String> sharedStrings =
                    readSharedStrings(zip);

            ZipEntry sheetEntry =
                    zip.getEntry(
                            "xl/worksheets/sheet1.xml"
                    );

            if (sheetEntry == null) {

                throw new IOException(
                        "First worksheet not found."
                );
            }

            Document document =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(
                                    zip.getInputStream(
                                            sheetEntry
                                    )
                            );

            NodeList rows =
                    document.getElementsByTagName(
                            "row"
                    );

            for (int i = 0;
                 i < rows.getLength();
                 i++) {

                Element row =
                        (Element) rows.item(i);

                NodeList cells =
                        row.getElementsByTagName(
                                "c"
                        );

                List<String> values =
                        new ArrayList<>();

                for (int j = 0;
                     j < cells.getLength();
                     j++) {

                    Element cell =
                            (Element) cells.item(j);

                    String type =
                            cell.getAttribute("t");

                    NodeList valueNodes =
                            cell.getElementsByTagName(
                                    "v"
                            );

                    String value = "";

                    if (valueNodes.getLength() > 0) {

                        value =
                                valueNodes
                                        .item(0)
                                        .getTextContent();
                    }

                    if ("s".equals(type)
                            && !value.isEmpty()) {

                        int index =
                                Integer.parseInt(
                                        value
                                );

                        value =
                                sharedStrings
                                        .getOrDefault(
                                                String.valueOf(
                                                        index
                                                ),
                                                ""
                                        );
                    }

                    values.add(value);
                }

                // Skip Excel header
                if (i == 0) {
                    continue;
                }

                if (values.size() >= 3) {

                    try {

                        int roll =
                                Integer.parseInt(
                                        values.get(0)
                                                .trim()
                                );

                        String name =
                                values.get(1)
                                        .trim();

                        String division =
                                values.get(2)
                                        .trim();

                        if (!name.isEmpty()
                                && !division.isEmpty()) {

                            list.add(
                                    new Student(
                                            roll,
                                            name,
                                            division
                                    )
                            );
                        }

                    } catch (NumberFormatException ignored) {
                        // Ignore invalid row
                    }
                }
            }
        }

        return list;
    }

    // =========================================================
    // SHARED STRINGS
    // =========================================================

    private Map<String, String> readSharedStrings(
            ZipFile zip) throws Exception {

        Map<String, String> map =
                new HashMap<>();

        ZipEntry entry =
                zip.getEntry(
                        "xl/sharedStrings.xml"
                );

        if (entry == null) {
            return map;
        }

        Document document =
                DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(
                                zip.getInputStream(
                                        entry
                                )
                        );

        NodeList strings =
                document.getElementsByTagName(
                        "si"
                );

        for (int i = 0;
             i < strings.getLength();
             i++) {

            Element item =
                    (Element) strings.item(i);

            NodeList textNodes =
                    item.getElementsByTagName(
                            "t"
                    );

            StringBuilder text =
                    new StringBuilder();

            for (int j = 0;
                 j < textNodes.getLength();
                 j++) {

                text.append(
                        textNodes
                                .item(j)
                                .getTextContent()
                );
            }

            map.put(
                    String.valueOf(i),
                    text.toString()
            );
        }

        return map;
    }

    // =========================================================
    // ALLOCATE
    // =========================================================

    private void allocateSeats() {

        if (students.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No students available.",
                    "Allocation",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        result =
                allocator.allocate(
                        students,
                        classroom
                );

        if (result.isSuccess()) {

            statusLabel.setText(
                    "✓ Seats allocated successfully"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Seats allocated successfully!\n\n"
                            + "Students: "
                            + students.size()
                            + "\nRoom: "
                            + classroom.getRoomNumber()
                            + "\nCapacity: "
                            + classroom.getCapacity(),
                    "Allocation Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // IMPORTANT:
            // Show dashboard AFTER allocation
            // so ALLOCATED status is visible.

            showDashboard();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    result.getMessage(),
                    "Allocation Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // SEAT MAP
    // =========================================================

    private void showAllocation() {

        if (result == null
                || !result.isSuccess()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please allocate seats first.",
                    "No Allocation",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(
                BACKGROUND
        );

        main.add(
                createHeader(
                        "Seat Map",
                        "Room "
                                + classroom.getRoomNumber()
                                + " • Click a seat for details"
                ),
                BorderLayout.NORTH
        );

        JPanel seatArea =
                new JPanel(
                        new GridLayout(
                                classroom.getRows(),
                                classroom.getColumns(),
                                15,
                                15
                        )
                );

        seatArea.setBackground(
                BACKGROUND
        );

        seatArea.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        30,
                        20,
                        30
                )
        );

        for (int row = 0;
             row < classroom.getRows();
             row++) {

            for (int column = 0;
                 column < classroom.getColumns();
                 column++) {

                Seat seat =
                        classroom.getSeat(
                                row,
                                column
                        );

                JButton seatButton =
                        new JButton();

                seatButton.setFocusPainted(
                        false
                );

                seatButton.setCursor(
                        new Cursor(
                                Cursor.HAND_CURSOR
                        )
                );

                seatButton.setMargin(
                        new Insets(
                                10,
                                5,
                                10,
                                5
                        )
                );

                if (!seat.isEmpty()) {

                    Student student =
                            seat.getStudent();

                    seatButton.setText(
                            "<html><center>"
                                    + "SEAT "
                                    + (column + 1)
                                    + "<br>"
                                    + student.getName()
                                    + "<br>"
                                    + "Roll "
                                    + student.getRollNo()
                                    + " • Div "
                                    + student.getDivision()
                                    + "</center></html>"
                    );

                    seatButton.setBackground(
                            getDivisionColor(
                                    student.getDivision()
                            )
                    );

                    seatButton.setForeground(
                            WHITE
                    );

                    final int selectedRow =
                            row;

                    final int selectedColumn =
                            column;

                    seatButton.addActionListener(
                            e ->
                                    showStudentDetails(
                                            student,
                                            selectedRow,
                                            selectedColumn
                                    )
                    );

                } else {

                    seatButton.setText(
                            "EMPTY"
                    );

                    seatButton.setBackground(
                            new Color(
                                    232,
                                    235,
                                    240
                            )
                    );

                    seatButton.setForeground(
                            MUTED
                    );
                }

                seatArea.add(
                        seatButton
                );
            }
        }

        main.add(
                seatArea,
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        bottom.setBackground(
                WHITE
        );

        JLabel info =
                new JLabel(
                        "ROOM "
                                + classroom.getRoomNumber()
                                + "   |   "
                                + classroom.getRows()
                                + " Rows   |   "
                                + classroom.getColumns()
                                + " Columns   |   "
                                + "Capacity "
                                + classroom.getCapacity()
                );

        info.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        info.setForeground(
                TEXT
        );

        bottom.add(info);

        main.add(
                bottom,
                BorderLayout.SOUTH
        );

        contentPanel.add(main);

        statusLabel.setText(
                "Seat map • Allocation complete"
        );

        refresh();
    }

    // =========================================================
    // DIVISION COLORS
    // =========================================================

    private Color getDivisionColor(
            String division) {

        if (division.equalsIgnoreCase("A")) {

            return BLUE;
        }

        if (division.equalsIgnoreCase("B")) {

            return GREEN;
        }

        if (division.equalsIgnoreCase("C")) {

            return ORANGE;
        }

        return new Color(
                120,
                100,
                180
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void searchStudent() {

        if (result == null
                || !result.isSuccess()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please allocate seats first.",
                    "Search",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        JLabel label =
                new JLabel(
                        "Enter Roll Number:"
                );

        JTextField field =
                new JTextField();

        field.setPreferredSize(
                new Dimension(
                        250,
                        35
                )
        );

        panel.add(
                label,
                BorderLayout.NORTH
        );

        panel.add(
                field,
                BorderLayout.CENTER
        );

        int option =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Search Student",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            int roll =
                    Integer.parseInt(
                            field.getText().trim()
                    );

            for (int row = 0;
                 row < classroom.getRows();
                 row++) {

                for (int column = 0;
                     column < classroom.getColumns();
                     column++) {

                    Seat seat =
                            classroom.getSeat(
                                    row,
                                    column
                            );

                    if (!seat.isEmpty()) {

                        Student student =
                                seat.getStudent();

                        if (student.getRollNo()
                                == roll) {

                            showStudentDetails(
                                    student,
                                    row,
                                    column
                            );

                            return;
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Student not found.",
                    "Search Result",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid roll number.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // STUDENT DETAILS
    // =========================================================

    private void showStudentDetails(
            Student student,
            int row,
            int column) {

        JPanel card =
                new JPanel(
                        new GridLayout(
                                6,
                                1,
                                5,
                                5
                        )
                );

        card.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        15,
                        10,
                        15
                )
        );

        card.add(
                new JLabel(
                        "Roll Number : "
                                + student.getRollNo()
                )
        );

        card.add(
                new JLabel(
                        "Name : "
                                + student.getName()
                )
        );

        card.add(
                new JLabel(
                        "Division : "
                                + student.getDivision()
                )
        );

        card.add(
                new JLabel(
                        "Room : "
                                + classroom.getRoomNumber()
                )
        );

        card.add(
                new JLabel(
                        "Row : "
                                + (row + 1)
                )
        );

        card.add(
                new JLabel(
                        "Seat : "
                                + (column + 1)
                )
        );

        JOptionPane.showMessageDialog(
                this,
                card,
                "Student Seat Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // DETAILED RESULT
    // =========================================================

    private void showDetailedAllocation() {

        if (result == null
                || !result.isSuccess()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please allocate seats first.",
                    "Detailed Result",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(
                BACKGROUND
        );

        main.add(
                createHeader(
                        "Detailed Allocation",
                        "Complete student-to-seat mapping"
                ),
                BorderLayout.NORTH
        );

        String[] columns = {
                "Row",
                "Seat",
                "Roll Number",
                "Student Name",
                "Division"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        for (int row = 0;
             row < classroom.getRows();
             row++) {

            for (int column = 0;
                 column < classroom.getColumns();
                 column++) {

                Seat seat =
                        classroom.getSeat(
                                row,
                                column
                        );

                if (!seat.isEmpty()) {

                    Student student =
                            seat.getStudent();

                    model.addRow(
                            new Object[] {
                                    row + 1,
                                    column + 1,
                                    student.getRollNo(),
                                    student.getName(),
                                    student.getDivision()
                            }
                    );
                }
            }
        }

        JTable table =
                new JTable(model);

        table.setRowHeight(35);

        table.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        table.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        table.setShowGrid(false);

        JScrollPane scroll =
                new JScrollPane(table);

        JPanel wrapper =
                new JPanel(
                        new BorderLayout()
                );

        wrapper.setBackground(
                WHITE
        );

        wrapper.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        30,
                        25,
                        30
                )
        );

        wrapper.add(
                scroll,
                BorderLayout.CENTER
        );

        main.add(
                wrapper,
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        statusLabel.setText(
                "Detailed allocation displayed"
        );

        refresh();
    }

    // =========================================================
    // RESET SAMPLE DATA
    // =========================================================

    private void resetSampleData() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Reset the application to sample student data?",
                        "Reset Sample Data",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        loadSampleStudents();
        saveStudentData();

        classroom = new Classroom("101", 6, 3);
        saveClassroom();

        result = null;

        statusLabel.setText(
                "Sample data restored"
        );

        showDashboard();
    }

    // =========================================================
    // EXIT
    // =========================================================

    private void exitApplication() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to exit?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice == JOptionPane.YES_OPTION) {

            System.exit(0);
        }
    }

    // =========================================================
    // REFRESH
    // =========================================================

    private void refresh() {

        contentPanel.revalidate();

        contentPanel.repaint();
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    ExamSeatGUI gui =
                            new ExamSeatGUI();

                    gui.setVisible(true);
                }
        );
    }
}