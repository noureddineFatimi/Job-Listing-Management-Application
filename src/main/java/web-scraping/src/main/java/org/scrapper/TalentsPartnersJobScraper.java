package org.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class TalentsPartnersJobScraper extends RekruteJobScraper {

    @Override
    public List<scraper.Job> scrapeJobs(String url) {
        List<scraper.Job> jobs = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements jobCards = doc.select(".job-card-selector"); // Update with actual selector

            for (Element card : jobCards) {
                scraper.Job job = new scraper.Job();
                job.setTitle(card.select(".job-title-selector").text()); // Update selectors
                job.setUrl(card.select(".job-link-selector").attr("href")); // Update selectors
                job.setCompanyName(card.select(".company-name-selector").text()); // Update selectors
                // Additional details...
                jobs.add(job);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobs;
    }
}
