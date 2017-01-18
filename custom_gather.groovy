/**
 * Converts a CSV file to XML, each row being stored as a separate record
 *
 * The XML tag names will be taken from the CSV column headers, or custom
 * headers defined in collection.cfg
 */
import com.funnelback.common.*;
import com.funnelback.common.config.*;
import com.funnelback.common.io.store.*;
import com.funnelback.common.io.store.xml.*;
import com.funnelback.common.utils.*;
import java.net.URL;
// CSV parser imports
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*
def final XML_HEADER = '<?xml version="1.0" encoding="utf-8"?>\n'
// Get our arguments, SEARCH_HOME first then the collection id
def searchHome = new File(args[0])
def collection = args[1]
// Create a configuration object to read collection.cfg
def config = new NoOptionsConfig(searchHome, collection)
// Create a Store instance to store gathered data
def store = new XmlStoreFactory(config).newStore()
// Read configuration or fall back to default values
def format = config.value("csv.format", "csv")
def csvHeader = config.valueAsBoolean("csv.header", true)
def csvCustomHeader = config.value("csv.header.custom")
def csvEncoding = config.value("csv.encoding", "UTF-8")
def csvDebug = config.valueAsBoolean("csv.debug", false)
// define the available fileformat based on supported types.
// See https://commons.apache.org/proper/commons-csv/archives/1.0/apidocs/org/apache/commons/csv/CSVFormat.html
def csvFormats = ["csv": DEFAULT, "xls": EXCEL, "rfc4180": RFC4180, "tsv": TDF, "mysql": MYSQL]
// Open the XML store
store.open()
def sources = config.value("data.sources").split(",")
sources.each { source ->
    println "Gathering CSV from ${source}"
    def csvText = new URL(source).getText(csvEncoding)
    // Remove blank lines
    csvText = csvText.replaceAll("[\n\r]+","\n")
    def csv = null
    if (csvHeader) {
        // use the header row to define fields
        csv = CSVParser.parse(csvText, csvFormats[format].withHeader())
    } else {
        if (csvCustomHeader != null) {
            // use field definitions
            csv = CSVParser.parse(csvText, csvFormats[format].withHeader(csvCustomHeader.split(",")))
        }
        else {
            csv = CSVParser.parse(csvText, csvFormats[format])
        }
    }
    
    // Note: counter will reset for each file
    def i = 0
    for (record in csv.iterator()) {
        if ((i % 100) == 0) {
            // Check to see if the update has been stopped 
            if (config.isUpdateStopped()) {
                store.close()
                throw new RuntimeException("Update stop requested by user.")
            }    	
            config.setProgressMessage("Processed "+i+" records")
        }
        def fields = record.toMap()
        def xmlString = new StringWriter()
        def xmlBuilder = new groovy.xml.MarkupBuilder(xmlString)
        xmlBuilder.item() {
            fields.each() { key, value ->
                // Replace non-word (\W) chars with _ to avoid illegal XML field names
                def escapedKey = key.replaceAll(/\W/, "_")
                // Trim whitespaces from values
                def trimmedValue = value.trim()
                // Yield an XML tag named after the key, containing the value
                "${escapedKey}"(trimmedValue)
            }
        }
        def xmlRecord = XML_HEADER + xmlString;
        if (csvDebug) {
            println xmlRecord
        }
        store.add(new XmlRecord(XMLUtils.fromString(xmlRecord), source+"/doc"+i))
        i++
    }
}
// close() required for the store to be flushed
store.close()