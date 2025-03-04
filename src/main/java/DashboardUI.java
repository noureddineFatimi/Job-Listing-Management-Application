import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class DashboardUI extends JFrame {
    private JTable searchTable;

    public DashboardUI() {
        setTitle("Job Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1600, 1000);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("Job Dashboard");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.CENTER);

        // Chart Panel
        JPanel chartPanel = new JPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Charts"));
        chartPanel.setLayout(new GridLayout(3, 2, 10, 10));

        JPanel pieChart = createPieChart();
        JPanel cityBarChart = createHorizontalCityBarChart();
        JPanel sectorBarChart = createSectorBarChart();
        JPanel companyBarChart = createCompanyBarChart();
        JPanel degreePieChart = createDegreePieChart();
        JPanel remoteWorkPieChart = createRemoteWorkPieChart();

        if (pieChart != null) chartPanel.add(pieChart);
        if (cityBarChart != null) chartPanel.add(cityBarChart);
        if (sectorBarChart != null) chartPanel.add(sectorBarChart);
        if (companyBarChart != null) chartPanel.add(companyBarChart);
        if (degreePieChart != null) chartPanel.add(degreePieChart);
        if (remoteWorkPieChart != null) chartPanel.add(remoteWorkPieChart);

        JPanel chartSection = createCollapsiblePanel("Charts", chartPanel);
        mainPanel.add(chartSection);

        // Recommendations Panel
        JPanel recommendationPanel = createRecommendationPanel();
        JPanel recommendationSection = createCollapsiblePanel("Recommendations", recommendationPanel);
        mainPanel.add(recommendationSection);

        // Filter Section
        JPanel filterPanel = createFilterPanel();
        JPanel filterSection = createCollapsiblePanel("Filters", filterPanel);
        mainPanel.add(filterSection);

        // Search Results Panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));
        searchTable = new JTable();
        styleSearchTable(searchTable);
        JScrollPane searchScrollPane = new JScrollPane(searchTable);
        searchPanel.add(searchScrollPane, BorderLayout.CENTER);
        JPanel searchSection = createCollapsiblePanel("Search Results", searchPanel);
        mainPanel.add(searchSection);

        // Load Initial Data
        loadAllData();
    }

    private void styleSearchTable(JTable searchTable) {
    }

    private JPanel createCollapsiblePanel(String title, JPanel contentPanel) {
        JPanel collapsiblePanel = new JPanel();
        collapsiblePanel.setLayout(new BorderLayout());

        JButton toggleButton = new JButton("Toggle " + title);
        toggleButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggleButton.setBackground(new Color(70, 130, 180));
        toggleButton.setForeground(Color.WHITE);
        toggleButton.setFocusPainted(false);

        collapsiblePanel.add(toggleButton, BorderLayout.NORTH);
        collapsiblePanel.add(contentPanel, BorderLayout.CENTER);

        contentPanel.setVisible(true); // Initially visible
        toggleButton.addActionListener(e -> contentPanel.setVisible(!contentPanel.isVisible()));

        return collapsiblePanel;
    }

    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Recommendations"));
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel sectorLabel = new JLabel("<html><b>Top Sector:</b> Not Available</html>");
        JLabel companyLabel = new JLabel("<html><b>Top Hiring Company:</b> Not Available</html>");
        JLabel cityLabel = new JLabel("<html><b>Top City for Jobs:</b> Not Available</html>");
        JLabel degreeLabel = new JLabel("<html><b>Most Common Degree:</b> Not Available</html>");

        try (Connection conn = DBConnection.connect()) {
            // Top Sector
            String topSectorSql = "SELECT sector, COUNT(*) as count FROM jobs WHERE sector IS NOT NULL AND TRIM(sector) != '' GROUP BY sector ORDER BY count DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(topSectorSql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String sector = rs.getString("sector");
                sectorLabel.setText("<html><b>Top Sector:</b> " + sector + "</html>");
            }

            // Top Company
            String topCompanySql = "SELECT company_name, COUNT(*) as count FROM jobs WHERE company_name IS NOT NULL AND TRIM(company_name) != '' GROUP BY company_name ORDER BY count DESC LIMIT 1";
            stmt = conn.prepareStatement(topCompanySql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String company = rs.getString("company_name");
                companyLabel.setText("<html><b>Top Hiring Company:</b> " + company + "</html>");
            }

            // Top City
            String topCitySql = "SELECT city, COUNT(*) as count FROM jobs WHERE city IS NOT NULL AND TRIM(city) != '' GROUP BY city ORDER BY count DESC LIMIT 1";
            stmt = conn.prepareStatement(topCitySql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String city = rs.getString("city");
                cityLabel.setText("<html><b>Top City for Jobs:</b> " + city + "</html>");
            }

            // Most Common Degree
            String topDegreeSql = "SELECT degree, COUNT(*) as count FROM jobs WHERE degree IS NOT NULL AND TRIM(degree) != '' GROUP BY degree ORDER BY count DESC LIMIT 1";
            stmt = conn.prepareStatement(topDegreeSql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String degree = rs.getString("degree");
                degreeLabel.setText("<html><b>Most Common Degree:</b> " + degree + "</html>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add Labels
        panel.add(sectorLabel);
        panel.add(companyLabel);
        panel.add(cityLabel);
        panel.add(degreeLabel);

        return panel;
    }

    private JPanel createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT contract_type, COUNT(*) as count FROM jobs GROUP BY contract_type";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String contractType = rs.getString("contract_type");
                int count = rs.getInt("count");

                if (contractType != null && !contractType.trim().isEmpty()) {
                    dataset.setValue(contractType, count);
                    hasData = true;
                }
            }

            if (!hasData) {
                return null; // No data for the chart
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        }

        JFreeChart chart = ChartFactory.createPieChart("Contract Type Distribution", dataset, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createHorizontalCityBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT city, COUNT(*) as count FROM jobs GROUP BY city ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String city = rs.getString("city");
                int count = rs.getInt("count");

                if (city != null && !city.trim().isEmpty()) {
                    dataset.addValue(count, "Job Postings", city);
                    hasData = true;
                }
            }

            if (!hasData) {
                return null; // No data for the chart
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Cities by Job Postings",
                "City",
                "Number of Postings",
                dataset,
                PlotOrientation.HORIZONTAL,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }

    private JPanel createSectorBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT sector, COUNT(*) as count FROM jobs GROUP BY sector ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String sector = rs.getString("sector");
                int count = rs.getInt("count");

                if (sector != null && !sector.trim().isEmpty()) {
                    dataset.addValue(count, "Job Postings", sector);
                    hasData = true;
                }
            }

            if (!hasData) {
                return null; // No data for the chart
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Sectors by Job Postings",
                "Sector",
                "Number of Postings",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }

    private JPanel createCompanyBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT company_name, COUNT(*) as count FROM jobs GROUP BY company_name ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String company = rs.getString("company_name");
                int count = rs.getInt("count");

                if (company != null && !company.trim().isEmpty()) {
                    dataset.addValue(count, "Job Postings", company);
                    hasData = true;
                }
            }

            if (!hasData) {
                return null; // No data for the chart
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Companies by Job Postings",
                "Company",
                "Number of Postings",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }

    private JPanel createDegreePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT degree, COUNT(*) as count FROM jobs GROUP BY degree";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            int otherCount = 0;
            boolean hasData = false;
            while (rs.next()) {
                String degree = rs.getString("degree");
                int count = rs.getInt("count");

                if (degree == null || degree.trim().isEmpty()) {
                    degree = "Unknown";
                }

                if (count < 5) {
                    otherCount += count;
                } else {
                    dataset.setValue(degree, count);
                    hasData = true;
                }
            }

            if (otherCount > 0) {
                dataset.setValue("Other", otherCount);
                hasData = true;
            }

            if (!hasData) {
                return null; // No data for the chart
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        }

        JFreeChart chart = ChartFactory.createPieChart("Jobs by Degree Level", dataset, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createRemoteWorkPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT remote_work, COUNT(*) as count FROM jobs GROUP BY remote_work";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            while (rs.next()) {
                String remoteWork = rs.getString("remote_work");
                int count = rs.getInt("count");

                if (remoteWork != null && !remoteWork.trim().isEmpty()) {
                    dataset.setValue(remoteWork, count);
                    hasData = true;
                }
            }

            if (!hasData) {
                return null; // No data for the chart
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        }

        JFreeChart chart = ChartFactory.createPieChart("Remote vs On-Site Jobs", dataset, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        filterPanel.setLayout(new GridLayout(5, 4, 10, 10));

        JTextField titleField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField contractField = new JTextField();
        JTextField companyField = new JTextField();
        JTextField sectorField = new JTextField();
        JTextField degreeField = new JTextField();
        JTextField remoteField = new JTextField();
        JTextField dateFromField = new JTextField();
        JTextField dateToField = new JTextField();

        filterPanel.add(new JLabel("Job Title:"));
        filterPanel.add(titleField);
        filterPanel.add(new JLabel("City:"));
        filterPanel.add(cityField);
        filterPanel.add(new JLabel("Contract Type:"));
        filterPanel.add(contractField);
        filterPanel.add(new JLabel("Company:"));
        filterPanel.add(companyField);
        filterPanel.add(new JLabel("Sector:"));
        filterPanel.add(sectorField);
        filterPanel.add(new JLabel("Degree:"));
        filterPanel.add(degreeField);
        filterPanel.add(new JLabel("Remote Work:"));
        filterPanel.add(remoteField);
        filterPanel.add(new JLabel("Date From:"));
        filterPanel.add(dateFromField);
        filterPanel.add(new JLabel("Date To:"));
        filterPanel.add(dateToField);

        JButton applyFiltersButton = new JButton("Apply Filters");
        JButton resetFiltersButton = new JButton("Reset Filters");
        filterPanel.add(applyFiltersButton);
        filterPanel.add(resetFiltersButton);

        applyFiltersButton.addActionListener(e -> applyFilters(titleField.getText(), cityField.getText(),
                contractField.getText(), companyField.getText(), sectorField.getText(),
                degreeField.getText(), remoteField.getText(),
                dateFromField.getText(), dateToField.getText()));

        resetFiltersButton.addActionListener(e -> {
            titleField.setText("");
            cityField.setText("");
            contractField.setText("");
            companyField.setText("");
            sectorField.setText("");
            degreeField.setText("");
            remoteField.setText("");
            dateFromField.setText("");
            dateToField.setText("");
            loadAllData();
        });

        return filterPanel;
    }

    private void applyFilters(String title, String city, String contract, String company, String sector, String degree, String remote, String dateFrom, String dateTo) {
        try (Connection conn = DBConnection.connect()) {
            StringBuilder sql = new StringBuilder("SELECT titre, city, contract_type, company_name, sector, degree, remote_work, publication_date, url FROM jobs WHERE 1=1");
            if (!title.isEmpty()) sql.append(" AND titre LIKE ?");
            if (!city.isEmpty()) sql.append(" AND city LIKE ?");
            if (!contract.isEmpty()) sql.append(" AND contract_type LIKE ?");
            if (!company.isEmpty()) sql.append(" AND company_name LIKE ?");
            if (!sector.isEmpty()) sql.append(" AND sector LIKE ?");
            if (!degree.isEmpty()) sql.append(" AND degree LIKE ?");
            if (!remote.isEmpty()) sql.append(" AND remote_work LIKE ?");
            if (!dateFrom.isEmpty()) sql.append(" AND publication_date >= ?");
            if (!dateTo.isEmpty()) sql.append(" AND publication_date <= ?");

            PreparedStatement stmt = conn.prepareStatement(sql.toString());

            int index = 1;
            if (!title.isEmpty()) stmt.setString(index++, "%" + title + "%");
            if (!city.isEmpty()) stmt.setString(index++, "%" + city + "%");
            if (!contract.isEmpty()) stmt.setString(index++, "%" + contract + "%");
            if (!company.isEmpty()) stmt.setString(index++, "%" + company + "%");
            if (!sector.isEmpty()) stmt.setString(index++, "%" + sector + "%");
            if (!degree.isEmpty()) stmt.setString(index++, "%" + degree + "%");
            if (!remote.isEmpty()) stmt.setString(index++, "%" + remote + "%");
            if (!dateFrom.isEmpty()) stmt.setString(index++, dateFrom);
            if (!dateTo.isEmpty()) stmt.setString(index++, dateTo);

            ResultSet rs = stmt.executeQuery();
            updateTable(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllData() {
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT titre, city, contract_type, company_name, sector, degree, remote_work, publication_date, url FROM jobs";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            updateTable(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTable(ResultSet rs) throws SQLException {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Job Title");
        columnNames.add("City");
        columnNames.add("Contract Type");
        columnNames.add("Company");
        columnNames.add("Sector");
        columnNames.add("Degree");
        columnNames.add("Remote Work");
        columnNames.add("Publication Date");
        columnNames.add("URL");

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            row.add(rs.getString("titre"));
            row.add(rs.getString("city"));
            row.add(rs.getString("contract_type"));
            row.add(rs.getString("company_name"));
            row.add(rs.getString("sector"));
            row.add(rs.getString("degree"));
            row.add(rs.getString("remote_work"));
            row.add(rs.getString("publication_date"));
            row.add(rs.getString("url"));
            data.add(row);
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        searchTable.setModel(model);

        // Add click listener for the URL column
        searchTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = searchTable.rowAtPoint(e.getPoint());
                int col = searchTable.columnAtPoint(e.getPoint());

                // Check if the clicked column is the URL column
                if (col == 8) { // URL column index
                    String url = (String) searchTable.getValueAt(row, col);
                    if (url != null && !url.trim().isEmpty()) {
                        try {
                            // Open the URL in the default browser
                            Desktop.getDesktop().browse(new URI(url));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    searchTable,
                                    "Failed to open URL: " + url,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardUI().setVisible(true));
    }
}
