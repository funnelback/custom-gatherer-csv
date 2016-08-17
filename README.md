Custom gatherer for use with a custom collection for download of csv/tsv files.

Supports the following collection.cfg settings:

Collection.cfg options:

csv.format (NOTE: only tested with csv and tsv)
values: These map to the available CSVFormat types
csv 
xls
rfc4180
tsv
mysql

csv.encoding
values: 
Java character encoding - eg: UTF-8, ISO-8859-1  

csv.sourceurl
set this to the URL of where to fetch the CSV document

csv.header
values: true (CSV has a header line) / false (CSV does not have a header line)

csv.header.custom
Value: comma separated list of header titles.  Assumes the number of items matches the number of columns in CSV.  Non word chars are converted to underscores
eg. csv.header.custom=field1,field2,field3,field4
Required if csv.header=false