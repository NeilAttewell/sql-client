package sqlclient.cli.executors;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sqlclient.core.ApplicationState;
import sqlclient.core.contracts.ICommand;
import sqlclient.core.contracts.IInputExecutor;
import sqlclient.core.domain.Query;

/**
 * @author Neil Attewell
 */
@Component
public class InputExecutorUse implements IInputExecutor, ICommand{
	private final static Pattern PATTERN = Pattern.compile("\\s*use\\s+(?<databaseName>[a-zA-Z_]+\\w*)\\s*",Pattern.CASE_INSENSITIVE);
	@Autowired private ApplicationState state;
	@Autowired private Connection connection;

	@Override
	public boolean isDefault() {
		return false;
	}
	@Override
	public boolean canExecute(Query query) {
		return PATTERN.matcher(query.getQuery()).matches();
	}
	@Override
	public void execute(Query query) throws SQLException {
		try(Statement statement = this.connection.createStatement()){
			statement.execute(query.getQuery());
			var matcher = PATTERN.matcher(query.getQuery());
			matcher.matches();
			this.state.setInputPromptPrefix(matcher.group("databaseName"));
		}
	}
	@Override
	public String getCommand() {
		return "use";
	}
	@Override
	public boolean isCommand(Query query) {
		return canExecute(query);
	}
}