import javax.swing.text.html.parser.Element; //kullanmadım?
import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

/**
 * DiagnosticRequest
 *
 * Parses diagnostic_requests.xml and exposes:
 *   - single_target : the test ID for top-down patient burden analysis (Step 3)
 *   - all_targets   : list of test IDs for bottom-up hospital cost analysis (Step 4)
 *
 * XML structure:
 *   <requests>
 *     <single_target ref="overall_health"/>
 *     <all_targets>
 *       <target ref="overall_health"/>
 *       <target ref="cardiovascular_risk"/>
 *     </all_targets>
 *   </requests>
 *
 */
public class DiagnosticRequest {

    private String       singleTarget;
    private List<String> allTargets;

    private DiagnosticRequest() {}

    /**
     * Parses the given XML file and returns a populated DiagnosticRequest.
     *
     * @param filePath path to diagnostic_requests.xml
     * @return populated DiagnosticRequest
     */
    public static DiagnosticRequest loadFromXML(String filePath) {
        DiagnosticRequest req = new DiagnosticRequest();
        req.allTargets = new ArrayList<>();

        /** TODO: Parse the given XML file
        *  return a populated DiagnosticRequest
        *  set singleTarget and allTargets
        */

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            //parse single_target
            NodeList singleList = doc.getElementsByTagName("single_target");
            if (singleList.getLength() > 0) {
                org.w3c.dom.Element singleElem = (org.w3c.dom.Element) singleList.item(0);
                req.singleTarget = singleElem.getAttribute("ref");

                /*Element singleElem = (Element) singleList.item(0);
                req.singleTarget = singleElem.getAttribute("ref");
                bunu kullanmak için şunu eklemelisin: import org.w3c.dom.Element;
                şimdilik import etme belki sıkıntı çıkar */   
            }

            //parse all targets
            NodeList targetList = doc.getElementsByTagName("target");
            for (int i = 0; i < targetList.getLength(); i++) {
                org.w3c.dom.Element targetElem = (org.w3c.dom.Element) targetList.item(i);
                req.allTargets.add(targetElem.getAttribute("ref"));

                /*Element targetElem = (Element) targetList.item(i);
                req.allTargets.add(targetElem.getAttribute("ref"));*/
            }

        } catch (Exception e) {
            System.err.println("Error parsing requests XML: " + e.getMessage());
            e.printStackTrace();
        }

        return req;
    }

    /** Returns the single target test ID for top-down analysis. */
    public String getSingleTarget() {
        return singleTarget;
    }

    /** Returns the list of target test IDs for bottom-up analysis. */
    public List<String> getAllTargets() {
        return Collections.unmodifiableList(allTargets);
    }
}
