package org.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class RekruteJobScraper {

    public List<scraper.Job> scrapeJobs(String url) {
        List<scraper.Job> jobs = new ArrayList<>();
        int page = 1;

        try {
            while (true)  {
                // Construct the paginated URL
                String paginatedUrl = url+"/offres.html?p="+page+"&s=1&o=1";
                System.out.println("Fetching URL: " + paginatedUrl);

                Document doc = Jsoup.connect(paginatedUrl).get();
                Elements jobCards = doc.select(".post-id");

                // Break the loop if no jobs are found
                if (jobCards.isEmpty()) {
                    break;
                }

                for (Element card : jobCards) {
                    scraper.Job job = new scraper.Job();
                    job.setSiteName("Rekrute");

                    // Basic job information from the list page
                    job.setTitle(card.select("h2 a.titreJob").text());
                    job.setUrl("https://www.rekrute.com" + card.select("h2 a.titreJob").attr("href"));
                    job.setCompanyName(card.select("img.photo").attr("alt"));

                    Elements dateElements = card.select("em.date span");
                    if (dateElements.size() >= 2) {
                        job.setPublicationDate(dateElements.get(0).text());
                        job.setApplicationDeadline(dateElements.get(1).text());
                    }
                    Element profileSection = card.select("div.info:has(i.fa-info-circle)").first();
                    if (profileSection != null) {
                        Elements listItems = profileSection.select("ul li");

                        for (Element li : listItems) {
                            String text = li.text();

                            // Extract sector
                            if (text.contains("Secteur d'activité")) {
                                job.setSector(text.replace("Secteur d'activité :", "").trim());
                            }

                            // Extract profession
                            if (text.contains("Fonction")) {
                                job.setProfession(text.replace("Fonction :", "").trim());
                            }

                            // Extract experience
                            if (text.contains("Expérience requise")) {
                                String experience = li.select("a").text();
                                job.setExperience(experience.trim());
                            }

                            // Extract degree
                            if (text.contains("Niveau d'étude demandé")) {
                                String degree = li.select("a").text();
                                job.setDegree(degree.trim());
                            }

                            // Extract contract type and remote work
                            if (text.contains("Type de contrat proposé")) {
                                String[] parts = text.replace("Type de contrat proposé :", "").trim().split("- Télétravail :");
                                job.setContractType(parts[0].trim());
                                if (parts.length > 1) {
                                    job.setRemoteWork(parts[1].trim());
                                }
                            }
                        }
                    }

                    // Scrape additional details from the job post page
                    scrapeJobDetails(job);

                    // Add the job to the list
                    jobs.add(job);
                }

                page++; // Move to the next page
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }

    private void scrapeJobDetails(scraper.Job job) {
        try {
            Document doc = Jsoup.connect(job.getUrl()).get();

            // Company information
            Element companyDescriptionElement = doc.select("#recruiterDescription").first();
            if (companyDescriptionElement != null) {
                Elements paragraphs = companyDescriptionElement.select("p");
                StringBuilder companyDescription = new StringBuilder();
                for (Element paragraph : paragraphs) {
                    companyDescription.append(paragraph.text()).append("\n");
                }
                job.setCompanyDescription(companyDescription.toString().trim());
            }
            Element regionElement = doc.select("li[title='Région']").first();
            if (regionElement != null) {
                String regionText = regionElement.text();
                String city = regionText.split(" - ")[0].trim();
                job.setCity(city);
            }

            // Select all spans with the class "tagSkills"
            Elements skillElements = doc.select("span.tagSkills");
            if (!skillElements.isEmpty()) {
                StringBuilder softSkills = new StringBuilder();
                for (Element skill : skillElements) {
                    softSkills.append(skill.text().trim()).append(", ");
                }
                if (softSkills.length() > 0) {
                    job.setSoftSkills(softSkills.substring(0, softSkills.length() - 2));
                }
            }

            // Select the "Profil recherché" section
            Element profileSection = doc.select(".col-md-12.blc:contains(Profil recherché)").first();
            if (profileSection != null) {
                Elements listItems = profileSection.select("ul li");

                StringBuilder requiredProfile = new StringBuilder();
                StringBuilder recommendedSkills = new StringBuilder();

                for (Element li : listItems) {
                    String text = li.text();

                    if (text.contains("formation Bac")) {
                        job.setEducationLevel(text);
                    }

                    if (text.contains("Maitrise des") || text.contains("Programmation") || text.contains("connaissances") ||text.contains("expertise") || text.contains("API")) {
                        recommendedSkills.append(text).append(", ");
                    } else {
                        requiredProfile.append(text).append(", ");
                    }
                }

                if (requiredProfile.length() > 0) {
                    job.setRequiredProfile(requiredProfile.substring(0, requiredProfile.length() - 2));
                }

                if (recommendedSkills.length() > 0) {
                    job.setRecommendedSkills(recommendedSkills.substring(0, recommendedSkills.length() - 2));
                }
            }

            Element companyAddress = doc.select("#address").first();
            if (companyAddress != null) {
                job.setCompanyAddress(companyAddress.text());
            }

            Element jobDescription = doc.select(".col-md-12.blc:contains(Poste) p").first();
            if (jobDescription != null) {
                job.setJobDescription(jobDescription.text());
            }

            Element sector = doc.select(".h2italic").first();
            if (sector != null) {
                job.setSector(sector.text());
            }

            Elements featureInfo = doc.select(".featureInfo li");
            for (Element item : featureInfo) {
                if (item.text().contains("Expérience")) {
                    job.setExperience(item.text().replace("Expérience requise", "").trim());
                }else if (item.attr("title").equals("Région")) {
                    // Remove the <i> element's text and extract the remaining text
                    String textContent = item.text();
                    String[] location = textContent.split("-");

                    // Extract and set the city and region
                    job.setCity(location[0].trim());
                    job.setRegion(location.length > 1 ? location[1].trim() : null);
                }
                else if (item.text().contains("Niveau d'étude")) {
                    job.setEducationLevel(item.text().replace("Niveau d'étude et formation", "").trim());
                }
            }

            Elements contractInfo = doc.select(".tagContrat");
            for (Element contract : contractInfo) {
                if (contract.text().contains("CDI") || contract.text().contains("CDD")) {
                    job.setContractType(contract.text());
                } else if (contract.text().contains("Télétravail")) {
                    job.setRemoteWork(contract.text().replace("Télétravail :", "").trim());
                }
            }

            Elements personalityTraits = doc.select(".tagSkills");
            if (!personalityTraits.isEmpty()) {
                List<String> traits = new ArrayList<>();
                for (Element trait : personalityTraits) {
                    traits.add(trait.text());
                }
                job.setPersonalityTraits(String.join(", ", traits));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}