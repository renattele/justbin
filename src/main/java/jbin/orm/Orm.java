package jbin.orm;

import jbin.util.SqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@SuppressWarnings("unchecked")
@Slf4j
public class Orm {
    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate namedTemplate;
    private final SqlUtil util;

    public Orm(DataSource dataSource) {
        this.dataSource = dataSource;
        var template = new JdbcTemplate(dataSource);
        this.namedTemplate = new NamedParameterJdbcTemplate(template);
        this.util = new SqlUtil(dataSource);
    }

    public <T> T create(Class<T> clazz) {
        var table = clazz.getAnnotation(Table.class);
        createSql(table);
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                (proxy, method, args) -> executeMethod(method, args));
    }

    private Object executeMethod(Method method, Object[] args) {
        var querySql = getQuerySql(method);
        if (querySql == null) return null;

        var returnType = method.getReturnType();
        UUID id = null;
        try {
            var params = new MapSqlParameterSource();
            if (args != null) {
                id = fillParamsAndGetId(method, args, params);
            }
            return processResult(method, querySql, id, params);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return returnFailure(returnType);
        }
    }

    private String getQuerySql(Method method) {
        var query = method.getAnnotation(Query.class);
        return query.value();
    }

    private Object processResult(Method method, String query, UUID id, MapSqlParameterSource params) {
        var returnType = method.getReturnType();
        if (returnType == boolean.class) {
            return namedTemplate.update(query, params) > 0;
        } else if (returnType == UUID.class) {
            var result = namedTemplate.update(query, params);
            return result > 0 ? id : null;
        } else if (returnType == List.class) {
            var rawType = (ParameterizedType) method.getGenericReturnType();
            var listType = (Class<?>) rawType.getActualTypeArguments()[0];
            return namedTemplate.query(query, params, new Mapper(listType));
        } else if (returnType == Optional.class) {
            var rawType = (ParameterizedType) method.getGenericReturnType();
            var optionalType = (Class<?>) rawType.getActualTypeArguments()[0];
            if (optionalType == UUID.class) {
                var result = namedTemplate.update(query, params);
                return result > 0 ? Optional.of(id) : Optional.empty();
            }
            var result = namedTemplate.query(query, params, new Mapper(optionalType));
            return Optional.ofNullable(result.isEmpty() ? null : result.get(0));
        } else {
            return namedTemplate.queryForObject(query, params, new Mapper(returnType));
        }
    }

    private UUID fillParamsAndGetId(Method method, Object[] args, MapSqlParameterSource params)
            throws IllegalAccessException, InvocationTargetException {
        UUID id = null;
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            var argCurrent = args[argIndex];
            var argClass = argCurrent.getClass();
            if (argClass.isRecord()) {
                var components = argClass.getRecordComponents();
                for (RecordComponent component : components) {
                    var type = component.getType();
                    var value = component.getAccessor().invoke(argCurrent);
                    if (type == UUID.class && component.getAnnotation(Id.class) != null) {
                        id = value == null ? UUID.randomUUID() : (UUID) value;
                        params.addValue(component.getName(), id, Types.OTHER);
                    } else {
                        params.addValue(component.getName(), value, Optional.ofNullable(value != null ? sqlTypes.get(value.getClass()) : null).orElse(Types.OTHER));
                    }
                }
            } else {
                var paramName = method.getParameters()[argIndex].getName();
                params.addValue(paramName, argCurrent, Optional.ofNullable(sqlTypes.get(argCurrent.getClass())).orElse(Types.OTHER));
            }
        }
        return id;
    }

    private static final Map<Class<?>, Integer> sqlTypes = Map.of(
            String.class, Types.VARCHAR,
            Integer.class, Types.INTEGER,
            Long.class, Types.BIGINT,
            Boolean.class, Types.BOOLEAN,
            Float.class, Types.FLOAT);

    private Object returnFailure(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        } else
            return null;
    }

    private void createSql(Table table) {
        if (!util.tableExists(table.name())) {
            try (var connection = dataSource.getConnection()) {
                var statement = connection.createStatement();
                statement.executeUpdate(table.createTable());
                statement.closeOnCompletion();
            } catch (SQLException e) {
                log.error(e.toString());
            }
        }
    }

    private record Mapper(Class<?> returnType) implements RowMapper<Object> {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) {
            try {
                return objectFromResult(rs, returnType);
            } catch (Exception e) {
                log.error(e.toString());
                return null;
            }
        }

        private Object objectFromResult(ResultSet result, Class<?> returnType)
                throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
            var constructor = returnType.getConstructors()[0];
            var args = new Object[constructor.getParameterCount()];
            Parameter[] parameters = constructor.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                var parameter = parameters[i];
                var name = parameter.getName();
                var nameAnnotation = parameter.getAnnotation(DbName.class);
                var finalName = nameAnnotation != null ? nameAnnotation.value() : name;
                args[i] = parameterFromResult(result, parameter, finalName);
            }
            return constructor.newInstance(args);
        }

        private Object parameterFromResult(ResultSet result, Parameter parameter, String name) throws SQLException {
            var type = parameter.getType();
            if (type == boolean.class) {
                return result.getBoolean(name);
            } else if (type == Integer.class) {
                return result.getInt(name);
            } else if (type == Long.class) {
                return result.getLong(name);
            } else if (type == Float.class) {
                return result.getFloat(name);
            } else if (type == Double.class) {
                return result.getDouble(name);
            } else if (type == String.class) {
                return result.getString(name);
            } else if (type == Instant.class) {
                return result.getTimestamp(name).toInstant();
            } else {
                return result.getObject(name);
            }
        }
    }
}
