/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.hydrator.plugin.db.batch.action;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Macro;
import co.cask.cdap.etl.api.batch.BatchActionContext;
import co.cask.hydrator.common.batch.action.Condition;
import co.cask.hydrator.common.batch.action.ConditionConfig;
import co.cask.hydrator.plugin.ConnectionConfig;
import co.cask.hydrator.plugin.DBManager;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nullable;

/**
 * Config for {@link QueryAction}.
 */
public class QueryConfig extends ConnectionConfig {

  public static final String JDBC_PLUGIN_ID = "driver";

  @Description("The query to run.")
  @Macro
  public String query;

  public QueryConfig() {
    super();
  }

  public void executeQuery(Class<? extends Driver> driverClass, Logger log) {
    DBManager dbManager = new DBManager(this);

    try {
      dbManager.ensureJDBCDriverIsAvailable(driverClass);

      try (Connection connection = getConnection()) {
        if (!this.enableAutoCommit) {
          connection.setAutoCommit(false);
        }
        try (Statement statement = connection.createStatement()) {
          statement.execute(this.query);
          if (!this.enableAutoCommit) {
            connection.commit();
          }
        }
      }
    } catch (Exception e) {
      log.error("Error running query {}.", this.query, e);
    } finally {
      dbManager.destroy();
    }
  }

  private Connection getConnection() throws SQLException {
    if (this.user == null) {
      return DriverManager.getConnection(this.connectionString);
    } else {
      return DriverManager.getConnection(this.connectionString, this.user, this.password);
    }
  }
}
