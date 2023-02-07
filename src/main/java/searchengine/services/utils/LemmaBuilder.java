package searchengine.services.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LemmaBuilder {
    public static HashMap<String, Integer> makeLemmas(Page page) {
        HashMap<String, Integer> lemmaMap = new HashMap<>();
        String pageText;
        try {
            LemmaFinder lemmaFinder = new LemmaFinder();
            pageText = lemmaFinder.cleanHTMLTagsFromText(page.getContext());
            lemmaMap = lemmaFinder.makeSequentialWordNumber(pageText);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return lemmaMap;
    }

    public static Set<String> makeLemmasFromSearchQuery(String query) throws IOException {
        LemmaFinder lemmaFinder = new LemmaFinder();
        return lemmaFinder.makeSequentialWordNumberFromQuery(query);
    }

    private static class LemmaFinder {
        LuceneMorphology russianLuceneMorphology;
        LuceneMorphology englishLuceneMorphology;

        public synchronized HashMap<String, Integer> makeSequentialWordNumber(String text) throws IOException {
            russianLuceneMorphology = new RussianLuceneMorphology();
            List<String> wordList = Arrays.stream(
                    text.replaceAll("[^а-яА-ЯёЁ\\s]", " ")
                        .replaceAll("\\s{2,}", " ")
                        .trim()
                        .toLowerCase()
                        .split(" ")
                ).filter(word -> isCorrectWord(russianLuceneMorphology.getMorphInfo(word).toString()))
                .map(russianLuceneMorphology::getNormalForms)
                .map(list -> list.get(0))
                .map(word -> word.replaceAll("ё", "е"))
                .filter(word -> word.length() > 2)
                .toList();

            Map<String, Long> wordMap = wordList.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            return new HashMap<>(
                wordMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> Integer.parseInt(String.valueOf(entry.getValue())))
                    )
            );
        }

        private Set<String> makeSequentialWordNumberFromQuery(String query) throws IOException {
            russianLuceneMorphology = new RussianLuceneMorphology();
            List<String> wordList = Arrays.stream(
                query.replaceAll("[^а-яА-ЯёЁ\\s]", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim()
                    .toLowerCase()
                    .split(" ")
            ).filter(word -> isCorrectWord(russianLuceneMorphology.getMorphInfo(word).toString())).toList();

            Set<String> lemmaQuerySet = new HashSet<>();
            wordList.forEach(word -> {
                List<String> queryLemmaList = russianLuceneMorphology.getNormalForms(word);
                lemmaQuerySet.addAll(queryLemmaList);
            });

            return new HashSet<>(lemmaQuerySet);
        }

        public synchronized String cleanHTMLTagsFromText(String context) {
            Document document = Jsoup.parse(context);
            return document.body().text();
        }

        private synchronized boolean isCorrectWord(String word) {
            return !word.contains("СОЮЗ") && !word.contains("ПРЕДЛ") && !word.contains("МЕЖД");
        }
    }
}
