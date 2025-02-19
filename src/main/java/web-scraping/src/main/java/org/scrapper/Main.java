package org.scrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] urls = {
               "https://www.rekrute.com/",
                "https://marocemploi.net/offre/",
                 "https://www.emploi.ma/"
        };

        for (String url : urls) {
            List<scraper.Job> jobs = null;

            if (url.contains("rekrute.com")) {
                RekruteJobScraper scraper = new RekruteJobScraper();
                jobs = scraper.scrapeJobs(url);
            } else if (url.contains("marocemploi.net")) {
                RekruteJobScraper scraper = new MarocEmploiJobScraper();
                jobs = scraper.scrapeJobs(url);
            } else if (url.contains("emploi.ma")) {
                RekruteJobScraper scraper = new EmploiMaJobScraper();
                jobs = scraper.scrapeJobs(url);
            }

            if (jobs != null) {
                try {
                    // Save data to JSON file
                    ObjectMapper mapper = new ObjectMapper();
                    String fileName = getBaseUrl(url) + "_jobs.json";
                    mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), jobs);
                    System.out.println("Data saved to " + fileName);

                    // Database insertion logic
                    String jdbcUrl = "jdbc:mysql://localhost:3306/java";
                    String username = "root";
                    String password = "";
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                        String insertQuery = "INSERT INTO jobs (titre, url, site_name, publication_date, application_deadline, company_address, " +
                                "company_website, company_name, company_description, job_description, region, city, sector, profession, " +
                                "contract_type, education_level, degree, experience, required_profile, personality_traits, hard_skills, " +
                                "soft_skills, recommended_skills, language, language_level, salary, social_benefits, remote_work) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

                        for (scraper.Job job : jobs) {
                            preparedStatement.setString(1, job.getTitle());
                            preparedStatement.setString(2, job.getUrl());
                            preparedStatement.setString(3, job.getSiteName());
                            preparedStatement.setDate(4, parseDate(job.getPublicationDate(), formatter));
                            preparedStatement.setDate(5, parseDate(job.getApplicationDeadline(), formatter));
                            preparedStatement.setString(6, job.getCompanyAddress());
                            preparedStatement.setString(7, job.getCompanyWebsite());
                            preparedStatement.setString(8, job.getCompanyName());
                            preparedStatement.setString(9, job.getCompanyDescription());
                            preparedStatement.setString(10, job.getJobDescription());
                            preparedStatement.setString(11, job.getRegion());
                            preparedStatement.setString(12, job.getCity());
                            preparedStatement.setString(13, job.getSector());
                            preparedStatement.setString(14, job.getProfession());
                            preparedStatement.setString(15, job.getContractType());
                            preparedStatement.setString(16, job.getEducationLevel());
                            preparedStatement.setString(17, job.getDegree());
                            preparedStatement.setString(18, job.getExperience());
                            preparedStatement.setString(19, job.getRequiredProfile());
                            preparedStatement.setString(20, job.getPersonalityTraits());
                            preparedStatement.setString(21, job.getHardSkills());
                            preparedStatement.setString(22, job.getSoftSkills());
                            preparedStatement.setString(23, job.getRecommendedSkills());
                            preparedStatement.setString(24, job.getLanguage());
                            preparedStatement.setString(25, job.getLanguageLevel());
                            preparedStatement.setString(26, job.getSalary());
                            preparedStatement.setString(27, job.getSocialBenefits());
                            preparedStatement.setString(28, job.getRemoteWork());

                            preparedStatement.addBatch();
                        }

                        int[] rowsInserted = preparedStatement.executeBatch();
                        System.out.println(rowsInserted.length + " rows inserted into the database.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No scraper found for URL: " + url);
            }
        }
    }

    private static String getBaseUrl(String url) {
        try {
            return new java.net.URL(url).getHost();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static java.sql.Date parseDate(String dateString, DateTimeFormatter fallbackFormatter) {
        try {
            if (dateString != null && !dateString.isEmpty()) {
                // Try parsing French dates (e.g., "13 décembre 2024")
                DateTimeFormatter frenchFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
                        .withLocale(Locale.FRENCH);
                try {
                    LocalDate localDate = LocalDate.parse(dateString, frenchFormatter);
                    return java.sql.Date.valueOf(localDate);
                } catch (DateTimeParseException ignored) {
                    // Fall back to the provided formatter
                }

                // Try parsing ISO dates (e.g., "2024-12-23")
                try {
                    LocalDate localDate = LocalDate.parse(dateString);
                    return java.sql.Date.valueOf(localDate);
                } catch (DateTimeParseException ignored) {
                    // Continue to fallback formatter
                }

                // Use fallback formatter if provided
                LocalDate localDate = LocalDate.parse(dateString, fallbackFormatter);
                return java.sql.Date.valueOf(localDate);
            }
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date: " + dateString);
        }
        return null;
    }

}
