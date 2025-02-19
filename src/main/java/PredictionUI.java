import machinelearning.ml.ML;
import weka.classifiers.trees.J48;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;

public class PredictionUI extends JFrame {
    private JTextField publicationDateField;
    private JTextField applicationDeadlineField;
    private JTextField cityField;
    private JTextField contractTypeField;
    private JTextField experienceField;
    private JTextField remoteWorkField;
    private JTextField sectorField;
    private JLabel resultLabel;

    public PredictionUI() {
        setTitle("Degree Prediction");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 204, 0), getWidth(), 0, new Color(0, 204, 153));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(600, 80));
        JLabel headerLabel = new JLabel("Predict Required Degree", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {
                "Publication Date (yyyy-MM-dd):", "Application Deadline (yyyy-MM-dd):",
                "City:", "Contract Type:", "Experience:",
                "Remote Work (Oui/Non/Hybride):", "Sector:"
        };

        JTextField[] fields = {
                publicationDateField = new JTextField(), applicationDeadlineField = new JTextField(),
                cityField = new JTextField(), contractTypeField = new JTextField(),
                experienceField = new JTextField(), remoteWorkField = new JTextField(),
                sectorField = new JTextField()
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.4;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            inputPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.6;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            inputPanel.add(fields[i], gbc);
        }

        // Predict Button
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton predictButton = new JButton("Predict");
        predictButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        predictButton.setBackground(new Color(0, 204, 153));
        predictButton.setForeground(Color.WHITE);
        predictButton.setFocusPainted(false);
        predictButton.addActionListener(this::predictDegree);
        buttonPanel.add(predictButton);

        JButton clusterButton = new JButton("View Clustering");
        clusterButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        clusterButton.setBackground(new Color(0, 102, 204));
        clusterButton.setForeground(Color.WHITE);
        clusterButton.setFocusPainted(false);
        clusterButton.addActionListener(this::showClustering);
        buttonPanel.add(clusterButton);

        inputPanel.add(buttonPanel, gbc);

        add(inputPanel, BorderLayout.CENTER);

        // Result Panel
        JPanel resultPanel = new JPanel();
        resultPanel.setPreferredSize(new Dimension(600, 60));
        resultPanel.setBackground(Color.WHITE);
        resultLabel = new JLabel("Predicted Degree: ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(new Color(0, 153, 51));
        resultPanel.add(resultLabel);
        add(resultPanel, BorderLayout.SOUTH);
    }

    private void predictDegree(ActionEvent e) {
        String publicationDate = publicationDateField.getText();
        String applicationDeadline = applicationDeadlineField.getText();
        String city = cityField.getText();
        String contractType = contractTypeField.getText();
        String experience = experienceField.getText();
        String remoteWork = remoteWorkField.getText();
        String sector = sectorField.getText();

        try {
            Connection con = ML.connectToDatabase("jdbc:mysql://localhost:3306/java", "root", "");
            Instances dataSet = ML.prepareData(ML.getAnnonceEmplois(con));
            J48 model = ML.trainJ48Model(dataSet);
            Instance instance = ML.createInstance(dataSet, publicationDate, applicationDeadline, city, contractType, experience, remoteWork, sector);
            String predictedDegree = ML.predict(model, instance);

            resultLabel.setText("Predicted Degree: " + predictedDegree);
            resultLabel.setForeground(new Color(0, 153, 51));
        } catch (Exception ex) {
            resultLabel.setText("Error: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    private void showClustering(ActionEvent e) {
        try {
            Connection con = ML.connectToDatabase("jdbc:mysql://localhost:3306/java", "root", "");
            Instances dataSet = ML.prepareData(ML.getAnnonceEmplois(con));
            EM clusterer = ML.performClustering(dataSet);
            ML.plotClusters(dataSet, clusterer);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error during clustering: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PredictionUI predictionUI = new PredictionUI();
            predictionUI.setVisible(true);
        });
    }
}
