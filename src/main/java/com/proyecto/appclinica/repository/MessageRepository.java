package com.proyecto.appclinica.repository;

import com.proyecto.appclinica.model.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    List<MessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
