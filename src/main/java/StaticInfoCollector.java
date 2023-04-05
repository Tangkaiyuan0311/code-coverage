import org.objectweb.asm.*;


/**
 * Adapter between classReader and classWriter.<br>
 * Implement the core algorithm for first pass.
 * @author kt27
 */
public class StaticInfoCollector extends ClassVisitor implements Opcodes {

    private final FirstPassResult staticInfo;
    private final String className;

    /**
     * @param   cv  the class visitor to be encapsulated, it should be a class writer
     * @param   staticInfo  the first pass object to be written into
     */
    public StaticInfoCollector(ClassVisitor cv, FirstPassResult staticInfo, String name) {
        super(Opcodes.ASM5, cv);
        this.staticInfo = staticInfo;
        this.className = name;
    }


    /**
     * @return  Adapter for underlying initial method visitor of class writer.
     * Adapter: save line number info and forward call
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions); // initial access class
        return new LineRecorder(mv, staticInfo, name, desc);
    }

    /**
     * Encapsulate a methodVisitor of class writer.
     * Override the visitLineNumber method
     */
    private static class LineRecorder extends MethodVisitor implements Opcodes {
        private final FirstPassResult staticInfo; // all methods-lineNumbers of a class
        private final String methodName;
        private final String methodDesc;

        //
        public LineRecorder(MethodVisitor mv, FirstPassResult staticInfo, String methodName, String methodDesc) {
            super(ASM5, mv);
            this.staticInfo = staticInfo;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        /**
         * @param   line    the line number with which this method is invoked
         * Mark this line as visited in the FirstPass object before forwarding
         */
        @Override
        public void visitLineNumber(int line, Label start) {
            staticInfo.saveLineNumber(methodName, methodDesc, line); // save line number information
            mv.visitLineNumber(line, start); // forward the actual call
        }
    }
}
