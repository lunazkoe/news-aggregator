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
//@SQLDelete(sql = "update users set is_deleted = true, deleted_at = NOW() where id = ?")
// - 삭제 쿼리 발생 시 기본적으로 논리 삭제로 가로채기
// - delete 메서드를 만들어서 시간이 안 맞을 수 있는 문제를 일단 해결
@SQLRestriction("is_deleted = false")
// - 조회 쿼리 발생 시 기본적으로 삭제되지 않은 데이터만 조회
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    // - DB 시작 시: CREATE UNIQUE INDEX uk_user_email ON users (email) WHERE is_deleted = false;
    // - 논리적으로 삭제된 경우 부분 인덱스 조건에서 제외됨
    // - 논리적으로 삭제된 경우에는 회원가입을 동일한 이메일로 시도 시 => 완전히 새로운 계정으로 취급
    // - 근데 이건 나~중에 복구 처리를 해야하는게 맞을 것 같음
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    // 닉네임 수정용
    public void updateNickName(String nickname) {
        this.nickname = nickname;
    }

    // 애플리케시션 로직 기반의 명시적인 논리 삭제 메서드를 추가
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now(); // DB, Application 서버 시간 일치
    }
}

// 여기서 고민 포인트
// - now()는 스프링 시간이 아닌 database 시간. 문제가 발생할 여지가 있음
//      - 해결방안은 뭐가 있을까?
//      - main
//      ```
//        @PostConstruct
//        public void init() {
//            TiemeZone.setDefault(TimeZone.getItmeZone(UTD))
//        }
//      - DB도 UTC로 맞추기
//      ```
//      - 애플리케이션 단에서 논리 삭제 처리
//      - @SQLDelete를 제거하고 user.delete()에서 isDeleted = true, deletedAt = LocalDateTime.now()
//      - 이후 더티 체크로 업데이트하는 방법도 있음
// - email에 있는 unique
//      - 논리적 삭제가 되면 조회는 안되서 같은 이메일로 무언가를 하려면 문제가 안되는 것처럼 보이지만
//      - 같은 이메일로 가입을 하려는 경우에 데이터베이스 내부 자체에서 unique 제약사항에 걸려 오류가 나게됨
//      - 일단 해결방안은
//              - 논리적 삭제시 이메일을 강제로 이상하게 변경
//              - 같은 이메일로 들어올 시 삭제된 데이터를 찾아 다시 활성화
