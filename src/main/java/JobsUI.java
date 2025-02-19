import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class JobsUI extends JFrame {
    private JTable table;
    private JComboBox<String> filterDropdown;
    private JPanel dynamicFilterPanel;
    private JButton addFilterButton, applyFiltersButton, resetFiltersButton;

    // Map to store filter fields and their corresponding input fields
    private Map<String, JTextField> activeFilters = new HashMap<>();

    public JobsUI() {
        setTitle("Job Listings Viewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 800);
        setLayout(new BorderLayout());


        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Job Listings Viewer");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        table = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(table);
        add(tableScrollPane, BorderLayout.CENTER);

        // Filters Panel
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        // Dropdown for Selecting Filters
        filterDropdown = new JComboBox<>(new String[]{
                "Job Title", "URL", "Site Name", "Publication Date", "Application Deadline",
                "Company Address", "Company Website", "Company Name", "Company Description",
                "Job Description", "Region", "City", "Sector", "Profession", "Contract Type",
                "Education Level", "Degree", "Experience", "Required Profile", "Personality Traits",
                "Hard Skills", "Soft Skills", "Recommended Skills", "Language", "Language Level",
                "Salary", "Social Benefits", "Remote Work"
        });
        addFilterButton = new JButton("Add Filter");
        addFilterButton.setBackground(new Color(70, 130, 180));
        addFilterButton.setForeground(Color.WHITE);

        JPanel topFilterPanel = new JPanel();
        topFilterPanel.add(filterDropdown);
        topFilterPanel.add(addFilterButton);

        filterPanel.add(topFilterPanel, BorderLayout.NORTH);

        // Dynamic Filter Panel
        dynamicFilterPanel = new JPanel();
        dynamicFilterPanel.setLayout(new BoxLayout(dynamicFilterPanel, BoxLayout.Y_AXIS));
        JScrollPane dynamicFilterScrollPane = new JScrollPane(dynamicFilterPanel);
        filterPanel.add(dynamicFilterScrollPane, BorderLayout.CENTER);

        // Apply and Reset Buttons
        applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.setBackground(new Color(34, 139, 34));
        applyFiltersButton.setForeground(Color.WHITE);

        resetFiltersButton = new JButton("Reset Filters");
        resetFiltersButton.setBackground(new Color(255, 69, 0));
        resetFiltersButton.setForeground(Color.WHITE);

        JPanel bottomFilterPanel = new JPanel();
        bottomFilterPanel.add(applyFiltersButton);
        bottomFilterPanel.add(resetFiltersButton);
        filterPanel.add(bottomFilterPanel, BorderLayout.SOUTH);

        add(filterPanel, BorderLayout.WEST);

        // Load Initial Data
        loadTableData("");

        // Add Filter Button Action
        addFilterButton.addActionListener(e -> addFilter());

        // Apply Filters Button Action
        applyFiltersButton.addActionListener(e -> applyFilters());

        // Reset Filters Button Action
        resetFiltersButton.addActionListener(e -> resetFilters());
    }

    private void addFilter() {
        String selectedField = (String) filterDropdown.getSelectedItem();
        if (!activeFilters.containsKey(selectedField)) {
            JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Added spacing
            JLabel filterLabel = new JLabel(selectedField + ":");
            JTextField filterField = new JTextField(20);
            filterRow.add(filterLabel);
            filterRow.add(filterField);

            dynamicFilterPanel.add(filterRow);
            dynamicFilterPanel.revalidate();
            dynamicFilterPanel.repaint();

            activeFilters.put(selectedField, filterField);
        } else {
            JOptionPane.showMessageDialog(this, "Filter already added!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilters() {
        StringBuilder query = new StringBuilder("SELECT * FROM jobs WHERE 1=1");
        try (Connection conn = DBConnection.connect()) {
            PreparedStatement stmt;
            int paramIndex = 1;

            for (Map.Entry<String, JTextField> entry : activeFilters.entrySet()) {
                String field = mapFieldToColumn(entry.getKey());
                String value = entry.getValue().getText();
                if (!value.isEmpty()) {
                    query.append(" AND ").append(field).append(" LIKE ?");
                }
            }

            stmt = conn.prepareStatement(query.toString());

            for (Map.Entry<String, JTextField> entry : activeFilters.entrySet()) {
                String value = entry.getValue().getText();
                if (!value.isEmpty()) {
                    stmt.setString(paramIndex++, "%" + value + "%");
                }
            }

            ResultSet rs = stmt.executeQuery();
            updateTable(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetFilters() {
        activeFilters.clear();
        dynamicFilterPanel.removeAll();
        dynamicFilterPanel.revalidate();
        dynamicFilterPanel.repaint();
        loadTableData("");
    }

    private void loadTableData(String condition) {
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT * FROM jobs " + condition;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            updateTable(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        table.setModel(model);
    }

    private String mapFieldToColumn(String field) {
        switch (field) {
            case "Job Title":
                return "titre";
            case "URL":
                return "url";
            case "Site Name":
                return "site_name";
            case "Publication Date":
                return "publication_date";
            case "Application Deadline":
                return "application_deadline";
            case "Company Address":
                return "company_address";
            case "Company Website":
                return "company_website";
            case "Company Name":
                return "company_name";
            case "Company Description":
                return "company_description";
            case "Job Description":
                return "job_description";
            case "Region":
                return "region";
            case "City":
                return "city";
            case "Sector":
                return "sector";
            case "Profession":
                return "profession";
            case "Contract Type":
                return "contract_type";
            case "Education Level":
                return "education_level";
            case "Degree":
                return "degree";
            case "Experience":
                return "experience";
            case "Required Profile":
                return "required_profile";
            case "Personality Traits":
                return "personality_traits";
            case "Hard Skills":
                return "hard_skills";
            case "Soft Skills":
                return "soft_skills";
            case "Recommended Skills":
                return "recommended_skills";
            case "Language":
                return "language";
            case "Language Level":
                return "language_level";
            case "Salary":
                return "salary";
            case "Social Benefits":
                return "social_benefits";
            case "Remote Work":
                return "remote_work";
            default:
                return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JobsUI().setVisible(true));
    }
}
