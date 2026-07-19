import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Main {

     public static void main(String[] args) {
        
        TestScript tes=new TestScript();
    
        tes.run("""

            fun3 = func

            arg = 3

            println ( arg )

            endfunc

            fun2 = func

            arg = 2

            fun3 ( )

            println ( arg )

            endfunc


            fun = func

            arg = 1

            fun2 ( )

            println ( - )

            println ( arg )

            endfunc



            println ( 1 )

            fun ( )

                """);

        if(true)return;

        tes.run("""


            fun = func

            key = tes newTes object arg key a

            res = getTesValue ( key )

            println ( res )

            endfunc


            obj = tes newTes a 0 b 1


            fun ( obj )


                """);  


    }

}
