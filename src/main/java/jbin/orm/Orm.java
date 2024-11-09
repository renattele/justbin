package jbin.orm;

import jbin.util.SqlUtil;

import java.lang.reflect.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;

@SuppressWarnings("unchecked")
public class Orm {
    private final Connection connection;
    private final SqlUtil util;

    public Orm(Connection connection) {
        this.connection = connection;
        this.util = new SqlUtil(connection);
    }

    public <T> T create(Class<T> clazz) {
        var table = clazz.getAnnotation(Table.class);
        createSql(table);
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                (proxy, method, args) -> executeMethod(table.name(), method, args));
    }

    private Object executeMethod(String tableName, Method method, Object[] args) {
        var querySql = getQuerySql(tableName, method);
        if (querySql == null) {
            return null;
        }
        var returnType = method.getReturnType();
        UUID id = null;
        try {
            var statement = connection.prepareStatement(querySql);
            if (args != null) {
                id = fillStatementAndGetUUID(args, id, statement);
            }
            statement.closeOnCompletion();
            return processResult(method, returnType, statement, id);
        } catch (Exception e) {
            e.printStackTrace();
            return returnFailure(returnType);
        }
    }

    private String getQuerySql(String tableName, Method method) {
        var query = method.getAnnotation(Query.class);
        return query.value();
    }

    private Object processResult(Method method, Class<?> returnType, PreparedStatement statement, UUID id)
            throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (returnType == boolean.class) {
            return statement.executeUpdate() > 0;
        } else if (returnType == UUID.class) {
            var result = statement.executeUpdate();
            return result > 0 ? id : null;
        } else if (returnType == List.class) {
            var result = statement.executeQuery();
            var list = new ArrayList<>();
            var rawType = (ParameterizedType) method.getGenericReturnType();
            var listType = (Class<?>) rawType.getActualTypeArguments()[0];
            while (result.next()) {
                list.add(objectFromResult(result, listType));
            }
            return list;
        } else if (returnType == Optional.class) {
            var rawType = (ParameterizedType) method.getGenericReturnType();
            var optionalType = (Class<?>) rawType.getActualTypeArguments()[0];
            if (optionalType == UUID.class) {
                var result = statement.executeUpdate();
                return result > 0 ? Optional.of(id) : Optional.empty();
            }
            var result = statement.executeQuery();
            return result.next() ? Optional.of(objectFromResult(result, optionalType)) : Optional.empty();
        } else {
            var result = statement.executeQuery();
            return result.next() ? objectFromResult(result, returnType) : null;
        }
    }

    private UUID fillStatementAndGetUUID(Object[] args, UUID id, PreparedStatement statement)
            throws IllegalAccessException, InvocationTargetException, SQLException {
        int count = 1;
        for (Object currentArg : args) {
            var argClass = currentArg.getClass();
            if (argClass.isRecord()) {
                var components = argClass.getRecordComponents();
                for (RecordComponent component : components) {
                    var value = component.getAccessor().invoke(currentArg);
                    var type = component.getType();
                    if (type == UUID.class && component.getAnnotation(Id.class) != null) {
                        id = value == null ? UUID.randomUUID() : (UUID) value;
                        setAutoStatement(statement, count++, id, type);
                    } else {
                        setAutoStatement(statement, count++, value, type);
                    }
                }
            } else {
                setAutoStatement(statement, count++, currentArg, currentArg.getClass());
            }
        }
        return id;
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

    private void setAutoStatement(PreparedStatement statement, int count, Object value, Class<?> valueType)
            throws SQLException {
        if (valueType == boolean.class) {
            statement.setBoolean(count, (Boolean) value);
        } else if (valueType == Integer.class) {
            statement.setInt(count, (Integer) value);
        } else if (valueType == Long.class) {
            statement.setLong(count, (Long) value);
        } else if (valueType == Double.class) {
            statement.setDouble(count, (Double) value);
        } else if (valueType == String.class) {
            statement.setString(count, (String) value);
        } else if (valueType == UUID.class) {
            statement.setObject(count, value);
        } else if (valueType == Instant.class) {
            statement.setTimestamp(count, Timestamp.from((Instant) value));
        } else {
            statement.setObject(count, value);
        }
    }

    private Object returnFailure(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        } else
            return null;
    }

    private void createSql(Table table) {
        if (!util.tableExists(table.name())) {
            try {
                var statement = connection.createStatement();
                statement.executeUpdate(table.createTable());
                statement.closeOnCompletion();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
