package com.lunazkoe.newsaggregator.domain.activity.repository;

import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserActivityRepository extends MongoRepository<UserActivity, UUID> {
}
