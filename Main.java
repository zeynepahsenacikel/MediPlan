import java.util.List;

/**
 * MediPlan — Intelligent Diagnostic Planner
 *
 * Entry point for the application.
 * Reads command-line arguments and coordinates all steps in order.
 *
 * Usage:
 *   java Main <diagnostic_catalogue.xml> <diagnostic_requests.xml>
 *
 * You may add private helper methods if needed,
 * but do NOT change the main method signature.
 */
public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Usage: java Main <diagnostic_catalogue.xml> <diagnostic_requests.xml>");
            System.exit(1);
        }

        String cataloguePath = args[0];
        String requestsPath  = args[1];

        // ── Step 1: Parse XML and build dependency graph ────────────────────
        DiagnosticCatalogue catalogue = new DiagnosticCatalogue();
        catalogue.loadFromXML(cataloguePath);

        // ── Step 2: Compute DERIVED test costs ──────────────────────
        catalogue.computeDerivedCosts();

        // ── Load requests ────────────────────────────────────────────────────
        DiagnosticRequest request = DiagnosticRequest.loadFromXML(requestsPath);
        String       singleTarget = request.getSingleTarget();
        List<String> allTargets   = request.getAllTargets();

        // ── Step 3: Top-down memoized DP — patient burden ────────────────────
        MediPlanDP planner = new MediPlanDP(catalogue);
        planner.computeBurdenTopDown(singleTarget);

        // ── Step 4: Bottom-up tabulation DP — naive hospital cost ──────────────────
        planner.computeCostBottomUp(allTargets);

        // ── Step 5: Traceback — combined diagnostic plan & optimized cost ─────────────────────
        planner.buildDiagnosticPlan(singleTarget);
    }
}
