package org.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class EmploiMaJobScraper extends RekruteJobScraper {

private static final String DB_URL = "jdbc:mysql://localhost:3306/java";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "";

public List<scraper.Job> scrapeJobs(String url) {
int page = 1;

try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
System.out.println("Connected to the database.");

while (true) {
String paginatedUrl = url+"recherche-jobs-maroc?page="+ page;
System.out.println("Fetching URL: " + paginatedUrl);

Document doc = Jsoup.connect(paginatedUrl).get();
Elements jobCards = doc.select(".card.card-job");

if (jobCards.isEmpty()) {
break; // Exit when no more jobs are found
}

for (Element card : jobCards) {
scraper.Job job = new scraper.Job();
    Element titleElement = card.selectFirst("h3 a");
    if (titleElement != null) {
        job.setTitle(titleElement.attr("title"));
        job.setUrl("https://www.emploi.ma" + titleElement.attr("href"));
    }

    // Extract company name
    Element companyElement = card.selectFirst(".card-job-company");
    if (companyElement != null) {
        job.setCompanyName(companyElement.text());
    }

    // Extract job description
    Element descriptionElement = card.selectFirst(".card-job-description p");
    if (descriptionElement != null) {
        job.setJobDescription(descriptionElement.text());
    }

    // Extract additional job details
    Elements details = card.select("ul li");
    for (Element detail : details) {
        String text = detail.text();

        if (text.contains("Niveau d´études requis")) {
            job.setDegree(detail.select("strong").text());
        } else if (text.contains("Niveau d'expérience")) {
            job.setExperience(detail.select("strong").text());
        } else if (text.contains("Contrat proposé")) {
            job.setContractType(detail.select("strong").text());
        } else if (text.contains("Région de")) {
            job.setCity(detail.select("strong").text());
        } else if (text.contains("Compétences clés")) {
            job.setRecommendedSkills(detail.select("strong").text());
        }
    }

    // Extract publication date
    Element dateElement = card.selectFirst("time");
    if (dateElement != null) {
        job.setPublicationDate(dateElement.attr("datetime"));
    }

    jobs.add(job);
}

    page++; // Increment the page number
}
} catch (Exception e) {
    e.printStackTrace();
}

    return jobs;
}

private void fetchAdditionalDetails(scraper.Job job) {
try {
Document doc = Jsoup.connect(job.getUrl()).get();

job.setCompanyDescription(doc.selectFirst("#recruiterDescription").text());
job.setCompanyAddress(doc.selectFirst("#address").text());

Elements skillElements = doc.select(".tagSkills");
if (!skillElements.isEmpty()) {
StringBuilder skills = new StringBuilder();
for (Element skill : skillElements) {
skills.append(skill.text()).append(", ");
}
job.setHardSkills(skills.toString());
}

// Fetch more fields as per requirement...
} catch (Exception e) {
System.err.println("Failed to fetch additional details for: " + job.getUrl());
e.printStackTrace();
}
}

private void insertJobIntoDatabase(Connection connection, scraper.Job job) {
String insertQuery = "INSERT INTO jobs (titre, url, site_name, publication_date, company_name, job_description, region, city, education_level, experience, contract_type, recommended_skills, company_description, company_address, hard_skills) "
+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
preparedStatement.setString(1, job.getTitle());
preparedStatement.setString(2, job.getUrl());
preparedStatement.setString(3, job.getSiteName());
preparedStatement.setString(4, job.getPublicationDate());
preparedStatement.setString(5, job.getCompanyName());
preparedStatement.setString(6, job.getJobDescription());
preparedStatement.setString(7, job.getRegion());
preparedStatement.setString(8, job.getCity());
preparedStatement.setString(9, job.getEducationLevel());
preparedStatement.setString(10, job.getExperience());
preparedStatement.setString(11, job.getContractType());
preparedStatement.setString(12, job.getRecommendedSkills());
preparedStatement.setString(13, job.getCompanyDescription());
preparedStatement.setString(14, job.getCompanyAddress());
preparedStatement.setString(15, job.getHardSkills());

preparedStatement.executeUpdate();
System.out.println("Inserted job: " + job.getTitle());
} catch (Exception e) {
e.printStackTrace();
}
}
}
