package sqlclient.cli;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import sqlclient.cli.domain.Query;
import sqlclient.cli.z_boot.util.SqlParserUtils;

/**
 * @author Neil Attewell
 */
@Component
public class QueryAliasRegistry {
	@Autowired private SpecialCharacterRegistry characterRegistry;
	private Map<String, Tuple2<String, List<Query>>> fixedAliases = new HashMap();
	private Map<String, Tuple2<String, List<Query>>> aliases = new HashMap<>();

	public void addFixedAlias(SpecialCharacterRegistry characterRegistry, String alias, String queryString) {
		List<Query> queries = SqlParserUtils.parse(queryString, characterRegistry);
		this.fixedAliases.put(alias.toLowerCase(), Tuple.of(alias, queries));
	}

	public List<String> getAliasNames(){
		return Stream.of(this.fixedAliases, this.aliases)
				.map(item -> item.values())
				.flatMap(item -> item.stream())
				.map(item -> item._1)
				.distinct()
				.collect(Collectors.toList());
	}
	public List<Query> getAlias(String input, String delimiter) {
		List<Query> queries = getAlias(input);
		if(queries == null) {
			return null;
		}
		if(delimiter == null) {
			return queries;
		}
		return queries.stream().map(item -> {
			if(StringUtils.isNotBlank(item.getDelimiter())) {
				return item;
			}
			return new Query(item.getParts(), delimiter);
		})
		.collect(Collectors.toList());
	}
	private List<Query> getAlias(String input) {
		input = StringUtils.trimToEmpty(StringUtils.lowerCase(input));
		var alias = this.aliases.get(input);
		if(alias != null) {
			return alias._2;
		}
		alias = this.fixedAliases.get(input);
		if(alias != null) {
			return alias._2;
		}
		return null;
	}

	public void setAlias(String name, String queryString) {
		List<Query> queries = SqlParserUtils.parse(queryString, this.characterRegistry);
		this.aliases.put(name.toLowerCase(), Tuple.of(name, queries));
	}

	public void removeAlias(String name) {
		this.aliases.remove(name.toLowerCase());
	}
}
