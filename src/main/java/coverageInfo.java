
/**
 * The overall two-part coverage information for a single class
 * First part: static line number association for each class: method.
 * Second part: line number coverage for each class: method
 * @author kt27
 */
public class coverageInfo {
    public FirstPassResult lineContained; // line numbers of methods
    public SecondPassResult lineCovered; // covered line numbers of methods

    public coverageInfo(FirstPassResult lineContained, SecondPassResult lineCovered) {
        this.lineContained = lineContained;
        this.lineCovered = lineCovered;
    }

    public String getMethodCoverage() {
        if (lineContained.numOfMethods() == 0)
            return "no coverage";
        double percent = lineCovered.numOfMethods() * 1.0 / lineContained.numOfMethods();
        percent = Math.round(percent*100.0)/100.0;
        return percent + "(" + lineCovered.numOfMethods() + "/" + lineContained.numOfMethods() + ")";
    }
    public String getStatementCoverage() {
        if (lineContained.numOfStatements() == 0)
            return "no coverage";
        double percent =  lineCovered.numOfStatements() * 1.0 / lineContained.numOfStatements();
        percent = Math.round(percent*100.0)/100.0;
        return percent + "(" + lineCovered.numOfStatements() + "/" + lineContained.numOfStatements() + ")";
    }

}
