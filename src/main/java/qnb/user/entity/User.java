package qnb.user.entity;

import qnb.question.entity.Question;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`USER`")  // 예약어 우려 줄이기 위해 백틱 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; //사용자의 고유 ID

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false)
    private String name; //사용자의 실명

    private String userNickname; //닉네임은 필수 아님

    private String profileUrl; //필수 아님

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING) //성별은 남,여로만 제한
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String phoneNumber;

    private String readingTaste;

    //question과 역방향 관계 추가 -> 사용자별 질문 목록 조회 시 필요
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

}