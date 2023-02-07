package searchengine.services.utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SearchIndexRepository;
import searchengine.repository.SiteRepository;

import java.util.*;

@Service
@AllArgsConstructor
@Getter
public class RepositoryUtils {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SearchIndexRepository searchIndexRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteAll() {
        siteRepository.deleteAll();
        pageRepository.deleteAll();
        lemmaRepository.deleteAll();
        searchIndexRepository.deleteAll();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void stopIndexing() {
        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                site.setStatus(Status.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                site.setStatusTime(new Date());
                siteRepository.save(site);
            }
        });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void oneSiteIndexingPrepare(Site site) {
        Optional<Site> optionalSite = siteRepository.findByUrl(site.getUrl());
        optionalSite.ifPresent(alreadyExistSite -> {
            List<Page> alreadyExistPageList = pageRepository.findAllBySite(alreadyExistSite);

            siteRepository.deleteById(alreadyExistSite.getId());
            pageRepository.deleteAllBySite(alreadyExistSite);
            lemmaRepository.deleteAllBySite(alreadyExistSite);
            deleteSearchIndex(alreadyExistPageList);
        });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addNewSiteToDB(Site site) {
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        site.setName(site.getName());
        site.setUrl(site.getUrl());
        siteRepository.saveAndFlush(site);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void setIndexingLastError(Site site, int statusCode, String message) {
        site.setLastError(
                "Ошибка подключения. Код ошибки - " + statusCode + " Причина: " + message
        );
        site.setStatusTime(new Date());
        siteRepository.saveAndFlush(site);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addPageToDB(Page page) {
        pageRepository.saveAndFlush(page);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteSearchIndex(List<Page> pageList) {
        pageList.forEach(page -> searchIndexRepository.deleteSelectedContains(page.getId()));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized Map<Page, Set<Lemma>> addLemmaToDBAndReturnData(Page page) {
        Map<String, Integer> lemmas = Collections.synchronizedMap(new HashMap<>(LemmaBuilder.makeLemmas(page)));
        Set<Lemma> lemmaSet = Collections.synchronizedSet(new HashSet<>());

        lemmas.forEach((word, count) -> {
            Optional<Lemma> optionalLemma = lemmaRepository.findByLemmaAndSiteId(word, page.getSite().getId());
            if(optionalLemma.isEmpty()) {
                Lemma lemma = new Lemma();
                lemma.setFrequency(1);
                lemma.setLemma(word);
                lemma.setSite(page.getSite());
                lemma.setRank(count);
                lemmaSet.add(lemma);
            } else {
                Lemma lemmaDB = optionalLemma.get();
                int existLemmaFrequency = lemmaDB.getFrequency();
                lemmaDB.setFrequency(++existLemmaFrequency);
                lemmaDB.setRank(count);
                lemmaSet.add(lemmaDB);
            }
        });

        lemmaRepository.saveAllAndFlush(lemmaSet);
        return new HashMap<>(){{put(page, lemmaSet);}};
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void setIndexedSite(Site site) {
        site.setStatus(Status.INDEXED);
        site.setStatusTime(new Date());
        siteRepository.saveAndFlush(site);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void setFailedSite(Site site) {
        site.setStatus(Status.FAILED);
        site.setStatusTime(new Date());
        siteRepository.saveAndFlush(site);
    }

    public Optional<Site> getSite(String url) {
        return siteRepository.findByUrl(url);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addIndexToDB(Map<Page, Set<Lemma>> lemmaMap) {
        Set<SearchIndex> indexSet = Collections.synchronizedSet(new HashSet<>());

        lemmaMap.forEach((page, lemmaSet) -> lemmaSet.forEach(lemma -> {
            SearchIndex index = new SearchIndex();
            index.setLemmaId(lemma.getId());
            index.setPageId(page.getId());
            index.setLemmaRank(lemma.getRank());
            indexSet.add(index);
        }));
        searchIndexRepository.saveAllAndFlush(indexSet);
    }
}
