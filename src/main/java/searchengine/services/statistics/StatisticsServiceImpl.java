package searchengine.services.statistics;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteConfig;
import searchengine.config.SitesListConfig;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;

@Service
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;

    private final SitesListConfig sites;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteRepository.findAll().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed;

        if (siteRepository.findAll().size() == 0) {
            detailed = new ArrayList<>(makeInitialPage(total));
        } else {
            detailed = new ArrayList<>(updatePage(total));
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private List<DetailedStatisticsItem> makeInitialPage(TotalStatistics total) {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteConfig> siteList = sites.getSites();
        siteList.forEach(site -> {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setPages(0);
            item.setLemmas(0);
            item.setStatus("INDEXING");
            item.setError("");
            item.setStatusTime(new Date().getTime());
            total.setPages(total.getPages() + item.getPages());
            total.setLemmas(total.getLemmas() + item.getLemmas());
            detailed.add(item);
        });

        return detailed;
    }

    @Transactional
    private List<DetailedStatisticsItem> updatePage(TotalStatistics total) {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        siteRepository.findAll().forEach(site -> {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = pageRepository.pageCount(site.getId());
            int lemmas = lemmaRepository.lemmaCount(site.getId());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(site.getStatus().toString());
            if (site.getLastError() != null) {
                item.setError(site.getLastError());
            } else {
                item.setError("");
            }
            item.setStatusTime(site.getStatusTime().getTime());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        });
        return detailed;
    }
}
