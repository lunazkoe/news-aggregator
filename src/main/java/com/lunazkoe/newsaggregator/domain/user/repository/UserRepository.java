package com.lunazkoe.newsaggregator.domain.user.repository;

import com.lunazkoe.newsaggregator.domain.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Transactional
    // - TODO: 일단 서비스 레이어에 나중에 붙으면 문제가 없는데 이거 단일로 사용하게 된다면 문제가 발생할 수 있을 것으로 예상됨
    @Modifying(clearAutomatically = true)
    // - JPA에서 @Query는 기본적으로 select 쿼리로 인식
    // - INSERT, UPDATE, DELETE와 같은 DML 쿼리를 실행할 때는 반드시 붙여줘야함
    @Query(value = "delete from users where id = :id", nativeQuery = true)
    // - 벌크 연산: 대량의 연산은 여기서는 아님
    // - 단, 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리를 날리는 행위는 맞음
    // - clearAutomatically = true: 쿼리 실행 직후 영속성 컨텍스트를 비워주는 작업
    void hardDeleteById(@Param("id") UUID id);

    // 스케쥴러용 (일단 임시 구현 - 아직 구현 X)
    // - 삭제 대상인 것만 추출
    // - 이렇게 id를 뽑아오는 것 이유는 (delete from users where .. 하지 않는)
    // - 나중에 무언가를 삭제해야되는데, delete로 하면 어떤 id가 삭제되었는지 알 수 없음
    // - 삭제될 id를 가지고 나중에 무언가를 할 수 있음
    @Query(value = "select id from users where is_deleted = true and deleted_at <= :time", nativeQuery = true)
    List<UUID> findDeletedUserIdsBefore(@Param("time")LocalDateTime time);
}

// 추가사항: 나중에 연관관계 삭제를 위해서 어떻게 해야할까?
// - hardDeleteById는 @Modifying을 서서 JPA의 연관관계 삭제 설정이 먹히지 않음
// - DB에 바로 쿼리를 쏘는 거기 때문에 DB 레벨에 on delete cascde 설정을 걸어주는 것이 좋을듯
// - 또는 직접 repository로 연관관게 삭제를 진행해주는 것도 있음
