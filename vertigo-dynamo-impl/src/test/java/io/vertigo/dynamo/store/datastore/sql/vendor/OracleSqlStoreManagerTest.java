package io.vertigo.dynamo.store.datastore.sql.vendor;

import java.util.List;

import io.vertigo.app.config.AppConfig;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.impl.database.vendor.oracle.OracleDataBase;
import io.vertigo.dynamo.store.data.domain.car.Car;
import io.vertigo.dynamo.store.datastore.sql.AbstractSqlStoreManagerTest;
import io.vertigo.dynamo.store.datastore.sql.SqlDataStoreAppConfig;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;
import oracle.jdbc.OracleDriver;

/**
 * Test of sql storage in Oracle DB.
 * @author mlaroche
 *
 */
public final class OracleSqlStoreManagerTest extends AbstractSqlStoreManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return SqlDataStoreAppConfig.build(
				OracleDataBase.class.getCanonicalName(),
				OracleDriver.class.getCanonicalName(),
				"jdbc:oracle:thin:DT_VERTIGO/DT_VERTIGO@selma.dev.klee.lan.net:1521/O11UTF8");
	}

	@Override
	protected List<String> getCreateFamilleRequests() {
		return new ListBuilder<String>()
				.add(" create table famille(fam_id NUMBER , LIBELLE varchar(255))")
				.add(" create sequence SEQ_FAMILLE start with 10001 increment by 1")
				.build();
	}

	@Override
	protected List<String> getCreateCarRequests() {
		return new ListBuilder<String>()
				.add(" create table fam_car_location(fam_id NUMBER , ID NUMBER)")
				.add(" create table car(ID NUMBER, FAM_ID NUMBER, MAKE varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50) )")
				.add(" create sequence SEQ_CAR start with 10001 increment by 1")
				.build();
	}

	@Override
	protected List<String> getCreateFileInfoRequests() {
		return new ListBuilder<String>()
				.add(" create table VX_FILE_INFO(FIL_ID NUMBER , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH NUMBER, LAST_MODIFIED date, FILE_DATA BLOB)")
				.add(" create sequence SEQ_VX_FILE_INFO start with 10001 increment by 1")
				.build();
	}

	@Override
	protected final List<String> getDropRequests() {
		return new ListBuilder<String>()
				.add(" drop table VX_FILE_INFO ")
				.add(" drop sequence SEQ_VX_FILE_INFO")
				.add(" drop table fam_car_location")
				.add(" drop table car")
				.add(" drop sequence SEQ_CAR")
				.add(" drop table famille")
				.add(" drop sequence SEQ_FAMILLE")
				.build();
	}

	@Override
	protected void nativeInsertCar(final Car car) {
		Assertion.checkArgument(car.getId() == null, "L'id n'est pas null {0}", car.getId());
		//-----
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Domain doCar = definitionSpace.resolve("DO_DT_CAR_DTO", Domain.class);

		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_INSERT_CAR")
				.withEngine(TaskEngineProc.class)
				.withRequest("insert into CAR (ID, FAM_ID,MAKE, MODEL, DESCRIPTION, YEAR, KILO, PRICE, MOTOR_TYPE) values "
						//syntaxe HsqlDb pour sequence.nextval
						+ "(SEQ_CAR.nextval, #DTO_CAR.FAM_ID#, #DTO_CAR.MAKE#, #DTO_CAR.MODEL#, #DTO_CAR.DESCRIPTION#, #DTO_CAR.YEAR#, #DTO_CAR.KILO#, #DTO_CAR.PRICE#, #DTO_CAR.MOTOR_TYPE#)")
				.addInRequired("DTO_CAR", doCar)
				.build();

		final Task task = Task.builder(taskDefinition)
				.addValue("DTO_CAR", car)
				.build();
		final TaskResult taskResult = taskManager
				.execute(task);
		nop(taskResult);
	}

}