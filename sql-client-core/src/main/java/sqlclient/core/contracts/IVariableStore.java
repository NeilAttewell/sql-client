package sqlclient.core.contracts;

import java.util.List;

public interface IVariableStore {
	public void clearAll();
	public void set(String name,String value);
	public void remove(String name);
	public String get(String name);
	public List<String> getNames();
}
