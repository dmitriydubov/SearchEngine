package searchengine.dto.searching.response;

import lombok.Data;

@Data
public class EmptySearchingResponse extends SearchingResponse {
    private final boolean result = false;
    private final String error = "Задан пустой поисковый запрос";
}
