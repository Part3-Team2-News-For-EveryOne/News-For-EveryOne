package com.example.newsforeveryone.newsarticle.batch.writer;

import com.example.newsforeveryone.newsarticle.entity.NewsArticle;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;

@Slf4j
@Component("ArticleBackupWriter")
@RequiredArgsConstructor
public class ArticleBackupWriter implements ItemWriter<NewsArticle>, StepExecutionListener {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    private File tempFile;
    private BufferedWriter writer;

    @Value("${newsforeveryone.storage.s3.bucket}")
    private String bucket;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            tempFile = File.createTempFile("article-backup", ".json");
            writer = Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 백업 파일 생성 실패", e);
        }
    }

    @Override
    public void write(Chunk<? extends NewsArticle> chunk) throws Exception {
        for (NewsArticle article : chunk) {
//            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(article);
            String json = objectMapper.writeValueAsString(article);
            writer.write(json);
            writer.newLine();
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            writer.close();

            String key = String.format("news-backup/%s/articles_%s.json", LocalDate.now(), LocalDate.now());
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(tempFile.toPath()));
            log.info("뉴스 {}건 S3 업로드 성공: {}", stepExecution.getWriteCount(), key);

            return ExitStatus.COMPLETED;

        } catch (Exception e) {
            log.error("S3 업로드 실패", e);
            return ExitStatus.FAILED;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("임시 파일 삭제 실패: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}
