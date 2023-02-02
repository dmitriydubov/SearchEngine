package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SearchIndex;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Integer> {
    @Transactional
    @Modifying
    @Query(value = "DELETE from search_index WHERE page_id = :pageId", nativeQuery = true)
    void deleteSelectedContains(int pageId);

    @Query(value = "SELECT page_id from search_index WHERE lemma_id = :id", nativeQuery = true)
    List<Integer> findPageIdByLemma(int id);

    @Query("from SearchIndex where page_id = ?1 and lemma_id = ?2")
    Optional<SearchIndex> findLemmaId(int pageId, int lemmaId);
}
