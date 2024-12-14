package com.revature.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.revature.util.ConnectionUtil;

class ConnectionTest {

	@Test
	void getConnectionTest() throws SQLException {
		Connection connection = new ConnectionUtil().getConnection();
		assertNotNull(connection, () -> "Connection should not be null");
		connection.close();
	}

}

