package sqlclient.cli.contracts;

import java.io.IOException;

import org.springframework.beans.factory.DisposableBean;

import sqlclient.cli.domain.Query;

/**
 * @author Neil Attewell
 */
public interface IInputSource extends DisposableBean{
	Query read() throws IOException;
}
