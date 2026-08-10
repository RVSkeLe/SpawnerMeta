package mc.rellox.spawnermeta.version.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import mc.rellox.spawnermeta.utility.reflect.Reflect.RF;

final class VersionReflection {

	private VersionReflection() {}

	static Class<?> craft(String name) {
		return require(RF.craft(name), "CraftBukkit class " + name);
	}

	static Class<?> type(String name) {
		return require(RF.get(name), "class " + name);
	}

	static Constructor<?> constructor(Class<?> type, Class<?>... parameters) {
		try {
			Constructor<?> constructor = type.getDeclaredConstructor(parameters);
			constructor.setAccessible(true);
			return constructor;
		} catch (NoSuchMethodException e) {}
		try {
			Constructor<?> constructor = type.getConstructor(parameters);
			constructor.setAccessible(true);
			return constructor;
		} catch (NoSuchMethodException e) {}
		throw missing("constructor " + type.getName() + signature(parameters), null);
	}

	static Field field(Class<?> type, String name) {
		Field field = findField(type, name);
		if(field == null)
			throw missing("field " + type.getName() + "#" + name, null);
		field.setAccessible(true);
		return field;
	}

	static Field doubleField(Class<?> type, String name) {
		Field field = field(type, name);
		if(field.getType() != double.class)
			throw missing("double field " + type.getName() + "#" + name
					+ " (found " + field.getType().getName() + ")", null);
		return field;
	}

	static Method method(Class<?> type, String name, Class<?>... parameters) {
		Method method = findMethod(type, name, parameters);
		if(method == null)
			throw missing("method " + type.getName() + "#" + name + signature(parameters), null);
		method.setAccessible(true);
		return method;
	}

	static Object instance(Constructor<?> constructor, Object... values) {
		try {
			return constructor.newInstance(values);
		} catch (Exception e) {
			throw missing("instantiate " + constructor.getDeclaringClass().getName(), e);
		}
	}

	static Object invoke(Method method, Object object, Object... values) {
		try {
			return method.invoke(object, values);
		} catch (Exception e) {
			throw missing("invoke " + method.getDeclaringClass().getName()
					+ "#" + method.getName(), e);
		}
	}

	static <T> T invoke(Method method, Object object, Class<T> type, Object... values) {
		Object value = invoke(method, object, values);
		return type.cast(value);
	}

	static double invokeDouble(Method method, Object object) {
		Object value = invoke(method, object);
		if(value instanceof Number number) return number.doubleValue();
		throw missing("numeric result from " + method.getDeclaringClass().getName()
				+ "#" + method.getName(), null);
	}

	static int invokeInt(Method method, Object object) {
		Object value = invoke(method, object);
		if(value instanceof Number number) return number.intValue();
		throw missing("integer result from " + method.getDeclaringClass().getName()
				+ "#" + method.getName(), null);
	}

	static Object get(Object object, Field field) {
		try {
			return field.get(object);
		} catch (Exception e) {
			throw missing("read field " + field.getDeclaringClass().getName()
					+ "#" + field.getName(), e);
		}
	}

	static void setDouble(Object object, Field field, double value) {
		try {
			field.setDouble(object, value);
		} catch (Exception e) {
			throw missing("write double field " + field.getDeclaringClass().getName()
					+ "#" + field.getName(), e);
		}
	}

	static Class<?> returnType(Method method) {
		return method.getReturnType();
	}

	static Class<?> fieldType(Field field) {
		return field.getType();
	}

	private static Field findField(Class<?> type, String name) {
		if(type == null || type == Object.class) return null;
		try {
			return type.getDeclaredField(name);
		} catch (NoSuchFieldException e) {}
		try {
			return type.getField(name);
		} catch (NoSuchFieldException e) {}
		return findField(type.getSuperclass(), name);
	}

	private static Method findMethod(Class<?> type, String name, Class<?>... parameters) {
		if(type == null || type == Object.class) return null;
		try {
			return type.getDeclaredMethod(name, parameters);
		} catch (NoSuchMethodException e) {}
		try {
			return type.getMethod(name, parameters);
		} catch (NoSuchMethodException e) {}
		return findMethod(type.getSuperclass(), name, parameters);
	}

	private static <T> T require(T value, String what) {
		if(value != null) return value;
		throw missing("resolve " + what, null);
	}

	private static IllegalStateException missing(String what, Exception cause) {
		String message = "Unable to " + what;
		return cause == null ? new IllegalStateException(message)
				: new IllegalStateException(message, cause);
	}

	private static String signature(Class<?>... parameters) {
		return Arrays.stream(parameters)
				.map(Class::getName)
				.collect(Collectors.joining(", ", "(", ")"));
	}

}
