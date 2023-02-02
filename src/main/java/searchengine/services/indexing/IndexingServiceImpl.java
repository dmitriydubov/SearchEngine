package searchengine.services.indexing;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesListConfig;
import searchengine.dto.indexing.*;
import searchengine.model.*;
import searchengine.services.utils.RepositoryUtils;
import searchengine.services.utils.TaskBuilder;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    public static volatile boolean isIndexingNow;
    private final SitesListConfig sites;
    private final RepositoryUtils repositoryUtils;

    @Override
    public IndexingResponse getIndexing() {
        IndexingResponse response;
        if (isIndexingNow) {
            response = new ErrorIndexingResponse();
        } else {
            IndexingServiceImpl.isIndexingNow = true;
            repositoryUtils.deleteAll();

            List<Site> siteList = sites.getSites().stream().map(item -> {
                Site site = new Site();
                site.setUrl(item.getUrl());
                site.setName(item.getName());
                return site;
            }).toList();

            new Thread(() -> TaskBuilder.makeIndexingAllSiteTask(siteList, repositoryUtils)).start();

            response = new SuccessIndexingResponse();
            response.setResult(true);
        }
        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse response;
        if (!isIndexingNow) {
            response = new ErrorStopIndexingResponse();
        } else {
            ForkJoinPool.commonPool().shutdownNow();
            isIndexingNow = false;

            repositoryUtils.stopIndexing();

            response = new StopIndexingResponse();
            response.setResult(true);
        }
        return response;
    }

    @Override
    public IndexingResponse getIndexingOneSite(String url) {
        IndexingResponse response;

        String regex = "^((http|https)://)?(w{3}.)?";
        String destSite = url.replaceAll(regex, "");

        Set<String> sitesSet = sites.getSites()
                .stream()
                .map(siteConfig -> siteConfig.getUrl().replaceAll(regex, "")).collect(Collectors.toSet());
        boolean isCorrectPage = sitesSet.contains(destSite);

        if (!isCorrectPage) {
            response = new ErrorIndexingResponseOnePage();
        } else if (isIndexingNow) {
            response = new ErrorIndexingResponse();
        } else {
            isIndexingNow = true;

            Site site = new Site();
            sites.getSites().stream()
                    .filter(siteConfig -> siteConfig.getUrl().replaceAll(regex, "").equals(destSite))
                    .forEach(siteConfig -> {
                        site.setName(siteConfig.getName());
                        site.setUrl(siteConfig.getUrl());
                    });

            new Thread(() -> TaskBuilder.makeIndexingOneSiteTask(site, repositoryUtils)).start();

            response = new SuccessIndexingResponseOnePage();
            response.setResult(true);
        }

        return  response;
    }
}
