package indexer.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doc_seq_gen")
    @SequenceGenerator(name = "doc_seq_gen", sequenceName = "documents_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "file_path", unique = true, nullable = false)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    public Document(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public Document() {}

    public Long getId() {
        return id;
    }
}