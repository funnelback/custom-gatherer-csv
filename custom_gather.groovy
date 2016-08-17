import com.funnelback.common.*;
import com.funnelback.common.config.*;
import com.funnelback.common.io.store.*;
import com.funnelback.common.io.store.xml.*;
import com.funnelback.common.utils.*;
import java.net.URL;

//CSV imports
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*

// Create a configuration object to read collection.cfg
def config = new NoOptionsConfig(new File(args[0]), args[1]);

if (config.value("csv.sourceurl") == null) {
        throw new RuntimeException("Error: CSV source URL is not defined");
}

// Create a Store instance to store gathered data
def store = new XmlStoreFactory(config).newStore();

// define default values
def format = "csv";
// define the available fileformat based on supported types.  see https://commons.apache.org/proper/commons-csv/archives/1.0/apidocs/org/apache/commons/csv/CSVFormat.html
def csvFormat = ["csv":DEFAULT,"xls":EXCEL,"rfc4180":RFC4180,"tsv":TDF,"mysql":MYSQL]
def csvEncoding = "UTF-8"
def csvHeader = "true";
def csvDebug = false;

// read config from collection.cfg and override defaults
if (config.value("csv.format") != null) {format = config.value("csv.format")}
if (config.value("csv.header") != null) {csvHeader = config.value("csv.header")}
if (config.value("csv.encoding") != null) {csvEncoding = config.value("csv.encoding")}
if (config.value("csv.format") != null) {format = config.value("csv.format")}
if (config.value("csv.debug") != null) {csvDebug = config.value("csv.debug")}

// Open the XML store
store.open()

// Fetch the CSV file and convert it to XML
def csvText = new URL(config.value("csv.sourceurl")).getText(csvEncoding)
// remove blank lines
csvText = csvText.replaceAll("[\n\r]+","\n")

if (csvDebug) {println csvText}

CSVParser csv = null
if (Boolean.parseBoolean(csvHeader)) {
    // use the header row to define fields
        csv = CSVParser.parse(csvText, csvFormat[format].withHeader())
}
else {
        if (config.value("csv.header.custom") != null) {
        // use field definitions
                csv = CSVParser.parse(csvText, csvFormat[format].withHeader(config.value("csv.header.custom").split(",")))
        }
        else {
                csv = CSVParser.parse(csvText, csvFormat[format])
        }
}
def i=0;
for (record in csv.iterator()) {
    if ((i % 100) == 0)
    {
    	// Check to see if the update has been stopped 
        if (config.isUpdateStopped()) {
                store.close()
                throw new RuntimeException("Update stop requested by user.");
        }    	
        config.setProgressMessage("Processed "+i+" records");
    }
    def fields = record.toMap();
        def xmlString = new StringWriter();
        def xmlRecord = new groovy.xml.MarkupBuilder(xmlString);

        xmlRecord.item() {
                fields.each() {key, value ->
                        // replace non-word chars with _ to avoid illegal XML field names and strip whitespace from values
                        "${key.replaceAll(/\W/, '_')}""${value.replaceAll(/^\s+|\s+$/, '')}"
                }
        }

    if (csvDebug) {println "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+xmlString}

    def xmlContent = XMLUtils.fromString("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+xmlString)
        store.add(new XmlRecord(xmlContent, config.value("csv.sourceurl")+"/doc"+i))
        i++
}

// close() required for the store to be flushed
store.close()