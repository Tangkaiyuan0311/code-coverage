import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The branch tracker for a single class.<br>
 * Including the branches contained in a method and branches in a method.<br>
 * Branches are recognized as jump instructions.<br>
 * Each jump instruction are assigned a unique branch id.
 * @author kt27
 */
public class BranchTracker {
    public String className;

    // method -> all branches ID contained
    public Map<FirstPassResult.MethodSignature, Set<Integer>> method2branches;

    // method -> all branches ID covered
    public Map<FirstPassResult.MethodSignature, Set<Integer>> method2Coveredbranches;

    BranchTracker(String className) {
        this.className = className;
        method2branches = new HashMap<>();
        method2Coveredbranches = new HashMap<>();
    }

    /**
     * Register a new branch with method
     * @param   method  the method signature of which a new branch is registered
     * @return  the newly assigned branch id
     */
    public int createNewBranchId(FirstPassResult.MethodSignature method) {
        Set<Integer> branches = method2branches.get(method);
        if (branches == null) {
            // first branches in this method
            branches = new HashSet<>();
        }
        // now we are sure that branches is not null
        // insert a new sequential branch Id
        int newId = branches.size();
        branches.add(newId);

        method2branches.put(method, branches);
        return newId;
    }

    /**
     * Mark that branch id of method has been covered
     * @param   method  the method signature of which a branch is covered during execution
     * @param   id  the branch id that has been covered
     */
    public void coverBranch(FirstPassResult.MethodSignature method, int id) {
        Set<Integer> coveredbranches = method2Coveredbranches.get(method);
        if (coveredbranches == null) {
            // first branches in this method
            coveredbranches = new HashSet<>();
        }
        // now we are sure that branches is not null
        // add the covered branch id
        coveredbranches.add(id);
        method2Coveredbranches.put(method, coveredbranches);
    }

    /**
     * Out put the branch coverage summary
     * @param   method  the method signature of which branch coverage information is requested
     * @return  branch coverage summary
     */
    public String getBranchCoverage(FirstPassResult.MethodSignature method) {
        Set<Integer> branches = method2branches.get(method);
        Set<Integer> coveredbranches = method2Coveredbranches.get(method);
        if (branches == null)
            return "no branch contained";
        if (coveredbranches == null || coveredbranches.size() == 0)
            return "no branch coverage";
        double percent = coveredbranches.size() * 1.0 / branches.size();
        percent = Math.round(percent*100.0)/100.0;
        return percent + "(" + coveredbranches.size() + "/" + branches.size() + ")";
    }
}
