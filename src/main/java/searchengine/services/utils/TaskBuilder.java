package searchengine.services.utils;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.indexing.IndexingServiceImpl;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class TaskBuilder {
    public static void makeIndexingOneSiteTask(Site site, RepositoryUtils repositoryUtils) {
        repositoryUtils.oneSiteIndexingPrepare(site);
        repositoryUtils.addNewSiteToDB(site);
        IndexingTask task = makeTask(site, repositoryUtils);
        new ForkJoinPool().invoke(task);
        setSiteIndexed(site, repositoryUtils);
        IndexingServiceImpl.isIndexingNow = false;
    }

    public static synchronized void makeIndexingAllSiteTask(List<Site> siteList, RepositoryUtils repositoryUtils) {
        siteList.forEach(repositoryUtils::addNewSiteToDB);
        siteList.forEach(site -> {
            IndexingTask task = makeTask(site, repositoryUtils);
            new ForkJoinPool().invoke(task);
            setSiteIndexed(site, repositoryUtils);

            List<Site> notIndexedSiteList = repositoryUtils.getSiteRepository()
                .findAll()
                .stream()
                .filter(siteDB -> siteDB.getStatus().equals(Status.INDEXING))
                .toList();

            if (notIndexedSiteList.isEmpty()) IndexingServiceImpl.isIndexingNow = false;
        });
    }

    private static synchronized IndexingTask makeTask(Site site, RepositoryUtils repositoryUtils) {
        return new IndexingTask(site.getUrl(), site, repositoryUtils);
    }

    private static synchronized void setSiteIndexed(Site site, RepositoryUtils repositoryUtils) {
        Optional<Site> optionalSite = repositoryUtils.getSite(site.getUrl());
        optionalSite.ifPresent(existSite -> {
            if (!existSite.getStatus().equals(Status.FAILED)) repositoryUtils.setIndexedSite(existSite);
        });
    }
}
