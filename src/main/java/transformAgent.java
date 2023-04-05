import java.lang.instrument.Instrumentation;

/**
 * Java agent startup<br>
 * Register and invoke the main driver
 * @author kt27
 */
public class transformAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        mainDriver transformer = new mainDriver(agentArgs); // should be the project name
        inst.addTransformer(transformer);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mainDriver.printResult();
            }
        });
    }

    public static void main(String[] args) {

    }
}