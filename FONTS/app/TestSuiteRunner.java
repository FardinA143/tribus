package app;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Runner simplificat que encapsula {@code org.junit.runner.JUnitCore} mitjançant
 * reflexió per reduir el soroll del {@code TextListener} predeterminat.
 */
public final class TestSuiteRunner {
    private TestSuiteRunner() {
    }

    /**
     * Executa les classes de test indicades per argument mitjançant JUnitCore.
     *
     * @param args noms complets de les classes de test.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No s'han proporcionat classes de test.");
            System.exit(1);
        }
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            List<Class<?>> tests = new ArrayList<>();
            for (String className : args) {
                tests.add(Class.forName(className, false, loader));
            }
            Object result = runTests(tests.toArray(new Class<?>[0]));
            printSummary(result);
            if (!wasSuccessful(result)) {
                System.exit(1);
            }
        } catch (ReflectiveOperationException ex) {
            System.err.println("Error en executar els tests: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static Object runTests(Class<?>[] testClasses) throws ReflectiveOperationException {
        Class<?> junitCoreClass = Class.forName("org.junit.runner.JUnitCore");
        Object junitCore = junitCoreClass.getDeclaredConstructor().newInstance();
        Method runMethod = junitCoreClass.getMethod("run", Class[].class);
        return runMethod.invoke(junitCore, new Object[]{testClasses});
    }

    private static void printSummary(Object result) throws ReflectiveOperationException {
        int runCount = (Integer) invoke(result, "getRunCount");
        int ignoreCount = (Integer) invoke(result, "getIgnoreCount");
        int failureCount = (Integer) invoke(result, "getFailureCount");
        long runTime = (Long) invoke(result, "getRunTime");
        System.out.println("Tests executats: " + runCount);
        System.out.println("Ignorats: " + ignoreCount);
        System.out.println("Fallades: " + failureCount);
        System.out.printf("Temps: %.3f s%n", runTime / 1000.0);
        List<?> failures = (List<?>) invoke(result, "getFailures");
        if (!failures.isEmpty()) {
            System.out.println("Detall de fallades:");
            for (Object failure : failures) {
                System.out.println(" - " + invoke(failure, "getTestHeader"));
                Object message = invoke(failure, "getMessage");
                if (message != null) {
                    System.out.println("   " + message);
                }
            }
        } else {
            System.out.println("Resultat: OK");
        }
    }

    private static boolean wasSuccessful(Object result) throws ReflectiveOperationException {
        return (Boolean) invoke(result, "wasSuccessful");
    }

    private static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
