package com.lunazkoe.newsaggregator.domain.interest.repository;

import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {
    boolean existsByName(String name);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM interests i WHERE similarity(i.name, :name) >= 0.8 AND i.is_deleted = false)", nativeQuery = true)
    // - pg_trgm similarity 함수를 사용하여 유사도 80%이상인 데이터가 있는지 확인하는 Native Query
    // - 주의사항: 이 쿼리를 실행하기 전에 반드시 DB에 CREATE EXTENSION IF NOT EXISTS pg_trgm; 가 실행되어야함
    boolean existsBySimilarName(@Param("name") String name);
}
