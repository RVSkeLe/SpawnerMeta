package mc.rellox.spawnermeta.version.types;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import mc.rellox.spawnermeta.utility.reflect.Reflect.RF;

final class VersionReflection {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

	private VersionReflection() {}

	static Class<?> craft(String name) {
		return require(RF.craft(name), "CraftBukkit class " + name);
	}

	static Class<?> type(String name) {
		return require(RF.get(name), "class " + name);
	}

	static MethodHandle constructor(Class<?> type, Class<?>... parameters) {
		Constructor<?> constructor = findConstructor(type, parameters);
		try {
			constructor.setAccessible(true);
			return LOOKUP.unreflectConstructor(constructor).asFixedArity();
		} catch (IllegalAccessException e) {
			throw missing("access constructor " + type.getName() + signature(parameters), e);
		}
	}

	static MethodHandle getter(Class<?> type, String name) {
		Field field = findField(type, name);
		if(field == null) throw missing("field " + type.getName() + "#" + name, null);
		try {
			field.setAccessible(true);
			return LOOKUP.unreflectGetter(field);
		} catch (IllegalAccessException e) {
			throw missing("access field " + type.getName() + "#" + name, e);
		}
	}

	static Class<?> fieldType(Class<?> type, String name) {
		Field field = findField(type, name);
		if(field == null) throw missing("field " + type.getName() + "#" + name, null);
		return field.getType();
	}

	static MethodHandle method(Class<?> type, String name, Class<?>... parameters) {
		Method method = findMethod(type, name, parameters);
		if(method == null) throw missing("method " + type.getName() + "#" + name + signature(parameters), null);
		try {
			method.setAccessible(true);
			return LOOKUP.unreflect(method);
		} catch (IllegalAccessException e) {
			throw missing("access method " + type.getName() + "#" + name + signature(parameters), e);
		}
	}

	static MethodHandle methodReturning(Class<?> type, Class<?> returnType) {
		Method found = findMethodReturning(type, returnType);
		try {
			found.setAccessible(true);
			return LOOKUP.unreflect(found);
		} catch (IllegalAccessException e) {
			throw missing("access no-arg method returning " + returnType.getName() + " on " + type.getName(), e);
		}
	}

	static Class<?> returnType(Class<?> type, String name, Class<?>... parameters) {
		Method method = findMethod(type, name, parameters);
		if(method == null) throw missing("method " + type.getName() + "#" + name + signature(parameters), null);
		return method.getReturnType();
	}

	static Object instance(MethodHandle constructor, Object... values) {
		return call(constructor, values);
	}

	static Object invoke(MethodHandle method, Object object, Object... values) {
		if(object == null) return call(method, values);
		Object[] arguments = new Object[values.length + 1];
		arguments[0] = object;
		System.arraycopy(values, 0, arguments, 1, values.length);
		return call(method, arguments);
	}

	static <T> T invoke(MethodHandle method, Object object, Class<T> type, Object... values) {
		return type.cast(invoke(method, object, values));
	}

	static int invokeInt(MethodHandle method, Object object) {
		Object value = invoke(method, object);
		if(value instanceof Number number) return number.intValue();
		throw missing("integer result from method handle " + method.type(), null);
	}

	static Object get(Object object, MethodHandle getter) {
		return call(getter, object);
	}

	private static Object call(MethodHandle handle, Object... arguments) {
		try {
			return handle.invokeWithArguments(arguments);
		} catch (Throwable e) {
			throw missing("invoke method handle " + handle.type(), e);
		}
	}

	private static Constructor<?> findConstructor(Class<?> type, Class<?>... parameters) {
		try { return type.getDeclaredConstructor(parameters); } catch (NoSuchMethodException e) {}
		try { return type.getConstructor(parameters); } catch (NoSuchMethodException e) {}
		throw missing("constructor " + type.getName() + signature(parameters), null);
	}

	private static Method findMethodReturning(Class<?> type, Class<?> returnType) {
		Method found = null;
		for(Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
			for(Method candidate : current.getDeclaredMethods()) {
				if(candidate.getParameterCount() != 0 || candidate.getReturnType() != returnType) continue;
				if(found != null && !sameSignature(found, candidate))
					throw missing("unique no-arg method returning " + returnType.getName() + " on " + type.getName(), null);
				found = candidate;
			}
		}
		if(found == null) throw missing("no-arg method returning " + returnType.getName() + " on " + type.getName(), null);
		return found;
	}

	private static boolean sameSignature(Method a, Method b) {
		return a.getName().equals(b.getName()) && Arrays.equals(a.getParameterTypes(), b.getParameterTypes());
	}

	private static Field findField(Class<?> type, String name) {
		if(type == null || type == Object.class) return null;
		try { return type.getDeclaredField(name); } catch (NoSuchFieldException e) {}
		try { return type.getField(name); } catch (NoSuchFieldException e) {}
		return findField(type.getSuperclass(), name);
	}

	private static Method findMethod(Class<?> type, String name, Class<?>... parameters) {
		if(type == null || type == Object.class) return null;
		try { return type.getDeclaredMethod(name, parameters); } catch (NoSuchMethodException e) {}
		try { return type.getMethod(name, parameters); } catch (NoSuchMethodException e) {}
		return findMethod(type.getSuperclass(), name, parameters);
	}

	private static <T> T require(T value, String what) {
		if(value != null) return value;
		throw missing("resolve " + what, null);
	}

	private static IllegalStateException missing(String what, Throwable cause) {
		String message = "Unable to " + what;
		return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
	}

	private static String signature(Class<?>... parameters) {
		return Arrays.stream(parameters).map(Class::getName).collect(Collectors.joining(", ", "(", ")"));
	}
}
