package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.searching.response.SearchingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.searching.SearchingService;
import searchengine.services.statistics.StatisticsService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchingService searchingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> makeIndexing() throws InterruptedException {
        return ResponseEntity.ok(indexingService.getIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() throws InterruptedException, ExecutionException {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> makeIndexingOnePage(
            @RequestParam(name = "url", defaultValue = "") String url
    ) throws InterruptedException {
        return ResponseEntity.ok(indexingService.getIndexingOneSite(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchingResponse> search(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "site", required = false) String url,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit
    ) throws InterruptedException, IOException {
        return ResponseEntity.ok(searchingService.searching(query, url, offset, limit));
    }
}
