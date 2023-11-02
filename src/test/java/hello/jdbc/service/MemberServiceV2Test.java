package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화
 */

class MemberServiceV2Test {

    public static final String MEMBER1 = "member1";
    public static final String MEMBER2 = "member2";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV2 memberRepository;
    private MemberServiceV2 memberService;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV2(dataSource);
        memberService = new MemberServiceV2(dataSource,memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER1);
        memberRepository.delete(MEMBER2);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {

        //given
        Member member1 = new Member(MEMBER1, 10000);
        Member member2 = new Member(MEMBER2, 10000);
        memberRepository.save(member1);
        memberRepository.save(member2);
        //when

        memberService.accountTransfer(member1.getMemberId(), member2.getMemberId(), 2000);
        //then
        Member findMember1 = memberRepository.findById(member1.getMemberId());
        Member findMember2 = memberRepository.findById(member2.getMemberId());

        assertThat(findMember1.getMoney()).isEqualTo(8000);
        assertThat(findMember2.getMoney()).isEqualTo(12000);

    }

    @Test
    @DisplayName("예외 발생 이체")
    void accountTransferEx() throws SQLException {

        //given
        Member member1 = new Member(MEMBER1, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(member1);
        memberRepository.save(memberEx);
        //when
        assertThatThrownBy(() -> memberService.accountTransfer(member1.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMember1 = memberRepository.findById(member1.getMemberId());
        Member findMember2 = memberRepository.findById(memberEx.getMemberId());

        assertThat(findMember1.getMoney()).isEqualTo(10000);
        assertThat(findMember2.getMoney()).isEqualTo(10000);

    }
}