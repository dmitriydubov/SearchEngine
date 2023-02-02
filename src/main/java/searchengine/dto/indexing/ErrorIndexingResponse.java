package searchengine.dto.indexing;

import lombok.Data;

@Data
public class ErrorIndexingResponse extends IndexingResponse {
    private boolean result = false;
    private String error = "Индексация уже запущена";
}
