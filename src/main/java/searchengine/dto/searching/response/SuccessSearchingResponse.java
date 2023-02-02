package searchengine.dto.searching.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.dto.searching.SearchingData;

import java.util.List;

@Data
@AllArgsConstructor
public class SuccessSearchingResponse extends SearchingResponse {
    private boolean result;
    private int count;
    private List<SearchingData> data;
}
