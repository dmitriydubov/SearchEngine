package searchengine.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    void deleteAllBySite(Site site);

    List<Page> findAllBySite(Site site);

    @Query("from Page where path = ?1 and site_id = ?2")
    Optional<Page> findByPathAndSiteId(String path, int siteId);

    @Query(value = "SELECT COUNT(*) from page WHERE site_id = :siteId", nativeQuery = true)
    int pageCount(int siteId);
}
