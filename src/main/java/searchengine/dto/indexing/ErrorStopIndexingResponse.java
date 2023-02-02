package searchengine.dto.indexing;

import lombok.Data;

@Data
public class ErrorStopIndexingResponse extends IndexingResponse {
    private boolean result = false;
    private String error = "Индексация не запущена";
}
