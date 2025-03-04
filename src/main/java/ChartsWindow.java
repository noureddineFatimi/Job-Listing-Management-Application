import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChartsWindow extends JFrame {

    public ChartsWindow() {
        setTitle("Charts Dashboard with Recommendations");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout(20, 20));

        // Recommendations Panel
        JPanel recommendationPanel = createRecommendationPanel();
        add(recommendationPanel, BorderLayout.NORTH);

        // Charts Panel
        JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartsPanel.setBackground(new Color(245, 245, 245));
        chartsPanel.add(createChartWithDescription("Contract Type Distribution", createPieChart()));
        chartsPanel.add(createChartWithDescription("Top Cities by Job Postings", createHorizontalCityBarChart()));
        chartsPanel.add(createChartWithDescription("Top Sectors by Job Postings", createSectorBarChart()));
        chartsPanel.add(createChartWithDescription("Top Degrees by Job Postings", createDegreePieChart()));
        add(chartsPanel, BorderLayout.CENTER);
    }

    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Recommendations"));
        panel.setBackground(new Color(236, 240, 241));

        JLabel titleLabel = new JLabel("Key Insights", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(70, 130, 180));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        JLabel topCityLabel = createStyledLabel("Top City: Loading...");
        JLabel topContractTypeLabel = createStyledLabel("Most Common Contract Type: Loading...");
        JLabel popularSectorLabel = createStyledLabel("Most Popular Sector: Loading...");
        JLabel topDegreeLabel = createStyledLabel("Most Common Degree: Loading...");

        try (Connection conn = DBConnection.connect()) {
            // Fetch Top City
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT city, COUNT(*) AS count FROM annonce_emplois GROUP BY city ORDER BY count DESC LIMIT 1");
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("city") != null) {
                topCityLabel.setText("Top City: " + rs.getString("city"));
            } else {
                topCityLabel.setText("Top City: Not Available");
            }

            // Fetch Most Common Contract Type
            stmt = conn.prepareStatement(
                    "SELECT contractType, COUNT(*) AS count FROM annonce_emplois GROUP BY contractType ORDER BY count DESC LIMIT 1");
            rs = stmt.executeQuery();
            if (rs.next() && rs.getString("contractType") != null) {
                topContractTypeLabel.setText("Most Common Contract Type: " + rs.getString("contractType"));
            } else {
                topContractTypeLabel.setText("Most Common Contract Type: Not Available");
            }

            // Fetch Most Popular Sector
            stmt = conn.prepareStatement(
                    "SELECT sector, COUNT(*) AS count FROM annonce_emplois GROUP BY sector ORDER BY count DESC LIMIT 1");
            rs = stmt.executeQuery();
            if (rs.next() && rs.getString("sector") != null) {
                popularSectorLabel.setText("Most Popular Sector: " + rs.getString("sector"));
            } else {
                popularSectorLabel.setText("Most Popular Sector: Not Available");
            }

            // Fetch Most Common Degree
            stmt = conn.prepareStatement(
                    "SELECT degree, COUNT(*) AS count FROM annonce_emplois GROUP BY degree ORDER BY count DESC LIMIT 1");
            rs = stmt.executeQuery();
            if (rs.next() && rs.getString("degree") != null) {
                topDegreeLabel.setText("Most Common Degree: " + rs.getString("degree"));
            } else {
                topDegreeLabel.setText("Most Common Degree: Not Available");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        contentPanel.add(topCityLabel);
        contentPanel.add(topContractTypeLabel);
        contentPanel.add(popularSectorLabel);
        contentPanel.add(topDegreeLabel);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(new Color(40, 55, 71));
        return label;
    }

    private JPanel createChartWithDescription(String description, JPanel chartPanel) {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        container.setBackground(Color.WHITE);

        JLabel descriptionLabel = new JLabel(description, SwingConstants.CENTER);
        descriptionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        descriptionLabel.setForeground(new Color(70, 130, 180));

        container.add(descriptionLabel, BorderLayout.NORTH);
        container.add(chartPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT contractType, COUNT(*) as count FROM annonce_emplois GROUP BY contractType";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String contractType = rs.getString("contractType");
                int count = rs.getInt("count");
                if (contractType != null && !contractType.trim().isEmpty()) {
                    dataset.setValue(contractType, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart("Contract Type Distribution", dataset, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createHorizontalCityBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT city, COUNT(*) as count FROM annonce_emplois GROUP BY city ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String city = rs.getString("city");
                int count = rs.getInt("count");
                if (city != null && !city.trim().isEmpty()) {
                    dataset.addValue(count, "Job Postings", city);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart("Top Cities by Job Postings", "City", "Job Count", dataset,
                PlotOrientation.HORIZONTAL, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createSectorBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT sector, COUNT(*) as count FROM annonce_emplois GROUP BY sector ORDER BY count DESC LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sector = rs.getString("sector");
                int count = rs.getInt("count");
                if (sector != null && !sector.trim().isEmpty()) {
                    dataset.addValue(count, "Job Postings", sector);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart("Top Sectors by Job Postings", "Sector", "Job Count", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        return new ChartPanel(chart);
    }

    private JPanel createDegreePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT degree, COUNT(*) as count FROM annonce_emplois GROUP BY degree";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String degree = rs.getString("degree");
                int count = rs.getInt("count");
                if (degree != null && !degree.trim().isEmpty()) {
                    dataset.setValue(degree, count);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart("Degree Distribution", dataset, true, true, false);
        return new ChartPanel(chart);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChartsWindow().setVisible(true));
    }
}
