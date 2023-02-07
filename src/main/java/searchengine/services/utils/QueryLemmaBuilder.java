package searchengine.services.utils;

import searchengine.model.Lemma;
import searchengine.model.Site;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class QueryLemmaBuilder {
    private static int siteId = -1;
    public static List<Lemma> makeLemmaSortedList(
            String query,
            String url,
            RepositoryUtils repositoryUtils
    ) throws IOException {
        final int FREQUENCY_LIMIT = (int) Math.round(repositoryUtils.getLemmaRepository().getLemmaMaxFrequency() * 0.8);
        Set<String> queryLemmaSet = LemmaBuilder.makeLemmasFromSearchQuery(query);
        Set<Lemma> lemmaSet;

        if (url == null) {
            lemmaSet = makeLemmaSetAllSites(queryLemmaSet, repositoryUtils, FREQUENCY_LIMIT);
        } else {
            Optional<Site> optionalSite = repositoryUtils.getSiteRepository().findByUrl(url);
            optionalSite.ifPresent(site -> siteId = site.getId());
            lemmaSet = makeLemmaSetOneSite(queryLemmaSet, repositoryUtils, FREQUENCY_LIMIT);
        }

        return lemmaSet.stream().sorted(Comparator.comparing(Lemma::getFrequency)).toList();
    }

    private static Set<Lemma> makeLemmaSetAllSites(
            Set<String> queryLemmaSet,
            RepositoryUtils repositoryUtils,
            int FREQUENCY_LIMIT
    ) {
        Set<Lemma> lemmaSet = new HashSet<>();
        queryLemmaSet.forEach(lemma -> lemmaSet.addAll(
            new HashSet<>(
                repositoryUtils.getLemmaRepository()
                    .findByLemma(lemma)
                    .stream()
                    .filter(lemmaDB -> lemmaDB.getFrequency() <= FREQUENCY_LIMIT).collect(Collectors.toSet()))
            )
        );

        return lemmaSet;
    }

    private static Set<Lemma> makeLemmaSetOneSite(
            Set<String> queryLemmaSet,
            RepositoryUtils repositoryUtils,
            int FREQUENCY_LIMIT
    ) {
        Set<Lemma> lemmaSet = new HashSet<>();
        queryLemmaSet.forEach(lemma -> lemmaSet.addAll(
            repositoryUtils.getLemmaRepository().getLemmaListByLemmaAndSiteId(lemma, siteId)
                .stream()
                .filter(lemmaDB -> lemmaDB.getFrequency() <= FREQUENCY_LIMIT)
                .collect(Collectors.toSet()))
        );
        return lemmaSet;
    }
}
