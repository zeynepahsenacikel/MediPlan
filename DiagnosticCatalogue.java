import javax.swing.text.html.parser.Element;
import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

/**
 * DiagnosticCatalogue
 *
 * Responsible for:
 *   Step 1 -- loadFromXML()
 *             Parses diagnostic_catalogue.xml and builds the test map.
 *
 *   Step 2 -- computeDerivedCosts()
 *             Computes processing_cost for DERIVED tests.
 *             DERIVED tests depend exclusively on RAW tests.
 *             COMPOSITE tests always have no processing_cost.
 */
public class DiagnosticCatalogue {

    // =========================================================================
    // Inner class -- do NOT change field names or types
    // =========================================================================
    /**
     * Represents a single diagnostic test in the catalogue.
     *
     * RAW tests:
     *   - sampleType is one of: BLOOD, URINE, TISSUE, NONE
     *   - cost is the collection_cost read from XML
     *   - inputs is empty
     *
     * DERIVED tests:
     *   - sampleType is null (not applicable)
     *   - cost is computed by computeDerivedCosts() in Step 2
     *   - inputs contains only RAW test IDs
     *
     * COMPOSITE tests:
     *   - sampleType is null (not applicable)
     *   - initial cost is 0 (pure aggregation, no processing cost of its own)
     *   - inputs contains only DERIVED or COMPOSITE test IDs
     */
    public static class Test {

        public String       id;
        public String       name;
        public String       type;        // "RAW", "DERIVED", or "COMPOSITE"
        public String       sampleType;  // meaningful for RAW only
        public int          cost;        // collection_cost (RAW) or processing_cost (DERIVED) or 0 (COMPOSITE)
        public List<String> inputs;      // direct dependency IDs (empty for RAW)

        public Test() {
            inputs = new ArrayList<>();
        }

        /** Returns true if this test is of type RAW. */
        public boolean isRaw() {
            return "RAW".equalsIgnoreCase(type);
        }

        /** Returns true if this test is of type DERIVED. */
        public boolean isDerived() {
            return "DERIVED".equalsIgnoreCase(type);
        }

        /** Returns true if this test is of type COMPOSITE. */
        public boolean isComposite() {
            return "COMPOSITE".equalsIgnoreCase(type);
        }

        @Override
        public String toString() {
            return id + " [" + type + ", cost=" + cost + "]";
        }
    }

    // =========================================================================
    // Fields -- do NOT change names or types
    // =========================================================================
    /**
     * All tests in the catalogue, keyed by test ID.
     * Populated by loadFromXML(). LinkedHashMap preserves XML insertion order
     * which keeps output deterministic.
     */
    private Map<String, Test> tests = new LinkedHashMap<>();

    // =========================================================================
    // Step 1 -- TODO: implement this method
    // =========================================================================
    /**
     * Parses diagnostic_catalogue.xml and populates the tests map.
     * Uses standard Java javax.xml.parsers -- no external libraries.
     *
     * XML format:
     *   RAW:      <test id=".." name=".." type="RAW"
     *                   sample_type=".." collection_cost=".."/>
     *   DERIVED:  <test id=".." name=".." type="DERIVED">
     *               <input ref=".."/>  (only RAW refs)
     *             </test>
     *   COMPOSITE:<test id=".." name=".." type="COMPOSITE">
     *               <input ref=".."/>  (DERIVED or COMPOSITE refs)
     *             </test>
     *
     * @param filePath path to diagnostic_catalogue.xml
     */
    public void loadFromXML(String filePath) {
        // TODO: implement Step 1
        try {
            //set up the xml parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            //hepsini nodeListe ekle
            //get all test elements
            NodeList testNodes = doc.getElementsByTagName("test");

            for (int i = 0; i < testNodes.getLength(); i++) {
                //Element elem = (Element) testNodes.item(i);
                org.w3c.dom.Element elem = (org.w3c.dom.Element) testNodes.item(i);

                Test t = new Test();
                t.id = elem.getAttribute("id");
                t.name = elem.getAttribute("name");
                t.type = elem.getAttribute("type");

                if (t.isRaw()) {
                    //raw -> read sample_type and collection_cost from attributes
                    t.sampleType = elem.getAttribute("sample_type");
                    t.cost = Integer.parseInt(elem.getAttribute("collection_cost"));

                } else {
                    //derived or composite -> read <input ref="..."> child elements
                    t.sampleType = null;
                    t.cost = 0; //will be set in step2 for derived

                    NodeList inputNodes = elem.getElementsByTagName("input");
                    for (int j = 0; j < inputNodes.getLength(); j++) {
                        //Element inputElem = (Element) inputNodes.item(j);
                        org.w3c.dom.Element inputElem = (org.w3c.dom.Element) inputNodes.item(j);
                        t.inputs.add(inputElem.getAttribute("ref"));
                    }
                }

                tests.put(t.id, t);
            }
        } catch (Exception e) {
            System.err.println("Error parsing catalogue XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Step 2 -- TODO: implement this method
    // =========================================================================
    /**
     * Computes and assigns processing_cost for every DERIVED test.
     *
     * Cost rule:
     *   DERIVED tests depend only on RAW tests directly.
     *   Iterate over the test's direct inputs
     *   Count the number of distinct non-NONE sample types among them.
     *   That count is the processing_cost.
     *
     * COMPOSITE tests are left at cost = 0 and must be skipped here. 
     * They are pure aggregations and carry no processing cost of their own.
     *
     * Prints a summary line for each DERIVED test:
     *   <id>    processing_cost: <value>
     * surrounded by ##COST COMPUTATION## markers.
     */
    public void computeDerivedCosts() {
        System.out.println("##COST COMPUTATION##");

        // TODO: implement Step 2
        // Use System.out.printf("%-25s processing_cost: %d%n", id, cost);

        for(Map.Entry<String, Test> entry : tests.entrySet()) {
            Test t = entry.getValue();

            //only process derived tests
            if (!t.isDerived()) {
                continue;
            }

            //collect distinct non-NONE sample types from direct inputs
            Set<String> distinctSampleTypes = new LinkedHashSet<>();
            for (String inputId : t.inputs) {
                Test inputTest = tests.get(inputId);
                if (inputTest != null && inputTest.isRaw()) {
                    String st = inputTest.sampleType;

                    // Only count non-NONE sample types
                    if (st != null && !"NONE".equalsIgnoreCase(st)) {
                        distinctSampleTypes.add(st);
                    }
                }
            }

            //processing cost is the count of dictinct none-NONE sample types
            t.cost = distinctSampleTypes.size();

            System.out.printf("%-25s processing_cost: %d%n", t.id, t.cost);
        }


        System.out.println("##COST COMPUTATION COMPLETED##");
        System.out.println();
    }

    // =========================================================================
    // Accessors -- do NOT change these
    // =========================================================================

    /** Returns the Test object for the given ID, or null if not found. */
    public Test getTest(String id) {
        return tests.get(id);
    }

    /** Returns an unmodifiable view of all tests in the catalogue. */
    public Map<String, Test> getAllTests() {
        return Collections.unmodifiableMap(tests);
    }
}
