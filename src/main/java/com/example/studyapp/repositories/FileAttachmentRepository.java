package com.example.studyapp.repositories;

import com.example.studyapp.entities.FileAttachment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE file_attachment SET delete_pending = true WHERE topic_id IN (:topicIds)", nativeQuery = true)
    void markAsPendingDeletionByTopicIds(@Param("topicIds") List<Long> topicIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE file_attachment SET topic_id = NULL WHERE topic_id IN (:topicIds)", nativeQuery = true)
    void unlinkByTopicIds(@Param("topicIds") List<Long> topicIds);

    @Modifying
    @Query(value = "UPDATE file_attachment SET delete_pending = true WHERE id = :fileId", nativeQuery = true)
    void markAsPendingDeletionById(@Param("fileId") Long fileId);

    @Modifying
    @Query(value = "UPDATE file_attachment SET topic_id = NULL WHERE id = :fileId", nativeQuery = true)
    void unlinkById(@Param("fileId") Long fileId);

    List<FileAttachment> findBydeletePendingTrue();

}
