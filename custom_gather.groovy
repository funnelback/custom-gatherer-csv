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

// Create a Store instance to store gathered data
def store = new XmlStoreFactory(config).newStore();

// set the fileformat based on supported types.  see https://commons.apache.org/proper/commons-csv/archives/1.0/apidocs/org/apache/commons/csv/CSVFormat.html
def format = config.value("csv.format")
def csvFormat = ["csv":DEFAULT,"xls":EXCEL,"rfc4180":RFC4180,"tsv":TDF,"mysql":MYSQL]
def csvEncoding = config.value("csv.encoding")

// Open the XML store
store.open()

// Fetch the CSV file and convert it to XML
def csvText = new URL(config.value("csv.sourceurl")).getText(csvEncoding) 
// Uncomment to see source data in update log
//println csvText
CSVParser csv = null
if (Boolean.parseBoolean(config.value("csv.header"))) { 
    // use the header row to define fields
	csv = CSVParser.parse(csvText, csvFormat[format].withHeader())
}
else { 
	// use field definitions
	csv = CSVParser.parse(csvText, csvFormat[format].withHeader(config.value("csv.header.custom").split(",")))
}
def i=0;
for (record in csv.iterator()) {
    def fields = record.toMap();
	def xmlString = new StringWriter();
	def xmlRecord = new groovy.xml.MarkupBuilder(xmlString);
	xmlRecord.item() {
		fields.each() {key, value ->
			// replace non-word chars with _ to avoid illegal XML field names
			"${key.replaceAll(/\W/, '_')}""${value}"
		}
	}
// Uncomment to see stored XML data in update log
//    println "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+xmlString
    def xmlContent = XMLUtils.fromString("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+xmlString)
	store.add(new XmlRecord(xmlContent, config.value("csv.sourceurl")+"/doc"+i))
	i++
}

// close() required for the store to be flushed
store.close()
