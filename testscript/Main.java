import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
     public static void main(String[] args) {
         TestScript testScript=new TestScript();

         if (args.length == 1 && new File(args[0]).exists()){
             try {
                 List<String> list=Files.readAllLines(Path.of(args[0]));
                 testScript.run(list.toArray(new String[list.size()]));
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }else {
             Scanner scanner = new Scanner(System.in);

             while (true){
                 System.out.print(">>");
                 testScript.run(scanner.next());
             }
         }
    }

}
