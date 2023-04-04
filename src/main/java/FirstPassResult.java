import java.util.*;

/**
 * Collect first-pass information for a single class.
 * <br>
 * Core data structure: method signature->line numbers
 * @author kt27
 */
class FirstPassResult {
    /**
     * represent a complete method signature: name+desc
     */
    public static class MethodSignature {
        private final String methodName;
        private final String desc;
        public MethodSignature(String methodName, String desc) {
            this.methodName = methodName;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodSignature that = (MethodSignature) o;

            if (!methodName.equals(that.methodName)) return false;
            return desc.equals(that.desc);

        }

        @Override
        public int hashCode() {
            int result = methodName.hashCode();
            result = 31 * result + desc.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return methodName+" "+desc;
        }
    }

    /**
     * mapping from a method representation to associated source code line numbers
     */
    final Map<MethodSignature, SortedSet<Integer>> method2lines;
    private final String className;


    /**
     * initialize an empty structure w.r.t input class name
     */
    public FirstPassResult(String className) {
        this.method2lines = new HashMap<>();
        this.className = className;
    }

    /**
     * associate a line number to a method signature
     * @param   methodName  the method name this line number to be associated with
     * @param   methodDesc  the method description this line number to be associated with
     * @param   line    the line number
     */
    public void saveLineNumber(String methodName, String methodDesc, int line) {
        MethodSignature m = new MethodSignature(methodName, methodDesc);
        if(method2lines.containsKey(m)){
            SortedSet<Integer> lines = method2lines.get(m);
            lines.add(line);
        } else {
            SortedSet<Integer> lines = new TreeSet<>();
            lines.add(line);
            method2lines.put(m, lines);
        }
    }

    /**
     * @return total number of (covered/contained) methods in this class
     */
    public int numOfMethods() {
        return method2lines.size();
    }

    /**
     * @return total number of (covered/contained) statements in this class
     */
    public int numOfStatements() {
        int sum = 0;
        for (var entry : method2lines.entrySet()) {
            sum += entry.getValue().size();
        }
        return sum;
    }

    /**
     * for each method: print a list of associate line numbers:<br>
     * className[method[lineNumbers]]
     */
    @Override
    public String toString() {
        Iterator<Map.Entry<MethodSignature, SortedSet<Integer>>> method2lines_itr = method2lines.entrySet().iterator();
        StringBuilder sb0 = new StringBuilder(); // root string builder
        sb0.append(className+System.getProperty("line.separator")); // print class name

        while(method2lines_itr.hasNext()){
            Map.Entry<MethodSignature, SortedSet<Integer>> thisMethod2lines = method2lines_itr.next();
            StringBuilder sb = new StringBuilder(thisMethod2lines.getKey()+" : "); // print method signature
            for (int line : thisMethod2lines.getValue()) {
                sb.append(line+" "); // print line numbers
            }
            sb0.append(sb+System.getProperty("line.separator"));
        }
        return sb0.toString();
    }
}