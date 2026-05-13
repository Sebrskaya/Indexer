package indexer.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "words")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "word_seq_gen")
    @SequenceGenerator(name = "word_seq_gen", sequenceName = "words_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "word_text", unique = true, nullable = false)
    private String text;

    public Word() {}

    public Word(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }
}