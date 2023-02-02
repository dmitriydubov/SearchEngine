package searchengine.services.indexing;

import searchengine.dto.indexing.IndexingResponse;

import java.util.concurrent.ExecutionException;

public interface IndexingService {
    IndexingResponse getIndexing() throws InterruptedException;
    IndexingResponse stopIndexing() throws InterruptedException, ExecutionException;
    IndexingResponse getIndexingOneSite(String page) throws InterruptedException;
}
