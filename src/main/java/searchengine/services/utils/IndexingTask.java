package searchengine.services.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.Connection;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.indexing.IndexingServiceImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
@Getter
public class IndexingTask extends RecursiveAction {
    private String url;
    private Site site;
    private RepositoryUtils repositoryUtils;

    @Override
    protected void compute() {
        if (!IndexingServiceImpl.isIndexingNow) Thread.currentThread().interrupt();

        Set<IndexingTask> taskSet = Collections.synchronizedSet(new HashSet<>());
        Set<String> linkSet = Collections.synchronizedSet(new HashSet<>());

        try {
            Optional<Connection.Response> response = ConnectionBuilder.makeConnection(url, site, repositoryUtils);
            if (response.isEmpty()) return;

            Optional<Page> optionalPage = PageBuilder.makePage(
                    response.get(),
                    new HashMap<>() {{
                        put(site, url);
                    }},
                    repositoryUtils
            );

            optionalPage.ifPresent(page -> {
                if (page.getCode() == 200) {
                    repositoryUtils.addPageToDB(page);
                    Map<Page, Set<Lemma>> indexMap = repositoryUtils.addLemmaToDBAndReturnData(page);
                    repositoryUtils.addIndexToDB(indexMap);
                }
            });

            ConnectionBuilder.parseUrl(response.get(), url).forEach(link -> {
                if (!linkSet.contains(link)) {
                    IndexingTask task = new IndexingTask(link, site, repositoryUtils);
                    taskSet.add(task);
                    linkSet.add(link);
                }
            });

            ForkJoinTask.invokeAll(taskSet);
        } catch (IOException e) {
            e.printStackTrace();
            repositoryUtils.setFailedSite(site);
        } catch (InterruptedException ignored) {}
    }
}
