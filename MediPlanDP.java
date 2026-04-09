import java.util.*;

/**
 * MediPlanDP
 *
 * Contains all dynamic programming logic for MediPlan.
 *
 * You will implement the following methods in this class:
 *
 * Step 3 -- computeBurdenTopDown() and its helper computeSamples()
 * Top-down memoized DP. Computes the patient burden (number of distinct
 * non-NONE sample types) for a single target
 *
 * Step 4 -- computeCostBottomUp(), topologicalSort(), dfsFinish()
 * Bottom-up tabulation DP. Computes the naive hospital cost for
 * multiple target tests at once using a topological ordering.
 * COMPOSITE tests have processing_cost = 0, so their total cost is
 * purely the sum of their dependencies' total costs.
 *
 * collectReachable() -- graph utility used by Steps 4 and 5
 * Iterative DFS that collects all tests reachable from a given root.
 *
 * Step 5 -- buildDiagnosticPlan()
 * Traceback. Combines burdenMemo (Step 3) and costTable (Step 4)
 * into a structured diagnostic plan report. Also computes and returns the
 * optimized cost. Returns the optimized cost as an int.
 */
public class MediPlanDP {

    // =========================================================================
    // Fields -- do NOT change names or types
    // =========================================================================

    /**
     * The diagnostic catalogue that provides test objects and dependency structure.
     */
    private final DiagnosticCatalogue catalogue;

    /**
     * Memo table for the top-down burden DP (Step 3).
     *
     * Maps test ID -> set of distinct non-NONE sample types reachable from it.
     * Populated lazily during computeBurdenTopDown().
     */
    private final Map<String, Set<String>> burdenMemo = new LinkedHashMap<>();

    /**
     * Cost table for the bottom-up DP (Step 4).
     *
     * Maps test ID -> naive aggregated hospital cost to compute it.
     * Naive means shared subtrees are counted as they appear.
     * Populated eagerly in topological order by computeCostBottomUp().
     */
    private final Map<String, Integer> costTable = new LinkedHashMap<>();

    /**
     * Topological ordering of all tests reachable from any requested target.
     * Computed inside computeCostBottomUp() and stored here so that
     * buildDiagnosticPlan() can reuse it for the execution order.
     */
    private List<String> topoOrder = new ArrayList<>();

    // =========================================================================
    // Constructor -- do NOT change
    // =========================================================================

    public MediPlanDP(DiagnosticCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    // =========================================================================
    // Step 3 -- TODO: implement computeBurdenTopDown and computeSamples
    // =========================================================================

    /**
     * Entry point for Step 3.
     * Computes and prints the patient burden for the given single target.
     *
     * The patient burden = number of distinct non-NONE sample types in the
     * full dependency subtree of the target. It answers: how many different
     * physical procedures must this patient undergo?
     *
     * This method must print the header, the call trace (produced by the helper),
     * the summary line, and the footer.
     *
     * Expected output format -- see assignment specification for full detail.
     *
     * @param targetId the test ID requested as single_target
     */
    public void computeBurdenTopDown(String targetId) {
        System.out.println("##PATIENT BURDEN ANALYSIS (Top-Down DP)##");

        // TODO
        // Use computeSamples function
        // Use System.out.println("Patient burden for " + targetId + ": "....;

        // Recursively compute the sample set with memoization
        Set <String> samples = computeSamples(targetId, 0);
        System.out.println();
        System.out.println("Patient burden for " + targetId + ": " + samples.size() + " sample type(s): " + sortedSetString(samples));

        System.out.println("##PATIENT BURDEN ANALYSIS COMPLETED##");
        System.out.println();
    }

    /**
     * Recursive memoized helper for the top-down burden computation.
     *
     * Print each call as it is made (indented by depth * 2 spaces):
     * " Called computeSamples(<id>)"
     * If the result is memoized, append " -> MEMOIZED [...]" on the same line.
     * For a RAW test result, print the result on a new line at the same indent.
     * For a DERIVED/COMPOSITE result, print "<id> memoized -> [...]".
     *
     * See the assignment specification for the exact expected output.
     *
     * @param testId the test whose sample set we want
     * @param depth  current recursion depth (used only for indentation)
     * @return the set of distinct non-NONE sample types reachable from testId
     */
    private Set<String> computeSamples(String testId, int depth) {
        // TODO: implement the memoized recursive computation

        // Use:
        // System.out.print(indent + "Called computeSamples(" + testId + ")");
        // System.out.println(" -> MEMOIZED " + ...);
        // System.out.println(indent + testId + " -> {} (NONE, not counted)");
        // System.out.println(indent + testId + " -> " + ...);
        // System.out.println(indent + testId + " memoized -> " + ...);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth * 2 ; i++) {
            sb.append(" ");
        }
        String indent = sb.toString();

        // print the call -> before checking memo -> always print it
        System.out.print(indent + "Called computeSamples(" + testId + ")");

        // first check memo table
        if (burdenMemo.containsKey(testId)) {
            System.out.println(" -> MEMOIZED " + sortedSetString(burdenMemo.get(testId)));
            return burdenMemo.get(testId);
        }

        System.out.println();

        DiagnosticCatalogue.Test t = catalogue.getTest((testId));

        Set<String> result;

        if(t.isRaw()) {
            //base case: raw test
            if ("NONE".equalsIgnoreCase(t.sampleType)) {
                result = new LinkedHashSet<>();
                System.out.println(indent + testId + " -> {} (NONE, not counted)");
            } else {
                result = new LinkedHashSet<>();
                result.add(t.sampleType);
                System.out.println(indent + testId + " -> " + sortedSetString(result));
            }

        }else {
            //derived or composite -> recurse into inputs and union results
            result = new LinkedHashSet<>();

            for (String inputId : t.inputs) {
                Set<String> childSamples = computeSamples(inputId, depth + 1);
                result.addAll((childSamples));
            }
            System.out.println(indent + testId + " memoized -> " + sortedSetString(result));
        }

        //store in memo table
        burdenMemo.put(testId, result);
        return result;
    }

    // =========================================================================
    // Step 4 -- TODO: implement computeCostBottomUp, topologicalSort, dfsFinish
    // =========================================================================

    /**
     * Entry point for Step 4.
     * Computes the naive aggregated hospital cost for each target using
     * bottom-up tabulation DP.
     *
     * Algorithm:
     * 1. Collect all tests reachable from any target (use collectReachable()).
     * 2. Derive a topological ordering of those tests (call topologicalSort()).
     * 3. Print the topological order.
     * 4. Fill costTable in topological order using the recurrence:
     *
     * Expected output format -- see assignment specification for full detail.
     *
     * @param targetIds list of test IDs from the all_targets request field
     */
    public void computeCostBottomUp(List<String> targetIds) {
        System.out.println("##HOSPITAL COST ANALYSIS (Bottom-Up DP)##");

        // TODO: implement Step 4
        //
        // Use:
        // System.out.println("Topological order: " + String.join(", ", topoOrder));
        // System.out.printf("Computing %-25s collection_cost = %-6d total: %d%n",testId
        // + ":"...
        // System.out.printf("Computing %-25s processing_cost = %-15s total:
        // %d%n",testId + ":"...
        // COMPOSITE: no processing cost, show just dependency sum
        // System.out.printf("Computing %-25s cost = %-15s total: %d%n",testId + ":"....
        // System.out.println("Results:");
        // System.out.printf(" %-28s naive hospital cost: %d%n",...

        //step1: collects all tests reachable from any of the targets
        Set<String> reachable = new LinkedHashSet<>();
        for (String targetId : targetIds) {
            collectReachable(targetId, reachable);
        }

        //step2: derive topological ordering via DFS finish times
        topoOrder = topologicalSort(reachable);
        System.out.println("Topological order: " + String.join(", ", topoOrder));
        System.out.println();

        //step3: fill costTable in topological order
        for (String testId : topoOrder) {
            DiagnosticCatalogue.Test t = catalogue.getTest(testId);

            if (t.isRaw()) {
                //cost = collection_cost
                costTable.put(testId, t.cost);
                System.out.printf("Computing %-25s collection_cost = %-6d total: %d%n",testId + ":", t.cost, t.cost);

            } else if (t.isDerived()) {
                //derived -> cost: processing_cost + sum of all inputs' total costs
                int depSum = 0;
                StringBuilder depStr = new StringBuilder();
                for (String inputId : t.inputs) {
                    int inputCost = costTable.getOrDefault(inputId, 0);
                    depSum += inputCost;
                    if (depStr.length() > 0) depStr.append(" + ");
                    depStr.append(inputCost);
                }

                int total = t.cost + depSum;
                costTable.put(testId, total);

                String formula = t.cost + (depStr.length() > 0 ? " + " + depStr : "");
                System.out.printf("Computing %-25s processing_cost = %-15s total: %d%n",testId + ":", formula, total);

            } else {
                //composite -> cost: sum of all inputs' total costs
                int depSum = 0;
                StringBuilder depStr = new StringBuilder();
                for (String inputId : t.inputs) {
                    int inputCost = costTable.getOrDefault(inputId, 0);
                    depSum += inputCost;
                    if (depStr.length() > 0) depStr.append(" + ");
                    depStr.append(inputCost);
                }
                costTable.put(testId, depSum);

                System.out.printf("Computing %-25s cost = %-15s total: %d%n",testId + ":", depStr.toString(), depSum);
            }
        }

        //step4: print the results for each target
        System.out.println();
        System.out.println("Results:");

        for (String targetId : targetIds) {
            int naiveCost = costTable.getOrDefault(targetId, 0);
            System.out.printf("  %-28s naive hospital cost: %d%n", targetId, naiveCost);
        } 

        System.out.println("##HOSPITAL COST ANALYSIS COMPLETED##");
        System.out.println();

    }

    /**
     * Derives a topological ordering of the given set of test IDs using
     * DFS finish times on the dependency graph.
     *
     * How it works:
     * Run DFS on the dependency graph. When DFS finishes processing a node
     * (all nodes reachable from it have been visited), record its finish.
     * The correct topological order is: nodes that finish first appear first.
     * Dependencies always finish before the tests that depend on them.
     *
     * Only tests in the reachable set should appear in the output.
     *
     * @param reachable the set of test IDs to sort
     * @return list of test IDs in topological order (dependencies first)
     */
    public List<String> topologicalSort(Set<String> reachable) {
        // TODO: implement topological sort via DFS

        Set<String> visited = new LinkedHashSet<>();
        Deque<String> finishStack = new ArrayDeque<>();

        //run dfs from every node in the reachable set
        for (String testId : reachable) {
            if (!visited.contains(testId)) {
                dfsFinish(testId, reachable, visited, finishStack);
            }
        }

        //finishStack has nodes in reverse finish order
        List<String> order = new ArrayList<>();
        while (!finishStack.isEmpty()) {
            order.add(finishStack.pop());
        }

        //reverse so that dependencies come first
        Collections.reverse(order);
        return order;

    }

    /**
     * DFS helper for topological sort.
     * Visits testId and all its unvisited dependencies within reachable,
     * then pushes testId onto finishStack after all descendants are done.
     *
     * @param testId      the current node being visited
     * @param reachable   only follow edges to nodes within this set
     * @param visited     nodes whose full DFS subtree has been explored
     * @param finishStack accumulates nodes in reverse finish order
     */
    private void dfsFinish(String testId, Set<String> reachable,
            Set<String> visited, Deque<String> finishStack) {
        // TODO: implement DFS
        visited.add(testId);

        DiagnosticCatalogue.Test t = catalogue.getTest(testId);
        if (t != null) {
            for (String inputId : t.inputs) {
                if (reachable.contains(inputId) && !visited.contains(inputId)) { //only follow edges within the reachable set
                    dfsFinish(inputId, reachable, visited, finishStack);
                }
            }
        }

        //push after all descendants have been processed
        finishStack.push(testId);
    }

    // =========================================================================
    // TODO: implement collectReachable
    // =========================================================================

    /**
     * Collects all test IDs reachable from startId (including startId itself)
     * by following the dependency graph, using iterative DFS.
     *
     * Used by computeCostBottomUp() to find all tests that need to be costed,
     * and by buildDiagnosticPlan() to filter the execution order.
     *
     * @param startId   the root test to start from
     * @param reachable the set to populate with reachable test IDs
     */
    private void collectReachable(String startId, Set<String> reachable) {
        // TODO: implement iterative DFS reachability collection
        Deque<String> stack = new ArrayDeque<>();
        stack.push(startId);

        while (!stack.isEmpty()) {
            String current = stack.pop();

            //skip if already visited
            if (reachable.contains(current)) {
                continue;
            }

            reachable.add(current);

            DiagnosticCatalogue.Test t = catalogue.getTest(current);

            if (t != null) { //push all dependencies onto the stack
                for (String inputId : t.inputs) {
                    if (!reachable.contains(inputId)) {
                        stack.push(inputId);
                    }
                }
            }
        }
    }

    // =========================================================================
    // Step 5 -- TODO: implement buildDiagnosticPlan
    // =========================================================================

    /**
     * Entry point for Step 5.
     * Produces a combined diagnostic plan for the single target by combining
     * results from Step 3 (burdenMemo) and Step 4 (costTable, topoOrder).
     *
     * The plan must show:
     * - Target header: ID and name
     * - Patient burden: number of sample types and procedure list
     * - Execution order: tests in topological order filtered to this
     * target's reachable set, annotated with type and added cost
     * (t.cost -- the per-test processing cost, not the accumulated total)
     * - RAW tests that require a collection procedure are marked with
     * "<- SAMPLE_TYPE sample"
     * - Naive hospital cost from costTable (Step 4 result, may double-count
     * sharing)
     * - Optimized hospital cost: flat sum of added costs, each test counted once
     *
     * Expected output format -- see assignment specification for full detail.
     *
     * @param targetId the single target for which to build the plan
     * @return the optimized hospital cost (flat sum of cost across execution plan)
     */
    public int buildDiagnosticPlan(String targetId) {
        System.out.println("##DIAGNOSTIC PLAN##");

        // TODO: implement Step 5
        //
        // Use:
        // System.out.println("Target: " + targetId + " (" + target.name + ")");
        // System.out.println("Patient burden: " + ...+ " sample type(s) required");
        // System.out.println(" Procedures: " + ...));
        // System.out.println("Execution order (hospital cost optimized):");
        /**
         * String annotation = "";
         * if (t.isRaw() && !"NONE".equalsIgnoreCase(sampleType)) {
         * annotation = " <- " + sampleType + " sample";
         * }
         * System.out.printf(" [%2d] %-28s %-12s cost: %2d%s%n",
         * step++, id, "[" + type + "]", cost, annotation);
         */
        // System.out.println("Naive hospital cost: " + ...);
        // System.out.println("Optimized hospital cost: " + ...);

        DiagnosticCatalogue.Test target = catalogue.getTest(targetId);

        //header: target id and name
        System.out.println("Target: " + targetId + " (" + target.name + ")");
        System.out.println();

        //patient burden for burdenMemo
        Set<String> sampleSet = burdenMemo.getOrDefault(targetId, new LinkedHashSet<>());
        System.out.println("Patient burden:  " + sampleSet.size() + " sample type(s) required");

        //build the procedure list
        List<String> procedureLabels = new ArrayList<>();
        List<String> sortedSamples = new ArrayList<>(sampleSet);
        Collections.sort(sortedSamples);

        for (String s : sortedSamples) {
            procedureLabels.add(procedureLabel(s));
        }

        System.out.println("  Procedures:    " + String.join(", ", procedureLabels));
        System.out.println();

        // execution order
        List<String> execOrder = getExecutionOrder(targetId);

        System.out.println("Execution order (hospital cost optimized):");

        int step = 1;
        int optimizedCost = 0;

        for (String id : execOrder) {
            DiagnosticCatalogue.Test t = catalogue.getTest(id);
            String type = t.type;
            int cost = t.cost; //per test cost -> not accumulated total

            String annotation = "";
            if (t.isRaw() && !"NONE".equalsIgnoreCase(t.sampleType)) {
                annotation = "  <- " + t.sampleType + " sample";
            }

            System.out.printf("  [%2d]  %-28s %-12s added cost: %2d%s%n", step++, id, "[" + type + "]", cost, annotation);

            optimizedCost += cost;
        }

        //naive cost from costTable
        int naiveCost = costTable.getOrDefault(targetId, 0);
        System.out.println();
        System.out.println("Naive hospital cost:     " + naiveCost);

        System.out.println("Optimized hospital cost: " + optimizedCost);

        System.out.println("##DIAGNOSTIC PLAN COMPLETED##");
        System.out.println();

        return optimizedCost;
    }

    // =========================================================================
    // Provided helper -- do NOT modify
    // =========================================================================

    /**
     * Returns the execution order for the given target as a List of test IDs.
     *
     * The list contains every test reachable from targetId, in topological
     * order -- all dependencies of a test appear before the test itself.
     *
     * PROVIDED IN FULL -- used by the autograder to verify Step 5.
     * Must be called after computeCostBottomUp() so that topoOrder is populated.
     * Do NOT modify.
     *
     * @param targetId the target test for which to return the execution order
     * @return list of test IDs in topological order, filtered to reachable set
     */
    public List<String> getExecutionOrder(String targetId) {
        Set<String> reachable = new LinkedHashSet<>();
        collectReachable(targetId, reachable);
        List<String> execOrder = new ArrayList<>();
        for (String id : topoOrder) {
            if (reachable.contains(id))
                execOrder.add(id);
        }
        return execOrder;
    }

    // =========================================================================
    // Utility -- provided in full, use freely
    // =========================================================================

    private String procedureLabel(String sampleType) {
        switch (sampleType.toUpperCase()) {
            case "BLOOD":
                return "BLOOD draw";
            case "URINE":
                return "URINE sample";
            case "TISSUE":
                return "TISSUE biopsy";
            default:
                return sampleType;
        }
    }

    /**
     * Returns a sorted, bracket-enclosed string representation of a set.
     * Example: {"URINE", "BLOOD"} -> "[BLOOD, URINE]"
     *
     * PROVIDED IN FULL. Use whenever you need to print a sample set.
     */
    protected String sortedSetString(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return list.toString();
    }
}
