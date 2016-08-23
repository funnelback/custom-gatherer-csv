Custom gatherer for use with a custom collection for download of csv/tsv files.

Add URLs for CSV to fetch to collection.cfg.start.urls file.  Format is 1 URL per line.  Each URL is fetched and the CSV is processed using the set of options defined.  Note: only 1 set of csv options is supported so the same settings will be applied to all the CSV files downloaded.

Supports the following collection.cfg settings:

Collection.cfg options:

csv.format (NOTE: only tested with csv and tsv)
values: These map to the available CSVFormat types
csv  (default) 
xls
rfc4180
tsv
mysql

csv.encoding
values: 
Java character encoding - eg: UTF-8  (default), ISO-8859-1  

csv.header
values: true (CSV has a header line) (default) / false (CSV does not have a header line)

csv.header.custom
Value: comma separated list of header titles.  Assumes the number of items matches the number of columns in CSV.  Non word chars are converted to underscores
eg. csv.header.custom=field1,field2,field3,field4
Required if csv.header=false

csv.debug
values: true (print out some additional information to the logs) / false (default)
