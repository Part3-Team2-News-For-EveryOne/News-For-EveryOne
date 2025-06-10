package com.example.newsforeveryone.newsarticle.entity;

import com.example.newsforeveryone.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "article_backup_history")
@Entity
public class ArticleBackupHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_backup_history_seq_gen")
    @SequenceGenerator(name = "article_backup_history_seq_gen", sequenceName = "article_backup_history_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "backup_date", nullable = false)
    private Instant backupDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

}
