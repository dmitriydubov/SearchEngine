package searchengine.services.searching;
import searchengine.dto.searching.response.SearchingResponse;

import java.io.IOException;

public interface SearchingService {
    SearchingResponse searching(
        String query,
        String url,
        int offset,
        int limit
    ) throws InterruptedException, IOException;
}
