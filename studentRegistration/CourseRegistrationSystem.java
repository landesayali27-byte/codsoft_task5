import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ==========================================
// 1. DATA MODELS
// ==========================================
class Course {
    private final String code;
    private final String title;
    private final String description;
    private final int capacity;
    private final String schedule;
    private final List<String> enrolledStudentIds;

    public Course(String code, String title, String description, int capacity, String schedule) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.schedule = schedule;
        this.enrolledStudentIds = new ArrayList<>();
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public String getSchedule() { return schedule; }
    public List<String> getEnrolledStudentIds() { return enrolledStudentIds; }

    public int getAvailableSlots() {
        return capacity - enrolledStudentIds.size();
    }

    public boolean enrollStudent(String studentId) {
        if (getAvailableSlots() > 0 && !enrolledStudentIds.contains(studentId)) {
            enrolledStudentIds.add(studentId);
            return true;
        }
        return false;
    }

    public boolean dropStudent(String studentId) {
        return enrolledStudentIds.remove(studentId);
    }
}

class Student {
    private final String studentId;
    private final String name;
    private final List<Course> registeredCourses;

    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
        this.registeredCourses = new ArrayList<>();
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public List<Course> getRegisteredCourses() { return registeredCourses; }

    public boolean registerCourse(Course course) {
        if (registeredCourses.contains(course)) {
            return false;
        }
        if (course.enrollStudent(this.studentId)) {
            registeredCourses.add(course);
            return true;
        }
        return false;
    }

    public boolean dropCourse(Course course) {
        if (registeredCourses.contains(course)) {
            course.dropStudent(this.studentId);
            registeredCourses.remove(course);
            return true;
        }
        return false;
    }
}

// ==========================================
// 2. MAIN GUI APPLICATION
// ==========================================
public class CourseRegistrationSystem extends JFrame {

    // Palette Colors
    private static final Color BG_DARK = new Color(20, 22, 31);
    private static final Color CARD_BG = new Color(29, 33, 47);
    private static final Color TABLE_BG = new Color(24, 27, 39);
    private static final Color ACCENT_BLUE = new Color(79, 110, 247);
    private static final Color ACCENT_GREEN = new Color(52, 211, 153);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color TEXT_WHITE = new Color(240, 243, 250);
    private static final Color TEXT_MUTED = new Color(155, 162, 180);
    private static final Color BORDER_COLOR = new Color(45, 52, 74);

    // In-Memory Databases
    private final Map<String, Course> courseDatabase = new HashMap<>();
    private final Map<String, Student> studentDatabase = new HashMap<>();
    private Student currentStudent;

    // UI Components
    private DefaultTableModel catalogTableModel;
    private JTable catalogTable;
    private DefaultTableModel enrolledTableModel;
    private JTable enrolledTable;
    private JLabel courseDescLabel;
    private JComboBox<String> studentPicker;

    public CourseRegistrationSystem() {
        setTitle("Campus Course Registration System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));

        seedDatabase();

        // Top Navigation Banner with Add Student option
        add(createTopBanner(), BorderLayout.NORTH);

        // Center Content: Catalog & Enrolled Tables
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        contentGrid.setBackground(BG_DARK);
        contentGrid.setBorder(new EmptyBorder(0, 20, 10, 20));

        contentGrid.add(createCatalogPanel());
        contentGrid.add(createEnrolledPanel());
        add(contentGrid, BorderLayout.CENTER);

        // Bottom Syllabus / Details Panel
        add(createFooterPanel(), BorderLayout.SOUTH);

        refreshTables();
    }

    private void seedDatabase() {
        addCourse(new Course("CS101", "Introduction to Java", "Fundamentals of OOP, loops, arrays & basic classes.", 3, "Mon/Wed 09:00 - 10:30 AM"));
        addCourse(new Course("CS204", "Data Structures", "Linked lists, stacks, queues, trees, and graphs.", 2, "Tue/Thu 11:00 - 12:30 PM"));
        addCourse(new Course("CS310", "Database Management", "SQL queries, relational algebra, and normalization.", 30, "Mon/Wed 02:00 - 03:30 PM"));
        addCourse(new Course("CS402", "Operating Systems", "Process scheduling, deadlocks, and virtual memory.", 25, "Tue/Thu 03:30 - 05:00 PM"));
        addCourse(new Course("SE220", "Software Engineering", "Agile methodologies, SDLC, Git, and system design.", 20, "Fri 10:00 - 01:00 PM"));

        Student s1 = new Student("STU-101", "Sayali Lande");
        Student s2 = new Student("STU-102", "Aarav Sharma");
        studentDatabase.put(s1.getStudentId(), s1);
        studentDatabase.put(s2.getStudentId(), s2);

        currentStudent = s1;
    }

    private void addCourse(Course course) {
        courseDatabase.put(course.getCode(), course);
    }

    // ==========================================
    // TOP BANNER (WITH ADD STUDENT)
    // ==========================================
    private JPanel createTopBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(CARD_BG);
        banner.setBorder(new EmptyBorder(16, 25, 16, 25));

        JLabel title = new JLabel("CAMPUS REGISTRATION SYSTEM");
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(TEXT_WHITE);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JLabel switchLabel = new JLabel("Active Student:");
        switchLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        switchLabel.setForeground(TEXT_MUTED);

        studentPicker = new JComboBox<>();
        refreshStudentPicker();
        studentPicker.setBackground(TABLE_BG);
        studentPicker.setForeground(TEXT_WHITE);
        studentPicker.setFont(new Font("SansSerif", Font.PLAIN, 12));
        studentPicker.addActionListener(e -> {
            String selected = (String) studentPicker.getSelectedItem();
            if (selected != null && selected.contains("(") && selected.contains(")")) {
                String id = selected.substring(selected.indexOf('(') + 1, selected.indexOf(')'));
                currentStudent = studentDatabase.get(id);
                refreshTables();
            }
        });

        JButton addStudentBtn = new JButton("+ Add Student");
        styleButton(addStudentBtn, ACCENT_GREEN, Color.BLACK, 120, 32);
        addStudentBtn.addActionListener(e -> showAddStudentDialog());

        rightControls.add(switchLabel);
        rightControls.add(studentPicker);
        rightControls.add(addStudentBtn);

        banner.add(title, BorderLayout.WEST);
        banner.add(rightControls, BorderLayout.EAST);
        return banner;
    }

    // ==========================================
    // CATALOG PANEL (WITH ADD COURSE)
    // ==========================================
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Header with title and "+ Add Course"
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Available Course Catalog");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_WHITE);

        JButton addCourseBtn = new JButton("+ Add Course");
        styleButton(addCourseBtn, new Color(48, 56, 82), TEXT_WHITE, 120, 28);
        addCourseBtn.addActionListener(e -> showAddCourseDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addCourseBtn, BorderLayout.EAST);

        String[] columns = {"Code", "Title", "Schedule", "Slots"};
        catalogTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        catalogTable = styleTable(new JTable(catalogTableModel));
        catalogTable.getSelectionModel().addListSelectionListener(e -> updateDescription());

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        scrollPane.getViewport().setBackground(TABLE_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        JButton registerBtn = new JButton("Register for Selected Course");
        styleButton(registerBtn, ACCENT_BLUE, TEXT_WHITE, 200, 38);
        registerBtn.addActionListener(e -> handleRegistration());

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(registerBtn, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // ENROLLED PANEL
    // ==========================================
    private JPanel createEnrolledPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("My Enrolled Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_WHITE);

        String[] columns = {"Code", "Title", "Schedule"};
        enrolledTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        enrolledTable = styleTable(new JTable(enrolledTableModel));

        JScrollPane scrollPane = new JScrollPane(enrolledTable);
        scrollPane.getViewport().setBackground(TABLE_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        JButton dropBtn = new JButton("Drop Selected Course");
        styleButton(dropBtn, ACCENT_RED, TEXT_WHITE, 180, 38);
        dropBtn.addActionListener(e -> handleCourseDrop());

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(dropBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel descTitle = new JLabel("Course Description / Details:");
        descTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        descTitle.setForeground(TEXT_MUTED);

        courseDescLabel = new JLabel("Select a course from the catalog to read its syllabus overview.");
        courseDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        courseDescLabel.setForeground(TEXT_WHITE);

        panel.add(descTitle, BorderLayout.NORTH);
        panel.add(courseDescLabel, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(0, 20, 15, 20));
        outer.add(panel, BorderLayout.CENTER);

        return outer;
    }

    // ==========================================
    // DIALOGS: ADD STUDENT & ADD COURSE
    // ==========================================
    private void showAddStudentDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();

        Object[] fields = {
                "Student ID (e.g., STU-105):", idField,
                "Student Full Name:", nameField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add New Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Student ID and Name are required.", "Input Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (studentDatabase.containsKey(id)) {
                JOptionPane.showMessageDialog(this, "A student with ID '" + id + "' already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student newStudent = new Student(id, name);
            studentDatabase.put(id, newStudent);
            currentStudent = newStudent;

            refreshStudentPicker();
            studentPicker.setSelectedItem(name + " (" + id + ")");
            refreshTables();

            JOptionPane.showMessageDialog(this, "Student " + name + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAddCourseDialog() {
        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(20, 1, 300, 1));
        JTextField scheduleField = new JTextField("Mon/Wed 10:00 - 11:30 AM");

        Object[] fields = {
                "Course Code (e.g., CS205):", codeField,
                "Course Title:", titleField,
                "Description:", descField,
                "Seat Capacity:", capacitySpinner,
                "Class Schedule:", scheduleField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Create New Course", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim().toUpperCase();
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            int capacity = (int) capacitySpinner.getValue();
            String sched = scheduleField.getText().trim();

            if (code.isEmpty() || title.isEmpty() || sched.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Code, Title, and Schedule cannot be empty.", "Input Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (courseDatabase.containsKey(code)) {
                JOptionPane.showMessageDialog(this, "Course code '" + code + "' already exists!", "Duplicate Course", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Course newCourse = new Course(code, title, desc.isEmpty() ? "No description provided." : desc, capacity, sched);
            addCourse(newCourse);
            refreshTables();

            JOptionPane.showMessageDialog(this, "Course " + title + " (" + code + ") added to catalog!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==========================================
    // UI REFRESH & ACTIONS
    // ==========================================
    private void refreshStudentPicker() {
        studentPicker.removeAllItems();
        for (Student s : studentDatabase.values()) {
            studentPicker.addItem(s.getName() + " (" + s.getStudentId() + ")");
        }
    }

    private void refreshTables() {
        // Refresh Catalog
        catalogTableModel.setRowCount(0);
        for (Course c : courseDatabase.values()) {
            catalogTableModel.addRow(new Object[]{
                    c.getCode(),
                    c.getTitle(),
                    c.getSchedule(),
                    c.getAvailableSlots() + " / " + c.getCapacity()
            });
        }

        // Refresh Enrolled for Current Student
        enrolledTableModel.setRowCount(0);
        if (currentStudent != null) {
            for (Course c : currentStudent.getRegisteredCourses()) {
                enrolledTableModel.addRow(new Object[]{
                        c.getCode(),
                        c.getTitle(),
                        c.getSchedule()
                });
            }
        }
    }

    private void updateDescription() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow != -1) {
            String code = (String) catalogTableModel.getValueAt(selectedRow, 0);
            Course c = courseDatabase.get(code);
            if (c != null) {
                courseDescLabel.setText(c.getTitle() + " (" + c.getCode() + "): " + c.getDescription());
            }
        }
    }

    private void handleRegistration() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course from the catalog.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) catalogTableModel.getValueAt(selectedRow, 0);
        Course course = courseDatabase.get(courseCode);

        if (currentStudent.getRegisteredCourses().contains(course)) {
            JOptionPane.showMessageDialog(this, "You are already registered for " + course.getTitle() + "!", "Already Enrolled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (course.getAvailableSlots() <= 0) {
            JOptionPane.showMessageDialog(this, "Sorry, this course is full.", "Course Full", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = currentStudent.registerCourse(course);
        if (success) {
            refreshTables();
            JOptionPane.showMessageDialog(this, "Successfully registered for " + course.getTitle() + "!", "Enrolled", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleCourseDrop() {
        int selectedRow = enrolledTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an enrolled course to drop.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) enrolledTableModel.getValueAt(selectedRow, 0);
        Course course = courseDatabase.get(courseCode);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Drop " + course.getTitle() + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            currentStudent.dropCourse(course);
            refreshTables();
            JOptionPane.showMessageDialog(this, "Course dropped. Seat released back to the catalog.", "Dropped", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JTable styleTable(JTable table) {
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(48, 56, 82));
        table.setSelectionForeground(TEXT_WHITE);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setBackground(new Color(36, 42, 60));
        table.getTableHeader().setForeground(TEXT_WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        return table;
    }

    private void styleButton(JButton btn, Color bg, Color fg, int width, int height) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CourseRegistrationSystem app = new CourseRegistrationSystem();
            app.setVisible(true);
        });
    }
}