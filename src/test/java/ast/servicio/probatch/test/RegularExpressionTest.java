package ast.servicio.probatch.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularExpressionTest {
    public static void main(String[] args) {
        String s = "eternalRecursiveSleeper.sh: line 24: $PROCESSOR_LEVEL    $ASD  $PYTHON_HOME";
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        Matcher matcher = pattern.matcher(s);
        matcher.find();
        String group = matcher.group(1);
        System.out.println(group);
        matcher.find();
        group = matcher.group(1);
        System.out.println(group);

        System.out.println(resuelveVariablesDeSistema(s));
    }

    static String resuelveVariablesDeSistema(String mensaje) {
        Pattern pattern = Pattern.compile("\\$(\\w+)");
        Matcher matcher = pattern.matcher(mensaje);

        String outMsg = mensaje;
        while (matcher.find()) {
            if (matcher.groupCount() >= 1) {
                String fullVarName = matcher.group(0);
                String varName = matcher.group(1);
                String varValue = System.getenv(varName);

                if (varValue != null && !"".equals(varValue)) {
                    outMsg = outMsg.replaceAll(Pattern.quote(fullVarName), varValue);
                }
            }
        }

        return outMsg;
    }

    private static void hyperion() {
        String s = "Hyperion SQR Server - 8.5.0.0.0.566";
        String expr = ".*Hyperion.*";
        System.out.println("testing " + expr + " on " + s);
        String msg = s.matches(expr) ? "matches!" : "no match!";
        System.out.println(msg);
    }
}
