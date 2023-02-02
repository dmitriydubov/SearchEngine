package searchengine.services.utils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;

public class ConnectionBuilder {
    public static Optional<Connection.Response> makeConnection(
            String url,
            Site site,
            RepositoryUtils repositoryUtils
    ) throws IOException, InterruptedException {
        Connection.Response response = getJSOPConnection(url, site, repositoryUtils);
        if (response == null) return Optional.empty();
        return Optional.of(response);
    }

    public static List<String> parseUrl(Connection.Response response, String url) throws IOException {
        Document doc = response.parse();
        return doc.select("a[href]")
            .stream()
            .map(element -> element.absUrl("href"))
            .filter(link -> isCorrectLink(link, url))
            .distinct()
            .toList();
    }

    private static Connection.Response getJSOPConnection(
            String url,
            Site site,
            RepositoryUtils repositoryUtils
    ) throws IOException, InterruptedException  {
        Connection.Response response = null;
        try {
            int timeout = (int) Math.round(1 + Math.random() * 5);
            Thread.sleep(timeout);
            response = Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute()
                .bufferUp();
        } catch (HttpStatusException ex) {
            ex.printStackTrace();
            Page page = PageBuilder.makePageWithError(site, url, ex.getStatusCode());
            repositoryUtils.addPageToDB(page);
            repositoryUtils.setIndexingLastError(site, ex.getStatusCode(), ex.getLocalizedMessage());
        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
            Page page = PageBuilder.makePageWithError(site, url, 408);
            repositoryUtils.addPageToDB(page);
            repositoryUtils.setIndexingLastError(site, 408, ex.getLocalizedMessage());
        }
        return response;
    }

    private static boolean isCorrectLink(String link, String url) {
        boolean isFile = link.matches("([http|https|ftp:]+)/{2}([\\D\\d]+)[.]([doc|pdf|rtf|mp4|mp3]+)");
        return link.startsWith(url) && !link.equals(url)
                && !isFile
                && !link.contains("#")
                && !link.contains("?");
    }
}
