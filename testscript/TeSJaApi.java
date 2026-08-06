import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

class TeSJaApi {

    private static Class<?>[] argsToClasses(Arg[] args) {
        Class<?>[] classes = null;

        if (args != null) {
            classes = Arrays.stream(args).map(arg -> {

                if (!arg.isPrimitive) {
                    return arg.object.getClass();
                }

                try {
                    return arg.object.getClass().getField("TYPE").get(null);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    e.printStackTrace();
                }

                return null;

            }).toArray(i -> new Class[i]);
        }
        return classes;
    }

    public static Object invokeJavaMethod(Object obj, String methodName, Arg... args)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        try {
            return invokeJavaMethod(obj, obj.getClass().getName(), methodName, args);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Object invokeJavaMethod(Object obj, String className, String methodName, Arg... args)
            throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
            InvocationTargetException {

        Class<?>[] classes = argsToClasses(args);

        try {
            Method method = Class.forName(className).getMethod(methodName, classes);
            return method.invoke(obj, Arg.toObjects(args));
        } catch (NoSuchMethodException e) {

            for (Method method : Class.forName(className).getMethods()) {
                if (method.getParameterTypes().length == classes.length && method.getName().equals(methodName)) {
                    boolean flag=true;
                    for (int n = 0; n < classes.length; n++) {
                        if (!method.getParameterTypes()[n].isAssignableFrom(classes[n])) {
                            flag=false;
                            break;
                        }
                    }

                    

                    if(flag){
                        return method.invoke(obj, Arg.toObjects(args));
                    }
                }
            }


            throw new NoSuchMethodError("invokeJavaMethod "+obj+" "+args[0].object);
            //throw new NoSuchMethodError("invokeJavaMethod "+className+" "+methodName+" "+Arrays.toString(classes));

        }

    }

    public static Object newjavaObject(String name, Arg... args)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        Class<?>[] classes = argsToClasses(args);

        try{
            Constructor<?> constructor = Class.forName(name).getConstructor(classes);
            return constructor.newInstance(Arg.toObjects(args));
        }catch(NoSuchMethodException e){
            for (Constructor<?> constructor : Class.forName(name).getConstructors()) {
                if (constructor.getParameterTypes().length == classes.length) {
                    boolean flag=true;
                    for (int n = 0; n < classes.length; n++) {
                        if (!constructor.getParameterTypes()[n].isAssignableFrom(classes[n])) {
                            flag=false;
                            break;
                        }
                    }

                    

                    if(flag){
                        return constructor.newInstance(Arg.toObjects(args));
                    }
                }
            }
            throw new NoSuchMethodError("newjavaObject "+name+" "+args[0].object);
        }
    }

    public static Object getJavaField(Object obj, String fieldName)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        try {
            return getJavaField(obj, obj.getClass().getName(), fieldName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Object getJavaField(Object obj, String objName, String fieldName) throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {

        Field field = Class.forName(objName).getField(fieldName);

        return field.get(obj);
    }

    public static void setJavaField(Object obj, String fieldName, Object value)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        try {
            setJavaField(obj, obj.getClass().getName(), fieldName, value);
        } catch (ClassNotFoundException e) {
        }
    }

    public static void setJavaField(Object obj, String objName, String fieldName, Object value)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
            ClassNotFoundException {
        Field field = Class.forName(objName).getField(fieldName);

        field.setAccessible(true);

        field.set(obj, value);
    }

    public static class Arg {

        public static Object[] toObjects(Arg[] args) {

            if (args == null)
                return null;

            Object[] objects = new Object[args.length];

            for (int i = 0; i < objects.length; i++) {
                objects[i] = args[i].object;
            }

            return objects;
        }

        public Arg(Object object) {
            this.object = object;
            isPrimitive = false;
        }

        public Arg(boolean bool) {
            this.object = bool;
            isPrimitive = true;
        }

        public Arg(char cha) {
            this.object = cha;
            isPrimitive = true;
        }

        public Arg(byte byt) {
            this.object = byt;
            isPrimitive = true;
        }

        public Arg(short sho) {
            this.object = sho;
            isPrimitive = true;
        }

        public Arg(int in) {
            this.object = in;
            isPrimitive = true;
        }

        public Arg(float floa) {
            this.object = floa;
            isPrimitive = true;
        }

        public Arg(long lon) {
            this.object = lon;
            isPrimitive = true;
        }

        public Arg(double doubl) {
            this.object = doubl;
            isPrimitive = true;
        }

        @Override
        public String toString() {
            return object.toString();
        }

        final Object object;
        final boolean isPrimitive;
    }
}
