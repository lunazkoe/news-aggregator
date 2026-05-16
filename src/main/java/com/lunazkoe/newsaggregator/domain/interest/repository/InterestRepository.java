package com.lunazkoe.newsaggregator.domain.interest.repository;

import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    boolean existsByName(String name);
}
