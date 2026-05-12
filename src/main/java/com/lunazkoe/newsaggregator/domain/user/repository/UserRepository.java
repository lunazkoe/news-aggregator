package com.lunazkoe.newsaggregator.domain.user.repository;

import com.lunazkoe.newsaggregator.domain.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
