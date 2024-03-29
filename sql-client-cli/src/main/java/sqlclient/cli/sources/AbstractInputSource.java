package sqlclient.cli.sources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import io.vavr.Tuple2;
import sqlclient.cli.contracts.IInputSource;
import sqlclient.core.QueryAliasRegistry;
import sqlclient.core.SpecialCharacterRegistry;
import sqlclient.core.contracts.ICommand;
import sqlclient.core.domain.Query;
import sqlclient.core.domain.QueryPart;
import sqlclient.core.util.SqlParserUtils;

/**
 * @author Neil Attewell
 */
public abstract class AbstractInputSource implements IInputSource{
	@Autowired private SpecialCharacterRegistry registry;
	@Autowired private QueryAliasRegistry aliasRegistry;
	@Autowired @Lazy 
	private List<? extends ICommand> commands;

	private List<Query> queries = new ArrayList<>();
	private Query partialQuery;
	
	@Override
	public synchronized final Query read() throws IOException{
		if(!this.queries.isEmpty()) {
			return this.queries.remove(0);
		}
		
		Tuple2<String, Boolean> line = readLine();
		if(line == null) {
			Query out = this.partialQuery;
			this.partialQuery = null;
			return out;
		}
		if(line._2 == true) {
			if(this.partialQuery != null) {
				this.queries.add(this.partialQuery);
				this.partialQuery=null;
			}
		}
		if(StringUtils.isBlank(line._1)) {
			return read();
		}
		List<Query> queries = SqlParserUtils.parse(line._1, this.registry);
		for(int i = 0 ; i < queries.size(); i++) {
			Query query = queries.get(i);
			if(i == 0 && this.partialQuery != null) {
				List<QueryPart> parts = new ArrayList<>();
				parts.addAll(this.partialQuery.getParts());
				parts.addAll(query.getParts());
				this.queries.add(new Query(parts, query.getDelimiter(), query.isPartOfMultiQuery()));
				this.partialQuery=null;
				continue;
			}
			if(i == queries.size()-1 && StringUtils.isBlank(query.getDelimiter()) && !isComplete(query)) {
				this.partialQuery=new Query(query.getParts(), this.registry.getPrimaryDelimiter(), query.isPartOfMultiQuery());
				continue;
			}
			this.queries.add(query);
		}
		return read();
	}
	protected boolean isComplete(Query query) {
		if(StringUtils.isNotBlank(query.getDelimiter())) {
			return true;
		}
		var alias = this.aliasRegistry.getAlias(query.getQuery(), query.getDelimiter());
		if(alias != null) {
			return true;
		}
		if(this.commands != null && this.commands.stream().filter(item -> item.isCommand(query)).count() > 0) {
			return true;
		}
		return false;
	}
	public abstract Tuple2<String, Boolean> readLine() throws IOException;
}
