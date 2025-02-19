package org.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class EmploiMaJobScraper extends RekruteJobScraper{

    public List<scraper.Job> scrapeJobs(String url) {
        List<scraper.Job> jobs = new ArrayList<>();
        int page = 0;

        try {
            while ((true) && (page<3)) {
                String paginatedUrl = url + "/recherche-jobs-maroc?page=" + page;
                System.out.println("Fetching URL: " + paginatedUrl);

                Document doc = Jsoup.connect(paginatedUrl).get();
                Elements jobCards = doc.select("div.card.card-job");

                if (jobCards.isEmpty()) {
                    break;
                }

                for (Element card : jobCards) {
                    scraper.Job job = new scraper.Job();
                    job.setSiteName("Emploi.ma");

                    // Extract job details from the list page
                    job.setTitle(card.select("h3 a").text());
                    job.setUrl("https://www.emploi.ma" + card.select("h3 a").attr("href"));
                    job.setCompanyName(card.select("a.card-job-company").text());

                    Elements ulElements = card.select("ul li");
                    for (Element li : ulElements) {
                        String text = li.text();

                        if (text.contains("Niveau d´études requis")) {
                            job.setDegree(li.select("strong").text());
                        } else if (text.contains("Niveau d'expérience")) {
                            job.setExperience(li.select("strong").text());
                        } else if (text.contains("Contrat proposé")) {
                            job.setContractType(li.select("strong").text());
                        } else if (text.contains("Région de")) {
                            job.setRegion(li.select("strong").text());
                        } else if (text.contains("Compétences clés")) {
                            job.setHardSkills(li.select("strong").text());
                        }
                    }

                    job.setPublicationDate(card.select("time").attr("datetime"));

                    // Fetch additional details from the job post page
                    scrapeJobDetails(job);

                    jobs.add(job);
                }

                page++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }

    private void scrapeJobDetails(scraper.Job job) {
        try {
            Document doc = Jsoup.connect(job.getUrl()).get();

            // Extract details from the single job post page
            Element summaryBlock = doc.select("div.page-application-wrapper").first();
            if (summaryBlock != null) {
                for (Element li : summaryBlock.select("li")) {
                    String text = li.text();
                    if (li.hasClass("suitcase")) {
                        job.setSector(li.select("span").text());
                    } else if (li.hasClass("location-dot")) {
                        job.setRegion(li.select("span").text());
                    } else if (li.hasClass("filter-slider")) {
                        job.setRemoteWork(li.select("span").text());
                    } else if (li.hasClass("chart")) {
                        job.setExperience(li.select("span").text());
                    } else if (li.hasClass("graduation-cap")) {
                        job.setDegree(li.select("span").text());
                    } else if (li.hasClass("file-signature")) {
                        job.setContractType(li.select("span").text());
                    } else if (text.contains("Salaire ")) {
                        job.setSalary(li.select("span").text());
                    }
                }
            }

            Element jobDescription = doc.select("li.company-description p").first();
            if (jobDescription != null) {
                job.setJobDescription(jobDescription.text());
            }

            Element profileSection = doc.select("div.job-qualifications ul").first();
            if (profileSection != null) {
                StringBuilder profile = new StringBuilder();
                for (Element li : profileSection.select("li")) {
                    profile.append(li.text()).append("\n");
                }
                job.setRequiredProfile(profile.toString().trim());
            }
            Element companyWebsite = doc.select("a[href^='/recruteur/']").first();
            if (companyWebsite != null) {
                job.setCompanyWebsite("https://www.emploi.ma" + companyWebsite.attr("href"));
            }

            Elements skills = doc.select("ul.skills li");
            if (!skills.isEmpty()) {
                StringBuilder skillSet = new StringBuilder();
                for (Element skill : skills) {
                    skillSet.append(skill.text()).append(", ");
                }
                if (skillSet.length() > 0) {
                    job.setHardSkills(skillSet.substring(0, skillSet.length() - 2));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
