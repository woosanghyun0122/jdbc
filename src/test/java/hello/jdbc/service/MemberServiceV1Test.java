package hello.jdbc.service;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 기본 동작, 트랜잭션이 없어서 문제 발생
 */

class MemberServiceV1Test {

    public static final String MEMBER1 = "member1";
    public static final String MEMBER2 = "member2";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    @BeforeEach
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);
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

        assertThat(findMember1.getMoney()).isEqualTo(8000);
        assertThat(findMember2.getMoney()).isEqualTo(10000);

    }
}