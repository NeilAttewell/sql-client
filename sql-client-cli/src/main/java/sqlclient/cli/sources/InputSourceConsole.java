package sqlclient.cli.sources;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import sqlclient.cli.ApplicationState;

/**
 * @author Neil Attewell
 */
@Component
public class InputSourceConsole extends AbstractInputSource{
	@Autowired private LineReader lineReader;
	@Autowired private ApplicationState state;
	@Autowired @Qualifier("dbType")
	private String dbType;
	public InputSourceConsole() {
	}
	@PostConstruct
	public void init() {
	}
	@Override
	public Tuple2<String, Boolean> readLine() throws IOException {
		try {
			return Tuple.of(this.lineReader.readLine(this.dbType + " SQL "+(this.state.isAutoCommit() ? StringUtils.rightPad(" #" + this.state.getUpdateCount(), 5, " ") : "")+"> "), true);
		}catch (EndOfFileException|UserInterruptException e) {
			return null;
		}
	}
	public void destroy() throws Exception {
	}
}
