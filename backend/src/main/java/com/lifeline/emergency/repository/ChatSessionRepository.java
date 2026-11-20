package com.lifeline.emergency.repository;

import com.lifeline.emergency.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findTopByUserIdOrderByUpdatedAtDesc(Long userId);
}

