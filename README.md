# CSV custom gatherer

Note: if you are using Funnelback 15.10 or newer you should use the built-in CSVToXML filter with a web collection instead of using this custom gatherer.

Custom gatherer for use with a custom collection for download of csv/tsv files.

Add URLs for CSV to fetch using the data.sources collection.cfg option.  Each URL is fetched and the CSV is processed using the set of options defined.  Note: only 1 set of csv options is supported so the same settings will be applied to all the CSV files downloaded.

Note: you need to set the following collection.cfg option otherwise cache copies will not work:

```
store.record.type=XmlRecord
```

## Compatibility

The custom-gatherer.groovy requires the org.apache.commons/commons-csv library.  This is included in Funnelback 15.

To use with v14, the org.apache.commons/commons-csv jar file must be downloaded and decompressed into the @groovy folder of the collection using the custom gatherer.

## Collection.cfg options

The CSV custom gatherer supports the following collection.cfg settings:


### data.sources

Contains a comma-separated list of CSV URLs to index. Must be specified as URLs.  Local files should be specified using the file:// URL notation (e.g. ```file:///opt/funnelback/data/mycollection/offline/data/mycsvfile.csv```).

### csv.format 

NOTE: only tested with csv and tsv

*Values:* These map to the available CSVFormat types
* csv  (default) 
* xls
* rfc4180
* tsv
* mysql

### csv.encoding

*Values:* 
Java character encoding - eg: UTF-8  (default), ISO-8859-1  

### csv.header
*Values:* true (CSV has a header line) (default) / false (CSV does not have a header line)

Note: non word characters included in header fields are converted to underscores when generating the XML field names.

### csv.header.custom

*Value:* comma separated list of header titles.  Assumes the number of items matches the number of columns in CSV.  Non word chars are converted to underscores

eg. csv.header.custom=field1,field2,field3,field4

Required if csv.header=false

### csv.debug

*Values:* true (print out some additional information to the logs) / false (default)

## XML record format
Each row in the CSV file is mapped to an xml file with the column values nested inside an outer ```<item>``` element.

The individual data items are stored as subitems in elements named using the  CSV column heading (or csv.header.custom value)

```
<?xml version="1.0" encoding="utf-8"?>
<item>
	<FIELDNAME>FIELDVALUE</FIELDNAME>
	...
</item>
```
## Item URLs 
If a field contains a unique URL value it can be mapped to the docurl field in the xml.cfg using /item/FIELDNAME

If docurl isn't set then URLs are automatically assigned using the CSV filename with the row appended.  e.g. csvfilename.csv/004

## Example

For a CSV file that looks like the following:

```
"DVD Title","Studio","Released","Status","Sound","Versions","Price","Rating","Year","Genre","Aspect","UPC","DVD_ReleaseDate","ID","Timestamp","Link_Address"
"Best Of HypeFest 2003: Spiderweb / Love Chains / Family In Mind / Ritchie's Itch / Blood Hunt / eRATicate / ...","CustomFlix",,"Out","2.0","4:3",19.95,"NR","VAR","VAR","1.33:1","879724005239",2004-01-01 00:00:00,89591,2006-07-21 00:00:00,"http://example.com/video001.html"
"Best Of ICW Wrestling: Vol, 2","Jadat Sports",,"Out","2.0","4:3",12.95,"NR","VAR","Sports","1.33:1","760137867395",2016-08-16 00:00:00,292588,2016-08-16 00:00:00,"http://example.com/video002.html"
"Best Of Jazz In Burghausen, Vol. 3","Double Moon",,"Out","2.0","4:3",24.98,"NR","UNK","Music","1.33:1","608917170498",2009-07-14 00:00:00,161994,2010-08-18 00:00:00,"http://example.com/video003.html"
"Best Of Jazz On TDK 2007","TDK Music DVD",,"Discontinued","5.1/DTS","LBX, 16:9",9.99,"NR","2007","Music","1.85:1","824121002176",2007-02-27 00:00:00,103660,2012-02-23 00:00:00,"http://example.com/video004.html"
"Best Of JDI, Vol. 1","JDI Records",,"Out","2.0","4:3",15.98,"NR","UNK","Music","1.33:1","798321127291",2007-09-18 00:00:00,117900,2014-11-26 00:00:00,"http://example.com/video005.html"
"Best Of JDI, Vol. 2","JDI Records",,"Out","2.0","4:3",15.98,"NR","UNK","Music","1.33:1","798321127390",2007-09-18 00:00:00,117894,2014-11-26 00:00:00,"http://example.com/video006.html"
"Best Of JDI, Vol. 3","JDI Records",,"Out","2.0","4:3",15.98,"NR","UNK","Music","1.33:1","798321127499",2007-09-18 00:00:00,117875,2014-11-26 00:00:00,"http://example.com/video007.html"
"Best Of John Wayne (2-Pack): Dawn Rider / Hurricane Express / McLintock! / Star Packer / Texas Terror / Trail Beyond / ...","GoodTimes Media",,"Discontinued","2.0","4:3",14.98,"NR","VAR","Western","1.33:1","018713833150",2004-06-01 00:00:00,42175,2011-07-23 00:00:00,"http://example.com/video008.html"
"Best Of John Wayne Collection 1: Rio Lobo / El Dorado / True Grit","Paramount",,"Discontinued","2.0","LBX",29.99,"NR","VAR","Western","1.85:1","097360561746",2003-04-29 00:00:00,26655,2007-05-19 00:00:00,"http://example.com/video009.html"
"Best Of John Wayne Collection 1: Rio Lobo / El Dorado / True Grit (Checkpoint)","Paramount",,"Discontinued","2.0","LBX",29.99,"NR","VAR","Western","1.85:1","097360561722",2003-04-29 00:00:00,57003,2013-01-05 00:00:00,"http://example.com/video010.html"
```
An xml.cfg similar to the following can be used
```
PADRE XML Mapping Version: 2
docurl,/item/Link_Address
dvdTitle,1,,//DVD_Title
dvdStudio,1,,//Studio
dvdReleased,0,,//Released
dvdStatus,0,,//Status
dvdSound,0,,//Sound
dvdVersions,0,,//Versions
dvdPrice,3,,//Price
dvdRating,0,,//Rating
dvdYear,0,,//Year
dvdGenre,1,,//Genre
dvdAspect//Aspect
dvdUpc//UPC
d,0,,//DVD_ReleaseDate
dvdId,0,,//ID
dvdTimestamp,0,,//Timestamp
```
A sample collection.cfg might look like:
```
#
# Filename: /opt/funnelback/conf/showcase-csv/collection.cfg
#
collection=csv
collection_group=Example collections
collection_type=custom
csv.debug=true
data.sources=http://examplecsv.com/dvd_csv.txt
data_report=false
filter=false
gather=custom-gather
query_processor_options=-stem=2 -SM=meta -SF=[dvdTitle,dvdStudio,dvdSound,dvdReleased,dvdStatus,dvdVersions,dvdPrice,dvdRating,dvdYear,dvdGenre,dvdAspect,dvdUpc] -rmc_sensitive=true
service_name=CSV example
spelling.suggestion_sources=[@,dvdTitle,%]
start_url=
store.record.type=XmlRecord
```
With csv.debug=true set additional information is written to the log files.  In 15.6 this is written to the main collection update log.

e.g.
```
...
  <Timestamp>2016-06-07 00:00:00</Timestamp>
  <Versions>4:3</Versions>
  <Year>UNK</Year>
  <Price>31.95</Price>
  <DVD_ReleaseDate>2005-05-10 00:00:00</DVD_ReleaseDate>
  <Genre>Music</Genre>
  <ID>61702</ID>
</item>
<?xml version="1.0" encoding="utf-8"?>
<item>
  <Status>Out</Status>
  <Released></Released>
  <Rating>NR</Rating>
  <UPC>4000127201294</UPC>
  <Sound>2.0</Sound>
  <DVD_Title>!!!! Beat, Vol. 4: Shows 14 - 17</DVD_Title>
  <Aspect>1.33:1</Aspect>
  <Studio>Bear Family</Studio>
  <Timestamp>2016-06-07 00:00:00</Timestamp>
  <Versions>4:3</Versions>
  <Year>UNK</Year>
  <Price>31.95</Price>
  <DVD_ReleaseDate>2005-07-12 00:00:00</DVD_ReleaseDate>
  <Genre>Music</Genre>
  <ID>65695</ID>
</item>
<?xml version="1.0" encoding="utf-8"?>
<item>
  <Status>Out</Status>
  <Genre>Animation</Genre>
...

```

