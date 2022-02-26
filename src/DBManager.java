import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver"; // 드라이버
	private final String DB_URL = "jdbc:mysql://localhost:3306/mydatabase"; // 접속할 DB 서버
	private final String USER_NAME = "root";
	private final String PASSWORD = "1215";

	public Object[][] Select() {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		String sql = "SELECT * FROM ranking ORDER BY score DESC, stand LIMIT 10";

		Object values[][] = null;
		String[] rank = { "1ST", "2ND", "3RD", "4TH", "5TH", "6TH", "7TH", "8TH", "9TH", "10TH" };

		try {
			Class.forName(JDBC_DRIVER);
			connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = statement.executeQuery(sql);
			rs.last();
			values = new Object[rs.getRow()][];
			rs.beforeFirst();
			int i = 0;
			while (rs.next()) {
				values[i] = new Object[] { rank[i], rs.getString("name"), String.valueOf(rs.getInt("score")),
						rs.getString("difficulty") };
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				statement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	public void Insert(String name, int score, String difficulty) {
		Connection connection = null;
		PreparedStatement pStatement = null;

		String sql = "INSERT INTO ranking (name, score, difficulty) VALUES (?, ?, ?)";

		try {
			Class.forName(JDBC_DRIVER);
			connection = DriverManager.getConnection(DB_URL, USER_NAME, PASSWORD);
			pStatement = connection.prepareStatement(sql);
			pStatement.setString(1, name);
			pStatement.setInt(2, score);
			pStatement.setString(3, difficulty);
			pStatement.executeUpdate(); // 업데이트
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				pStatement.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
