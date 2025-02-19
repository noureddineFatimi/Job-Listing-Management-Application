import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchResultsWindow extends JFrame {

    private JPanel resultsPanel;

    public SearchResultsWindow() {
        setTitle("Search Results Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());

        // Filters Section
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Results Section
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new GridLayout(0, 2, 15, 15)); // Two panels per row
        resultsPanel.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        loadAllData();
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Filters", 0, 0, new Font("Arial", Font.BOLD, 14)));
        filterPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JTextField titleField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField sectorField = new JTextField();
        JTextField degreeField = new JTextField();
        JTextField contractTypeField = new JTextField();
        JTextField remoteWorkField = new JTextField();
        JTextField dateFromField = new JTextField();
        JTextField dateToField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Job Title:"), gbc);
        gbc.gridx = 1;
        filterPanel.add(titleField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("City:"), gbc);
        gbc.gridx = 3;
        filterPanel.add(cityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Sector:"), gbc);
        gbc.gridx = 1;
        filterPanel.add(sectorField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Degree:"), gbc);
        gbc.gridx = 3;
        filterPanel.add(degreeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        filterPanel.add(new JLabel("Contract Type:"), gbc);
        gbc.gridx = 1;
        filterPanel.add(contractTypeField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Remote Work (Yes/No):"), gbc);
        gbc.gridx = 3;
        filterPanel.add(remoteWorkField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        filterPanel.add(new JLabel("Date From:"), gbc);
        gbc.gridx = 1;
        filterPanel.add(dateFromField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Date To:"), gbc);
        gbc.gridx = 3;
        filterPanel.add(dateToField, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(Color.WHITE);

        JButton applyButton = new JButton("Apply Filters");
        JButton resetButton = new JButton("Reset Filters");

        applyButton.setBackground(new Color(70, 130, 180));
        applyButton.setForeground(Color.WHITE);
        applyButton.setFont(new Font("Arial", Font.BOLD, 12));

        resetButton.setBackground(new Color(255, 69, 0));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));

        applyButton.addActionListener(e -> applyFilters(
                titleField.getText(), cityField.getText(), sectorField.getText(), degreeField.getText(),
                contractTypeField.getText(), remoteWorkField.getText(), dateFromField.getText(), dateToField.getText()
        ));

        resetButton.addActionListener(e -> {
            titleField.setText("");
            cityField.setText("");
            sectorField.setText("");
            degreeField.setText("");
            contractTypeField.setText("");
            remoteWorkField.setText("");
            dateFromField.setText("");
            dateToField.setText("");
            loadAllData();
        });

        buttonsPanel.add(applyButton);
        buttonsPanel.add(resetButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        filterPanel.add(buttonsPanel, gbc);

        return filterPanel;
    }

    private void loadAllData() {
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT titre, city, sector, contract_type, publication_date, url, remote_work FROM jobs";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            displayResults(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters(String title, String city, String sector, String degree, String contractType,
                              String remoteWork, String dateFrom, String dateTo) {
        try (Connection conn = DBConnection.connect()) {
            StringBuilder sql = new StringBuilder("SELECT titre, city, sector, contract_type, publication_date, url, remote_work FROM jobs WHERE 1=1");
            if (!title.isEmpty()) sql.append(" AND titre LIKE ?");
            if (!city.isEmpty()) sql.append(" AND city LIKE ?");
            if (!sector.isEmpty()) sql.append(" AND sector LIKE ?");
            if (!degree.isEmpty()) sql.append(" AND degree LIKE ?");
            if (!contractType.isEmpty()) sql.append(" AND contract_type LIKE ?");
            if (!remoteWork.isEmpty()) sql.append(" AND remote_work LIKE ?");
            if (!dateFrom.isEmpty()) sql.append(" AND publication_date >= ?");
            if (!dateTo.isEmpty()) sql.append(" AND publication_date <= ?");

            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int index = 1;
            if (!title.isEmpty()) stmt.setString(index++, "%" + title + "%");
            if (!city.isEmpty()) stmt.setString(index++, "%" + city + "%");
            if (!sector.isEmpty()) stmt.setString(index++, "%" + sector + "%");
            if (!degree.isEmpty()) stmt.setString(index++, "%" + degree + "%");
            if (!contractType.isEmpty()) stmt.setString(index++, "%" + contractType + "%");
            if (!remoteWork.isEmpty()) stmt.setString(index++, "%" + remoteWork + "%");
            if (!dateFrom.isEmpty()) stmt.setString(index++, dateFrom);
            if (!dateTo.isEmpty()) stmt.setString(index++, dateTo);

            ResultSet rs = stmt.executeQuery();
            displayResults(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayResults(ResultSet rs) throws SQLException {
        resultsPanel.removeAll();
        while (rs.next()) {
            String title = rs.getString("titre");
            String city = rs.getString("city");
            String sector = rs.getString("sector");
            String contractType = rs.getString("contract_type");
            String publicationDate = rs.getString("publication_date");
            String url = rs.getString("url");
            String remoteWork = rs.getString("remote_work");

            JPanel jobPanel = createJobPanel(title, city, sector, contractType, publicationDate, remoteWork, url);
            resultsPanel.add(jobPanel);
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createJobPanel(String title, String city, String sector, String contractType, String publicationDate, String remoteWork, String url) {
        JPanel jobPanel = new JPanel();
        jobPanel.setLayout(new BorderLayout(10, 10));
        jobPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        jobPanel.setBackground(Color.WHITE);
        jobPanel.setPreferredSize(new Dimension(450, 150));

        // Job Details Section
        JPanel detailsPanel = new JPanel(new GridLayout(4, 1));
        detailsPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("<html><b>" + title + "</b></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel infoLabel = new JLabel(city + " | " + sector + " | " + contractType);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel remoteWorkLabel = new JLabel("Remote Work: " + (remoteWork != null ? remoteWork : "N/A"));
        remoteWorkLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel dateLabel = new JLabel("Posted: " + publicationDate);
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        detailsPanel.add(titleLabel);
        detailsPanel.add(infoLabel);
        detailsPanel.add(remoteWorkLabel);
        detailsPanel.add(dateLabel);

        // Button to Open Link
        JButton viewMoreButton = new JButton("View More ➡");
        viewMoreButton.setBackground(new Color(70, 130, 180));
        viewMoreButton.setForeground(Color.WHITE);
        viewMoreButton.setFont(new Font("Arial", Font.BOLD, 12));
        viewMoreButton.addActionListener((ActionEvent e) -> openURL(url));

        // Add Components to Panel
        jobPanel.add(detailsPanel, BorderLayout.CENTER);
        jobPanel.add(viewMoreButton, BorderLayout.EAST);

        return jobPanel;
    }

    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open URL: " + url, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SearchResultsWindow().setVisible(true));
    }
}
