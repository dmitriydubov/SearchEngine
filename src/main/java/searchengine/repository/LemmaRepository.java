package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Query("from Lemma where lemma = :value and site_id = :siteId")
    Optional<Lemma> findByLemmaAndSiteId(String value, int siteId);

    void deleteAllBySite(Site site);

    @Query(value = "SELECT COUNT(*) from lemma WHERE site_id = :siteId", nativeQuery = true)
    int lemmaCount(int siteId);

    @Query(value = "SELECT MAX(frequency) from lemma", nativeQuery = true)
    int getLemmaMaxFrequency();

    List<Lemma> findByLemma(String lemma);

    @Query(value = "SELECT * from lemma WHERE lemma = :lemma and site_id = :siteId", nativeQuery = true)
    List<Lemma> getLemmaListByLemmaAndSiteId(String lemma, int siteId);
}
