package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - Datasource 사용, JDBCUtils 사용
 * */

@Slf4j
public class MemberRepositoryV1 {

    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?,?)";

        Connection con = null;
        PreparedStatement pstmt = null; // preparedStatement 는 파라미터 바인딩이 가능 -> sqlInjection을 막을 수 있다.


        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 준비가 된 쿼리가 실행 -> 결과값이 int 인데 처리된 row 의 갯수가 나옴
            return member;

        } catch (SQLException e) {

            log.error("db error", e);
            throw e;
        }finally {
            close(con, pstmt, null); // 리소스 정리 -> 예외 여부와 상관없이 반드시 close 해줘야함
        }

    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found memberId="+memberId);
            }


        }catch(SQLException e){
            log.error("error", e);
            throw e;
        }finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {

        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);


            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {

            log.error("db error", e);
            throw e;
        }finally {
            close(con, pstmt, null); // 리소스 정리 -> 예외 여부와 상관없이 반드시 close 해줘야함
        }
    }

    public void delete(String memberId) throws SQLException {

        String sql = "delete from  member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);


            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {

            log.error("db error", e);
            throw e;
        }finally {
            close(con, pstmt, null); // 리소스 정리 -> 예외 여부와 상관없이 반드시 close 해줘야함
        }

    }

    private void close(Connection con, Statement stmt, ResultSet resultSet) {

        JdbcUtils.closeResultSet(resultSet);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);

    }

    private Connection getConnection() throws SQLException {

        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}",con,con.getClass());
        return con;
    }

}
