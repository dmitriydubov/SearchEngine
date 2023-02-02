package searchengine.services.utils;
import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@AllArgsConstructor
public class PageBuilder {
    public synchronized static Optional<Page> makePage(
            Connection.Response response,
            HashMap<Site, String> siteMap,
            RepositoryUtils repositoryUtils
    ) throws IOException {
        Optional<Site> site = siteMap.keySet().stream().findFirst();
        Optional<String> url = siteMap.values().stream().findFirst();
        if (site.isEmpty()) return Optional.empty();
        String path = makePath(site.get(), url.get());
        if (checkForAlreadyExistPath(path, repositoryUtils, site.get().getUrl())) return Optional.empty();
        Page page = new Page();
        page.setPath(path);
        page.setSite(site.get());
        page.setCode(response.statusCode());
        page.setContext(getContext(response));
        return Optional.of(page);
    }

    public static synchronized Page makePageWithError(Site site, String url, int statusCode) {
        Page page = new Page();
        page.setCode(statusCode);
        page.setSite(site);
        page.setPath(PageBuilder.makePath(site, url));
        return page;
    }

    private synchronized static String makePath(Site site, String url) {
        return url.equals(site.getUrl()) ?
                url.replaceAll(site.getUrl(), "/") :
                url.replaceAll(site.getUrl(), "");
    }

    private synchronized static boolean checkForAlreadyExistPath(String path, RepositoryUtils repositoryUtils, String url) {
        SiteRepository siteRepository = repositoryUtils.getSiteRepository();
        PageRepository pageRepository = repositoryUtils.getPageRepository();
        Optional<Site> site = siteRepository.findByUrl(url);
        if (site.isEmpty()) return false;
        Optional<Page> optionalPage = pageRepository.findByPathAndSiteId(path, site.get().getId());
        return optionalPage.isPresent();
    }

    private synchronized static String getContext(Connection.Response response) throws IOException {
        Document doc = response.parse();
        return doc.html();
    }
}
