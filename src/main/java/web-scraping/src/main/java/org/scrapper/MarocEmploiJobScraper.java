package org.scrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MarocEmploiJobScraper extends RekruteJobScraper {

    @Override
    public List<scraper.Job> scrapeJobs(String url) {
        List<scraper.Job> jobs = new ArrayList<>();
        int page = 1;

        try {
            while (true) {
                String paginatedUrl = url+"/?ajax_filter=true&job_page="+page;
                System.out.println("Fetching URL: " + paginatedUrl);
                Document doc = Jsoup.connect(paginatedUrl).get();

                // Select all job postings
                Elements jobCards = doc.select(".jobsearch-joblisting-classic-wrap");
                if (jobCards.isEmpty()) {
                    break; // Exit the loop if no jobs are found
                }

                for (Element card : jobCards) {
                    scraper.Job job = new scraper.Job();
                    job.setSiteName("Maroc Emploi");

                    // Extract basic job details
                    Element titleElement = card.selectFirst(".jobsearch-pst-title a");
                    if (titleElement != null) {
                        job.setTitle(titleElement.text());
                        job.setUrl(titleElement.attr("href"));
                    }

                    Element companyElement = card.selectFirst(".job-company-name a");
                    if (companyElement != null) {
                        job.setCompanyName(companyElement.text());
                    }

                    Element locationElement = card.selectFirst(".jobsearch-icon.jobsearch-maps-and-flags ~ li");
                    if (locationElement != null) {
                        job.setCity(locationElement.text());
                    }

                    Element jobTypeElement = card.selectFirst(".jobsearch-option-btn");
                    if (jobTypeElement != null) {
                        job.setContractType(jobTypeElement.text());
                    }

                    Element dateElement = card.selectFirst(".jobsearch-icon.jobsearch-calendar ~ li");
                    if (dateElement != null) {
                        job.setPublicationDate(dateElement.text());
                    }

                    Element sectorElement = card.selectFirst(".jobsearch-icon.jobsearch-filter-tool-black-shape ~ a");
                    if (sectorElement != null) {
                        job.setSector(sectorElement.text());
                    }

                    // Fetch additional details from the job post link
                    if (job.getUrl() != null) {
                        scrapeJobDetails(job);
                    }

                    jobs.add(job);
                }

                page++; // Increment page number
            }

            // Save extracted jobs to JSON
            saveJobsToJson(jobs, "marocemploi_jobs.json");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }

    private void scrapeJobDetails(scraper.Job job) {
        try {
            Document doc = Jsoup.connect(job.getUrl()).get();

            // Extract job description
            Element descriptionElement = doc.selectFirst(".jobsearch-description");
            if (descriptionElement != null) {
                job.setJobDescription(descriptionElement.text());
            }

            // Extract publication and application deadlines
            Elements dateElements = doc.select(".jobsearch-jobdetail-options li");
            for (Element dateElement : dateElements) {
                String text = dateElement.text();
                if (text.contains("Date de Parution")) {
                    job.setPublicationDate(text.replace("Date de Parution :", "").trim());
                } else if (text.contains("Postuler Avant")) {
                    job.setApplicationDeadline(text.replace("Postuler Avant :", "").trim());
                }
            }
            Elements detailElements = doc.select(".jobsearch-jobdetail-options li");
            for (Element detailElement : detailElements) {
                String text = detailElement.text();

                // Extract address if it contains the map marker
                if (detailElement.selectFirst(".fa.fa-map-marker") != null) {
                    String address = detailElement.text().replace("Google Map", "").trim();
                    job.setCity(address);
                }
            }

            // Extract required skills
            Elements skillElements = doc.select(".jobsearch-jobdetail-tags a");
            if (!skillElements.isEmpty()) {
                List<String> skills = new ArrayList<>();
                for (Element skillElement : skillElements) {
                    skills.add(skillElement.text());
                }
                job.setRecommendedSkills(String.join(", ", skills));
            }

            // Extract additional requirements
            Element requirementsElement = doc.selectFirst("h3:contains(Ce que nous recherchons chez vous :) + ul");
            if (requirementsElement != null) {
                Elements listItems = requirementsElement.select("li");
                List<String> requirements = new ArrayList<>();
                for (Element li : listItems) {
                    requirements.add(li.text());
                }
                job.setRequiredProfile(String.join(", ", requirements));
            }

        } catch (Exception e) {
            System.err.println("Error fetching job details for: " + job.getUrl());
            e.printStackTrace();
        }
    }

    private void saveJobsToJson(List<scraper.Job> jobs, String fileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), jobs);
            System.out.println("Jobs saved to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
