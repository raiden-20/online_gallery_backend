package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.EventSubjectEntity;

import java.util.List;
import java.util.Optional;

public interface EventSubjectRepository extends JpaRepository<EventSubjectEntity, Integer> {

    List<EventSubjectEntity> findAllByEventId(Integer eventId);

    Boolean existsBySubjectId(Integer subjectId);

    Optional<EventSubjectEntity> findBySubjectId(Integer subjectId);

    @Transactional
    void deleteAllBySubjectId(Integer subjectId);
}
