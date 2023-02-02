package searchengine.services.searching;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.searching.*;
import searchengine.dto.searching.response.EmptySearchingResponse;
import searchengine.dto.searching.response.SearchingResponse;
import searchengine.dto.searching.response.SuccessSearchingResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.services.utils.QueryLemmaBuilder;
import searchengine.services.utils.RepositoryUtils;
import searchengine.services.utils.SearchingUtils;
import java.io.IOException;
import java.util.*;

@Service
@AllArgsConstructor
public class SearchingServiceImpl implements SearchingService{
    private final RepositoryUtils repositoryUtils;

    @Override
    public SearchingResponse searching(
            String query,
            String url,
            int offset,
            int limit
    ) {
        SearchingResponse response;
        List<SearchingData> searchingItemList;

        if (!isValidQuery(query)) {
            response = new EmptySearchingResponse();
            return response;
        }

        try {
            List<Lemma> lemmaSortedList = new ArrayList<>(
                    QueryLemmaBuilder.makeLemmaSortedList(query, url, repositoryUtils)
            );
            List<Page> pageList = new ArrayList<>();
            lemmaSortedList.forEach(lemma -> updatePageList(pageList, lemma));
            List<Page> uniquePageList = pageList.stream()
                    .filter(page -> Collections.frequency(pageList, page) > 1)
                    .distinct()
                    .toList();

            searchingItemList = SearchingUtils.makeDetailedSearchingItem(
                    uniquePageList,
                    lemmaSortedList,
                    repositoryUtils,
                    query
            );

            List<SearchingData> sortedSearchingItemList = searchingItemList.stream()
                    .sorted(Comparator.comparingDouble(SearchingData::getRelevance).reversed())
                    .limit(limit)
                    .toList();

            if (offset > 0 && sortedSearchingItemList.size() > offset) {
                sortedSearchingItemList = sortedSearchingItemList.stream().skip(offset).toList();
            }

            int resultCount = searchingItemList.size();

            response = new SuccessSearchingResponse(true, resultCount, sortedSearchingItemList);
        } catch (IOException ex) {
            ex.printStackTrace();
            response = new EmptySearchingResponse();
        }

        return response;
    }

    private void updatePageList(List<Page> pageList, Lemma lemma) {
        pageList.addAll(
            repositoryUtils.getSearchIndexRepository().findPageIdByLemma(lemma.getId())
                .stream()
                .map(pageId -> repositoryUtils.getPageRepository().findById(pageId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList()
        );
    }

    private boolean isValidQuery(String query) {
        if (query.equals("")) {
            return false;
        }

        return repositoryUtils.getLemmaRepository().findAll().size() != 0 &&
                repositoryUtils.getSiteRepository().findAll().size() != 0;
    }
}
