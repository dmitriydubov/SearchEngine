package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Page implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private Site site;

    @Column(columnDefinition = "TEXT", unique = true)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String context;

    @Transient
    private HashMap<String, Integer> lemmas;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
