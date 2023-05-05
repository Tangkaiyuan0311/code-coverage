import org.objectweb.asm.*;

/**
 * Encapsulate the class writer.<br>
 * Used during second pass.
 * @author kt27
 */
public class CoverageRecorder extends ClassVisitor implements Opcodes {

    private final String className;
    public BranchTracker branchTracker;

    public CoverageRecorder(ClassVisitor cv, String className, BranchTracker branchTracker) {
        super(Opcodes.ASM5, cv);
        this.className = className;
        this.branchTracker = branchTracker;
    }

    /**
     * @return return the adapter that encapsulate a MethodVisitor of class writer
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return new methodInjector(mv, name, desc, branchTracker);
    }

    /**
     * Encapsulate the method visitor of class writer.<br>
     * Override visitLineNumber method.
     */
    private class methodInjector extends MethodVisitor implements Opcodes {
        private final String methodName;
        private final String methodDesc;
        public BranchTracker branchTracker;

        public methodInjector(MethodVisitor mv, String name, String desc, BranchTracker branchTracker) {
            super(ASM5, mv);
            this.methodName = name;
            this.methodDesc = desc;
            this.branchTracker = branchTracker;
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

        /**
         * implement the core logic of branch coverage
         * for each valid jump instruction that invoke this method:<br>
         * register current branch with the given global branch tracker, <br>
         * put class name, method signature, assigned branch id onto the argument stack before <br>
         * inserting the method invocation of recordBranch
         */
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR) { // do not consider the two kind of jump instructions
                // record the existence of current branch
                FirstPassResult.MethodSignature method = new FirstPassResult.MethodSignature(methodName, methodDesc);
                int branchId = branchTracker.createNewBranchId(method);
                // Insert instructions to track branch execution
                mv.visitLdcInsn(className);
                mv.visitLdcInsn(methodName);
                mv.visitLdcInsn(methodDesc);
                mv.visitIntInsn(SIPUSH, branchId); // set up argument
                mv.visitMethodInsn(INVOKESTATIC, "mainDriver", "recordBranch", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V", false);
            }
            mv.visitJumpInsn(opcode, label);
        }
    }
}
