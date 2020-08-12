package com.capgemini.mrchecker.database.unit.base;

import com.capgemini.mrchecker.database.tags.UnitTest;
import com.capgemini.mrchecker.database.unit.base.database.MySqlDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@UnitTest
public class EntityManagerTest {

	private MySqlDatabase mySqlDatabase;

	@BeforeEach
	public void setUp() throws Exception {
		mySqlDatabase = new MySqlDatabase();
	}

	@Test
	public void testShouldValidateDatabaseUnitName() {
		//given
		String exampleDatabaseUnitName = "mySqlExample";

		//when
		//then
		assertTrue(mySqlDatabase.getDatabaseUnitName().equals(exampleDatabaseUnitName));
	}

	@Test
	public void testShouldValidateEntityManagerCreation() {
		//given
		//when
		//then
		assertNotNull(mySqlDatabase.getEntityManager());
	}

}