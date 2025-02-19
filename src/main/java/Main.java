import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {
        setTitle("Job Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout(20, 20));

        // Header with Gradient Background
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(41, 128, 185), getWidth(), 0, new Color(52, 152, 219));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Welcome to Job Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 40, 40));
        mainPanel.setBackground(new Color(236, 240, 241));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Add Menu Items with Icons
        mainPanel.add(createMenuItem("Search Results", "icons/searchresult.png", this::openSearchResults));
        mainPanel.add(createMenuItem("Charts Dashboard", "icons/ChartsDashboard.png", this::openChartsDashboard));
        mainPanel.add(createMenuItem("Job Listings", "icons/joblistting.png", this::openJobListings));
        mainPanel.add(createMenuItem("Degree Prediction", "icons/prediction.png", this::openPredictionUI)); // New menu item
        mainPanel.add(createMenuItem("Settings", "icons/settings.png", this::openSettings));


        add(mainPanel, BorderLayout.CENTER);

        // Footer with Gradient Background
        JPanel footerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(52, 152, 219), getWidth(), 0, new Color(41, 128, 185));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel footerLabel = new JLabel("Job Management System © 2024");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createMenuItem(String title, String iconPath, Runnable action) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        try {
            // Use ClassLoader to load icons
            ClassLoader classLoader = getClass().getClassLoader();
            ImageIcon icon = new ImageIcon(classLoader.getResource(iconPath));
            Image scaledIcon = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(iconLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            JLabel placeholder = new JLabel("[Icon Missing]", SwingConstants.CENTER);
            placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            placeholder.setForeground(Color.GRAY);
            panel.add(placeholder, BorderLayout.CENTER);
        }

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(41, 128, 185));
        panel.add(titleLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.run(); // Execute the provided action
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(230, 230, 250));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(Color.WHITE);
            }
        });

        return panel;
    }

    private void openSearchResults() {
        SwingUtilities.invokeLater(() -> new SearchResultsWindow().setVisible(true));
    }

    private void openChartsDashboard() {
        SwingUtilities.invokeLater(() -> new ChartsWindow().setVisible(true));
    }

    private void openJobListings() {
        SwingUtilities.invokeLater(() -> new JobsUI().setVisible(true));
    }


    private void openPredictionUI() {
        SwingUtilities.invokeLater(() -> new PredictionUI().setVisible(true));
    }

    private void openSettings() {
        JOptionPane.showMessageDialog(this, "Settings Page Coming Soon!", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
