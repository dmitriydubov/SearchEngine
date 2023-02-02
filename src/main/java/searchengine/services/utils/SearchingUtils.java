package searchengine.services.utils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import searchengine.dto.searching.SearchingData;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.repository.SearchIndexRepository;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchingUtils {
    public static List<SearchingData> makeDetailedSearchingItem(
            List<Page> uniquePageList,
            List<Lemma> lemmaSortedList,
            RepositoryUtils repositoryUtils,
            String query
    ) {
        List<SearchingData> searchingItemList = new ArrayList<>();
        float maxRel = SearchingUtils.getMaxRelevance(uniquePageList, lemmaSortedList, repositoryUtils.getSearchIndexRepository());

        uniquePageList.forEach(page -> {
            List<Lemma> lemmaList = new ArrayList<>();
            SearchingData searchingItem = new SearchingData();
            AtomicReference<Float> absRelevance = new AtomicReference<>(0.0f);

            lemmaSortedList.forEach(lemma -> {
                Optional<SearchIndex> index = repositoryUtils.getSearchIndexRepository()
                        .findLemmaId(page.getId(), lemma.getId());
                index.ifPresent(searchIndex -> {
                    absRelevance.set(absRelevance.get() + searchIndex.getLemmaRank());
                    lemmaList.add(lemma);
                });
            });

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            float relRelevance = Float.parseFloat(
                    decimalFormat.format(absRelevance.get() / maxRel).replaceAll(",", ".")
            );

            searchingItem.setRelevance(relRelevance);
            searchingItem.setUri(page.getPath());
            searchingItem.setTitle(Jsoup.parse(page.getContext()).title());

            String snippet = makeSnippet(page, query);
            if (!snippet.equals("")) {
                searchingItem.setSnippet(snippet);
                searchingItem.setSite(lemmaList.get(0).getSite().getUrl());
                searchingItem.setSiteName(lemmaList.get(0).getSite().getName());
                searchingItemList.add(searchingItem);
            }
        });
        return searchingItemList;
    }

    private static float getMaxRelevance(
            List<Page> uniquePageList,
            List<Lemma> lemmaSortedList,
            SearchIndexRepository searchIndexRepository
    ) {
        List<Float> absRelevanceList = new ArrayList<>();
        uniquePageList.forEach(page -> {
            AtomicReference<Float> absRelevance = new AtomicReference<>(0.0f);
            lemmaSortedList.forEach(lemma -> {
                Optional<SearchIndex> index = searchIndexRepository.findLemmaId(page.getId(), lemma.getId());
                index.ifPresent(searchIndex -> absRelevance.set(absRelevance.get() + searchIndex.getLemmaRank()));
            });
            absRelevanceList.add(absRelevance.get());
        });

        OptionalDouble optionalDouble = absRelevanceList.stream().mapToDouble(Float::floatValue).max();
        float maxRel = 0.0f;
        if ((optionalDouble.isPresent())) {
            maxRel = (float) optionalDouble.getAsDouble();
        }

        return maxRel;
    }

    private static String makeSnippet(Page page, String query) {
        String[] queryStrArr = query.trim().split(" ");
        StringBuilder sb = new StringBuilder();
        Arrays.stream(queryStrArr).forEach(word -> sb.append(word).append(" "));
        String text = sb.toString().trim();
        String regex = "(" + text + ")";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        Elements elements = Jsoup.parse(
            page.getContext()
        ).body().getElementsMatchingOwnText(pattern);

        StringBuilder snippet = new StringBuilder();
        Matcher matcher = pattern.matcher(elements.text());
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String targetQuery = elements.text().substring(start, end);
            String textAfter = elements.text().substring(end);
            String visibleSnippetText;

            if (textAfter.length() > 200) {
                visibleSnippetText = textAfter.substring(0, 200) + " . . .";
            } else {
                visibleSnippetText = textAfter;
            }

            snippet.append(targetQuery.replaceAll(targetQuery, "<b>" + targetQuery + "</b>"))
                    .append(visibleSnippetText);
        }
        return snippet.toString();
    }
}
