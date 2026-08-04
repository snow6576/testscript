import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

public class TestScript {

    public TestScript() {

        variable.put("println", new Value(TYPE.FUNCTION,
                """

                        out = tes getStaticField java.lang.System out

                        arg = tes invokeMethod arg toString

                        tes invokeMethod out println arg

                        """));

        variable.put("getTesValue", new Value(TYPE.FUNCTION,
                """

                        object = tes invokeMethod arg get object
                        key = tes invokeMethod arg get key

                        arg = tes invokeMethod object get key
                        arg = tes javaToTes arg
                        

                        """));

    }

    private TeSJaApi.Arg strToObject(String string) {

        if (string.matches("-?\\d+(\\.\\d+)?")) {
            if (string.contains(".")) {
                return new TeSJaApi.Arg(Double.parseDouble(string));
            }
            return new TeSJaApi.Arg(Integer.parseInt(string));
        }

        return new TeSJaApi.Arg(string);
    }

    private TeSJaApi.Arg[] for_nant_args(String[] strings) {
        TeSJaApi.Arg[] objects = new TeSJaApi.Arg[strings.length];

        for (int i = 0; i < objects.length; i++) {
            String str = strings[i];

            if (variable.containsKey(str)) {

                Value value = variable.get(str);

                switch (value.type) {
                    case TES_OBJECT:
                        objects[i] = strToObject((String) value.object);
                        break;

                    case JAVA_OBJECT:
                        objects[i] = new TeSJaApi.Arg(value.object);
                        break;

                    default:
                }

            } else {
                objects[i] = strToObject(str);
            }
        }
        return objects;
    }

    private String for_fun_arg(String fun_arg) {

        if (variable.containsKey(fun_arg)) {
            Value value = variable.get(fun_arg);
            if (value.type == TYPE.TES_OBJECT)
                return (String) value.object;
        }

        return fun_arg;

    }

    private Value native_func(String[] strings) {
        String fun_name = strings[0];

        String[] fun_args = Arrays.copyOfRange(strings, 1, strings.length);

        switch (fun_name) {

            case "newTes" -> {
                HashMap<String, Object> hashMap = new HashMap<>();

                for (int n = 0; n < fun_args.length; n = n + 2) {
                    hashMap.put(fun_args[n], calculate(new String[] { fun_args[n + 1] }).object);
                }

                return new Value(TYPE.JAVA_OBJECT, hashMap);
            }

            case "newJava" -> {
                try {
                    return new Value(TYPE.JAVA_OBJECT, TeSJaApi.newjavaObject(for_fun_arg(fun_args[0]),
                            for_nant_args(Arrays.copyOfRange(fun_args, 1, fun_args.length))));
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException
                        | ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            case "invokeMethod" -> {
                try {
                    return new Value(TYPE.JAVA_OBJECT, TeSJaApi.invokeJavaMethod(variable.get(fun_args[0]).object,
                            for_fun_arg(fun_args[1]), for_nant_args(Arrays.copyOfRange(fun_args, 2, fun_args.length))));
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();

                    return null;
                }
            }
            case "invokeStaticMethod" -> {
                try {
                    return new Value(TYPE.JAVA_OBJECT, TeSJaApi.invokeJavaMethod(null, for_fun_arg(fun_args[0]),
                            for_fun_arg(fun_args[1]), for_nant_args(Arrays.copyOfRange(fun_args, 2, fun_args.length))));
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException
                        | ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            case "getField" -> {
                try {
                    return new Value(TYPE.JAVA_OBJECT,
                            TeSJaApi.getJavaField(variable.get(fun_args[0]).object, for_fun_arg(fun_args[1])));
                } catch (IllegalAccessException | IllegalArgumentException
                        | SecurityException | NoSuchFieldException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            case "getStaticField" -> {
                try {
                    return new Value(TYPE.JAVA_OBJECT,
                            TeSJaApi.getJavaField(null, for_fun_arg(fun_args[0]), for_fun_arg(fun_args[1])));
                } catch (IllegalAccessException | IllegalArgumentException
                        | SecurityException | NoSuchFieldException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            case "setField" -> {

                Object val;

                if (variable.containsKey(fun_args[2])) {
                    val = variable.get(fun_args[2]).object;
                } else {
                    String string = fun_args[2];
                    if (string.matches("-?\\d+(\\.\\d+)?")) {
                        if (string.contains(".")) {
                            val = Double.parseDouble(string);
                        } else {
                            val = Integer.parseInt(string);
                        }
                    } else {
                        val = string;
                    }
                }

                try {
                    TeSJaApi.setJavaField(variable.get(fun_args[0]).object, for_fun_arg(fun_args[1]), val);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException e) {
                    e.printStackTrace();
                }

                return null;
            }
            case "setStaticField" -> {

                Object val;

                if (variable.containsKey(fun_args[2])) {
                    val = variable.get(fun_args[2]).object;
                } else {
                    String string = fun_args[2];
                    if (string.matches("-?\\d+(\\.\\d+)?")) {
                        if (string.contains(".")) {
                            val = Double.parseDouble(string);
                        } else {
                            val = Integer.parseInt(string);
                        }
                    } else {
                        val = string;
                    }
                }

                try {
                    TeSJaApi.setJavaField(null, for_fun_arg(fun_args[0]), for_fun_arg(fun_args[1]), val);
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                        | SecurityException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                return null;
            }
            case "javaToTes" -> {
                return new Value(TYPE.TES_OBJECT, variable.get(fun_args[0]).object.toString());
            }
            case "if" -> {
                if (Integer.parseInt((String) calculate(new String[] { fun_args[0] }).object) == 1) {
                    run((String) variable.get(fun_args[1]).object);
                }
                return null;
            }
            case "while" -> {
                while (Integer.parseInt((String) calculate(new String[] { fun_args[0] }).object) == 1) {
                    run((String) variable.get(fun_args[1]).object);
                }
                return null;
            }
            case "equal" -> {
                return new Value(TYPE.TES_OBJECT,
                        String.valueOf(new BigDecimal((String) calculate(new String[] { fun_args[0] }).object)
                                .compareTo(new BigDecimal((String) calculate(new String[] { fun_args[1] }).object)) == 0
                                        ? 1
                                        : 0));
            }
            case "bigger" -> {
                return new Value(TYPE.TES_OBJECT,
                        String.valueOf(new BigDecimal((String) calculate(new String[] { fun_args[0] }).object)
                                .compareTo(new BigDecimal((String) calculate(new String[] { fun_args[1] }).object)) == 1
                                        ? 1
                                        : 0));
            }
            case "less" -> {
                return new Value(TYPE.TES_OBJECT, String
                        .valueOf(new BigDecimal((String) calculate(new String[] { fun_args[0] }).object).compareTo(
                                new BigDecimal((String) calculate(new String[] { fun_args[1] }).object)) == -1 ? 1
                                        : 0));
            }
            case "load" ->{

                List<String> list;
                try {
                    list = Files.readAllLines(Path.of(fun_args[0]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                run(list.toArray(new String[list.size()]));


                return null;
            }
            default -> {
                return null;
            }
        }

    }

    VariablesMap variable = new VariablesMap();

    Stack<VariablesMap> variable_stack=new Stack<>();

    DefineFunction defineFunction = null;

    public void run(String[] rawCode) {

        for (String string : rawCode) {
            line(parseLine(string), string);
        }

    }

    public void run(String rawCode) {
        run(rawCode.split("\\r?\\n"));
    }

    private String[] parseLine(String line) {
        List<String> list=new ArrayList<>();

        String temp="";
        boolean str_mark=false;
        for (int n=0;n<line.length();n++){
            char c=line.charAt(n);

            if (str_mark){

                if (c=='"'){
                    str_mark=false;

                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("\"");
                    temp="";
                }
                else
                    temp+=c;
            }else
                switch (c){

                case '"':
                    str_mark=true;
                    list.add("\"");
                    break;
                case ',':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add(",");
                    temp="";
                    break;

                case '+':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("+");
                    temp="";
                    break;

                case '-':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("-");
                    temp="";
                    break;

                case '/':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("/");
                    temp="";
                    break;

                case '*':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("*");
                    temp="";
                    break;

                case '=':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("=");
                    temp="";
                    break;

                case ')':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add(")");
                    temp="";
                    break;

                case '(':
                    if (!temp.isEmpty())
                        list.add(temp);
                    list.add("(");
                    temp="";
                    break;

                case ' ':
                    if (!temp.isEmpty())
                        list.add(temp);
                    temp="";
                    break;
                default:
                    temp+=c;
            }

        }

        list.add(temp);
        return list.toArray(new String[list.size()]);
    }

    private void line(String[] line, String line_ori) {

        if(line[0].equals("//"))return;

        if (defineFunction != null) {

            if (line[0].equals("endfunc")) {
                defineFunction.toValueFunction(variable);
                defineFunction = null;

                return;
            }

            defineFunction.codes.add(line_ori);

            return;
        }

        int atEqual = Arrays.asList(line).indexOf("=");
        if (atEqual != -1) {

            if (line[atEqual + 1].equals("func")) {
                defineFunction = new DefineFunction(line[atEqual - 1]);
                return;
            }

            if (line[atEqual + 1].equals("tes")) {
                Value value = native_func(Arrays.copyOfRange(line, atEqual + 1 + 1, line.length));
                variable.put(line[atEqual - 1], value);
                return;
            }

            Value value = evaluation(Arrays.copyOfRange(line, atEqual + 1, line.length));

            variable.put(line[atEqual - 1], value);
        } else {

            if (line[0].equals("tes")) {
                native_func(Arrays.copyOfRange(line, 1, line.length));
                return;
            }

            evaluation(line);

        }

    }

    /*public Value evaluation(String[] formula) {
        System.out.print(Arrays.toString(formula));
        List<Integer> list=new ArrayList<>();
        list.add(-1);
        for (int n=0;n<formula.length;n++){
            if (formula[n].equals(",")){
                list.add(n);
            }
        }
        list.add(formula.length);

        if (list.size()==2){
            return evaluation2(formula);
        }

        List result=new ArrayList();
        for (int n=0;n<list.size()-1;n++){
            String[] arr=new String[list.get(n+1)-list.get(n)-1];
            int first=list.get(n)+1;
            for (int n2=0;n2<arr.length;n2++){
                arr[n2]=formula[first];
                first++;
            }
            result.add(evaluation2(arr).object);
        }

        return new Value(TYPE.JAVA_OBJECT,result);
    }*/

    public Value evaluation(String[] formula) {

        if (formula[0].equals("\"")) {

            return new Value(TYPE.TES_OBJECT, formula[1]);

        }

        return calculate(calculateFormula(formula));
    }

    public String[] calculateFormula(String[] formula) {

        int count = 1;

        int[] counts = new int[formula.length];
        int max = 0;

        for (int n = 0; n < formula.length; n++) {
            String e = formula[n];

            switch (e) {
                case ")":
                    count--;
                    break;

                case "(":
                    count++;

                default:
                    counts[n] = count;

                    if (max < count)
                        max = count;
            }
        }

        if (max > 1) {

            boolean isMaxBefore = false;
            int startAt = 0;

            String[] new_result = new String[formula.length];
            int new_result_n = 0;

            for (int n = 0; n < counts.length; n++) {
                if (counts[n] == max) {

                    if (!isMaxBefore) {
                        startAt = n;
                    }

                } else {
                    if (isMaxBefore) {
                        int endAt = n;
                        Value result_String = calculate(Arrays.copyOfRange(formula, startAt + 1, endAt));

                        if (startAt - 1 >= 0) {
                            if (variable.containsKey(formula[startAt - 1])) {
                                Value function = variable.get(formula[startAt - 1]);
                                if (function.type == TYPE.FUNCTION) {
                                    Value temp_arg = arg;

                                    arg = result_String;

                                    

                                    variable_stack.push(variable);

                                    VariablesMap temp=new VariablesMap();

                                    for(String key:variable.keySet()){
                                        Value value=variable.get(key);

                                        if(value.type == TYPE.FUNCTION){
                                            temp.put(key, value);
                                        }
                                    }

                                    variable=temp;
                                    
                                    

                                    run((String) function.object);

                                    variable=variable_stack.pop();


                                    if (arg.type != TYPE.TES_OBJECT) {
                                        result_String = new Value(TYPE.TES_OBJECT, "0");
                                    } else {
                                        result_String = arg;
                                    }

                                    arg = temp_arg;

                                    new_result_n--;
                                }
                            }
                        }


                        new_result[new_result_n] = (String) result_String.object;
                        new_result_n++;

                    } else {

                        new_result[new_result_n] = formula[n];
                        new_result_n++;

                    }

                }

                isMaxBefore = counts[n] == max;
            }

            new_result = Arrays.copyOfRange(new_result, 0, new_result_n);

            new_result = calculateFormula(new_result);

            return new_result;
        }

        return formula;

    }

    private Value calculate(String[] formula){
        List<Integer> list=new ArrayList<>();
        list.add(-1);
        for (int n=0;n<formula.length;n++){
            if (formula[n].equals(",")){
                list.add(n);
            }
        }
        list.add(formula.length);

        if (list.size()==2){
            return calculate2(formula);
        }

        List result=new ArrayList();
        for (int n=0;n<list.size()-1;n++){
            String[] arr=new String[list.get(n+1)-list.get(n)-1];
            int first=list.get(n)+1;
            for (int n2=0;n2<arr.length;n2++){
                arr[n2]=formula[first];
                first++;
            }
            result.add(calculate2(arr).object);
        }

        return new Value(TYPE.JAVA_OBJECT,result);
    }

    private Value calculate2(String[] simple_formula) {

        BigDecimal value = new BigDecimal(0);

        String symbol_flag = "+";

        for (int n = 0; n < simple_formula.length; n++) {
            String string = simple_formula[n];

            if (symbol_flag != null) {

                if (string.matches("-?\\d+(\\.\\d+)?")) {

                    value = __calcu(value, new BigDecimal(string), symbol_flag);

                } else
                if (variable.containsKey(string)) {
                    Value va = variable.get(string);

                    switch (va.type) {
                        case TES_OBJECT:

                            if (((String) va.object).matches("-?\\d+(\\.\\d+)?")) {
                                value = __calcu(value, new BigDecimal((String) va.object), symbol_flag);
                            } else {
                                return va;
                            }

                            break;

                        case JAVA_OBJECT:
                            return va;

                        default:

                    }
                }else{
                    return new Value(TYPE.TES_OBJECT, string);
                }

                symbol_flag = null;
            } else {
                symbol_flag = string;
            }

        }

        return new Value(TYPE.TES_OBJECT, String.valueOf(value));
    }

    private BigDecimal __calcu(BigDecimal value, BigDecimal value2, String symbol) {
        return switch (symbol) {
            case "+" -> value.add(value2);
            case "-" -> value.subtract(value2);
            case "*" -> value.multiply(value2);
            case "/" -> value.divide(value2);
            default -> value;
        };
    }


    public Value arg = null;


    class VariablesMap extends HashMap<String, Value> {
        

        @Override
        public Value put(String key, Value value) {
            if (key.equals("arg")) {
                arg = value;
                return value;
            }

            return super.put(key, value);
        }

        @Override
        public Value get(Object key) {

            if (key.equals("arg")) {
                return arg;
            }

            return super.get(key);
        }

        @Override
        public boolean containsKey(Object key) {

            if (key.equals("arg"))
                return true;

            return super.containsKey(key);
        }
    }

    class DefineFunction {

        public DefineFunction(String name) {
            this.name = name;
        }

        final String name;
        final List<String> codes = new ArrayList<>();

        public void toValueFunction(VariablesMap variablesMap) {
            String code = "";

            for (String string : codes) {
                code += string + "\n";
            }

            variablesMap.put(name, new Value(TYPE.FUNCTION, code));
        }
    }

}

class Value {

    TYPE type;
    Object object;

    public Value(TYPE type, Object object) {
        this.type = type;
        this.object = object;
    }

    @Override
    public String toString() {
        return type.name() + " " + object;
    }

}

enum TYPE {

    TES_OBJECT,
    FUNCTION,
    JAVA_OBJECT

}
