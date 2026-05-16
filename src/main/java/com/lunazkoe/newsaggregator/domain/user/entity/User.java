package com.lunazkoe.newsaggregator.domain.user.entity;

import com.lunazkoe.newsaggregator.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
// - 자동으로 where 절이 추가됨
// - 따라서 논리 삭제된 대상은 가져올 수 없음
// TODO: 회원가입 / 로그인 시에만 email로 검색 => 인덱스를 걸지 말지 고민중
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    // TODO: 논리 삭제 기능이 있는 객체들의 공통 속성으로 묶을 수 있을 것 같음
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    // - 클래스 레벨이 아닌 생성자 레벨에 @Builder를 사용하면 default 값을 자동으로 반영함
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public void updateNickName(String nickname) {
        this.nickname = nickname;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
