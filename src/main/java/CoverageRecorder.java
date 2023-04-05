import org.objectweb.asm.*;

/**
 * Encapsulate the class writer.<br>
 * Used during second pass.
 * @author kt27
 */
public class CoverageRecorder extends ClassVisitor implements Opcodes {

    private final String className;

    public CoverageRecorder(ClassVisitor cv, String className) {
        super(Opcodes.ASM5, cv);
        this.className = className;
    }

    /**
     * @return return the adapter that encapsulate a MethodVisitor of class writer
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return new methodInjector(mv, name, desc);
    }

    /**
     * Encapsulate the method visitor of class writer.<br>
     * Override visitLineNumber method.
     */
    private class methodInjector extends MethodVisitor implements Opcodes {
        private final String methodName;
        private final String methodDesc;

        public methodInjector(MethodVisitor mv, String name, String desc) {
            super(ASM5, mv);
            this.methodName = name;
            this.methodDesc = desc;
        }

        /**
         * implement the core logic <br>
         * for each line number that invoke this method: <br>
         * put class name, method signature, line number onto the argument before <br>
         * inserting the method invocation of collect.<br>
         * At last, do normal forwarding
         */
        @Override
        public void visitLineNumber(int line, Label start) {
            mv.visitLdcInsn(className);
            mv.visitLdcInsn(methodName);
            mv.visitLdcInsn(methodDesc);
            mv.visitIntInsn(SIPUSH, line);
            // now argument stack is set up, insert the method call
            mv.visitMethodInsn(INVOKESTATIC, "mainDriver", "record", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", false);
            super.visitLineNumber(line, start);
        }
    }
}
