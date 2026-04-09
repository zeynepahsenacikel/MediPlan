#MediPlan — Intelligent Diagnostic Planner
MediPlan is a Java-based decision-support system designed to manage medical diagnostic tests, analyze patient burden, and optimize hospital costs. The application processes complex test dependencies using Dynamic Programming (DP) and Graph Theory to generate efficient medical execution plans.


##🚀 Key Features
The system operates through five distinct computational steps:

*XML Data Integration: Parses diagnostic catalogues and request files using standard Java XML libraries.

*Automated Cost Derivation: Computes processing costs for DERIVED tests based on the number of distinct physical sample types required.

*Patient Burden Analysis (Top-Down DP): Utilizes a memoized recursive approach to determine the exact number of physical procedures (e.g., blood draws, biopsies) a patient must undergo for a specific target test.

*Hospital Cost Analysis (Bottom-Up DP): Implements a tabulation-based DP and topological sorting to calculate the naive aggregated cost for multiple diagnostic targets.

*Diagnostic Plan Optimization: Generates a finalized execution order that eliminates redundant testing, providing both naive and optimized cost reports.


##📂 Project Structure
To maintain standard Java conventions, it is recommended to keep the source files in a src directory:

*src/Main.java: The entry point that coordinates the execution flow from parsing to final reporting.

*src/DiagnosticCatalogue.java: Manages the test registry, XML loading, and initial cost calculations.

*src/DiagnosticRequest.java: Handles the extraction of single and multiple test targets from request files.

*src/MediPlanDP.java: The core engine containing the logic for topological sorting, memoized recursion, and plan construction.


##🛠️ Getting Started
*Prerequisites
Java Development Kit (JDK) 8 or higher.

*Installation & Execution

Clone the repository:
```git clone https://github.com/yourusername/MediPlan.git```


Navigate to the source folder and compile:
```javac *. java -d .```


Run the application (providing the paths to your XML data files):
```java Main diagnostic_catalogue.xml diagnostic_requests.xml```


##📊 Logic & Methodology
*Dependency Management: The system treats tests as nodes in a Directed Acyclic Graph (DAG).

*Topological Sort: Used to ensure all dependency tests are processed before the tests that rely on them.

*Optimization: The final diagnostic plan distinguishes between "Naive Cost" (which may double-count shared sub-tests) and "Optimized Cost" (where each unique test is billed only once).