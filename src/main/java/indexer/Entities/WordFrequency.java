package indexer.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "word_frequencies")
public class WordFrequency {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "word_freq_seq")
    @SequenceGenerator(name = "word_freq_seq", sequenceName = "word_frequencies_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "frequency", nullable = false)
    private Integer count;

    public WordFrequency() {}

    public WordFrequency(Document document, Word word, Integer count) {
        this.document = document;
        this.word = word;
        this.count = count;
    }

    public Long getId() {
        return id;
    }
}