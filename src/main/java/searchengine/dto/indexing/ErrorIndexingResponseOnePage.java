package searchengine.dto.indexing;

import lombok.Data;

@Data
public class ErrorIndexingResponseOnePage extends IndexingResponse {
    private boolean result = false;
    private String error = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";
}
