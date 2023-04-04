import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * the main class monitoring the coverage information <br>
 * implement the two pass algorithm and the coverage collecting trigger
 * @author kt27
 */
public class mainDriver implements ClassFileTransformer {


    /**
     * Mapping from className to printable coverage information.<br>
     * The main data structure that is updated by collecting trigger
     */
    private static final Map<String, coverageInfo> class2coverage = new HashMap<>();

    /**
     * the trigger invoke for each line executed.<br>
     * imply that a line has been covered
     * @param   line    the line number that trigger this method
     */
    public static void record(String className, String methodName, String mDesc, int line) {
        class2coverage.get(className).lineCovered.saveLineNumber(methodName, mDesc, line);
    }

    public static void printResult() {
        for (Map.Entry<String, coverageInfo> classCoverageInfo : class2coverage.entrySet()) {
            System.out.println("class name: " + classCoverageInfo.getKey());
            System.out.println("Method coverage: " + classCoverageInfo.getValue().getMethodCoverage());
            System.out.println("Statement coverage: " + classCoverageInfo.getValue().getStatementCoverage());
            coverageInfo coverage = classCoverageInfo.getValue();
            for (FirstPassResult.MethodSignature methodSig: coverage.lineContained.method2lines.keySet()) {
                System.out.println("************************************************");
                System.out.println("method: " + methodSig.toString());
                SortedSet<Integer> linesContained = coverage.lineContained.method2lines.get(methodSig);
                SortedSet<Integer> linesCovered = coverage.lineCovered.method2lines.get(methodSig);
                System.out.println("statements contained: " + linesContained);
                System.out.println("statements covered: " + linesCovered);

            }
            System.out.println("---------------------------------------------------");
            System.out.println("---------------------------------------------------");
            System.out.println();
        }
    }


    private final String projectName;


    public mainDriver(String projectName) {
        this.projectName = projectName;
    }

    /**
     * driver for class file pre-processing<br>
     * For each non-test class file, apply two-pass transformation on it
     * @param   className   class name, only non-test file get selected and transformed
     * @param   classfileBuffer class file bytecode
     */
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {

        if (className.contains(projectName) && !className.contains("/test/") && !className.contains("Test")) { // do not coverage junit class
            FirstPassResult firstPassInfo = new FirstPassResult(className);
            // record method->lines into FirstPassInfo for className
            byte[] output = passOne(classfileBuffer, firstPassInfo, className);
            // setup with empty second pass information
            class2coverage.put(className, new coverageInfo(firstPassInfo, new SecondPassResult(className)));

            output = passTwo(output, className); // add several bytecodes
            // for each method, add:
            // an invocation of collect method for each line of code
            // when the method is invoked: we can know that line has been covered

            return output;
        } else return classfileBuffer;
    }

    /**
     * @param   classByte   input class bytecode
     * @param   firstPassInfo   FirstPass object include static method-lines association <br>
     * stream class ByteCode from input to output, only line number info is recorded into first-pass object:<br>
     * 1. classReader invoke StaticInfoCollector.visitMethod for each method, get an adapter(lineRecorder) for underlying classWriter methodVisitor.<br>
     * 2. classReader invoke corresponding method of lineRecorder on each instruction element of the method.<br>
     * 3. for each invocation of lineRecorder.visitLineNumber, the method-lineNumber association is added to first-pass object<br>
     * 4. normal forwarding to the underlying classWriter
     */
    private byte[] passOne(byte[] classByte, FirstPassResult firstPassInfo, String name) {
        byte[] outPutByte;
        ClassReader reader = new ClassReader(classByte);
        ClassWriter writer = new ClassWriter(reader, 0);
        StaticInfoCollector adapter = new StaticInfoCollector(writer, firstPassInfo, name);
        reader.accept(adapter, 0);
        outPutByte = writer.toByteArray();
        return outPutByte;
    }


    /**
     * @param    name    class name
     * 1. classReader invoke CoverageRecorder.visitMethod for each method, get a adapter encapsulating classWriter methodVisitor<br>
     * 2. classReader invoke corresponding method of the adapter on each instruction element of the method<br>
     * 3. for each invocation of adapter's visitLineNumber:<br>
     * forward calls to classWriter to insert a method invocation of mainDriver.collect before a plain delegation
     */
    private byte[] passTwo(byte[] classByte, String name) {
        byte[] output;
        ClassReader reader = new ClassReader(classByte);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        CoverageRecorder adapter = new CoverageRecorder(writer, name);
        reader.accept(adapter, 0);
        output = writer.toByteArray();
        return output;
    }


}


