CrossFitDataExtract
===================
A simple Java console-based program to extract CrossFit Affiliates Data from Google Map at [CrossFit](http://map.crossfit.com/).
It was designed for learning purposes. Built off _Java 7.0_.Project guide was obtained from [Data Extract Google Map](https://www.freelancer.com/projects/Data-Entry-Excel/Data-Extract-Google-Map.html).

Experimental 
------------
- jsoup and openCSV library
- HttpURLConnection for GET requests
- Java concurrency (multi-threading)
- Object-oriented; polymorphism.

CSV Format
----------
CSV generated as entries.csv.
CSV Format generated:

    // Example
    id, name, address, phone, url, latitude, longitude
	"1","http://goldenstatecrossfit.com/","11174 La Grange Ave, Los Angeles, CA","(818) 665-6512","Golden State CrossFit","34.0423563","-118.4413598"

Bugs 
----
`doc[3]` in DataExtract.extractData() may cause `NullPointerException` due to the _CrossFit_ website itself which returns a different text response. Will have to work more on this.

Comments
--------
Comments are greatly appreciated, please do send me an email at `imjching@hotmail.com`.

License
-------
### Acknowledgements
- [CrossFit](http://map.crossfit.com/).
- [openCSV](http://opencsv.sourceforge.net/).
- [jsoup](http://jsoup.org/).

### Copyright
Copyright (C) 2014, J.Ching.

### Licensing
Modified (a/k/a "New") BSD License - refer to the LICENSE file for more information or click [here](http://www.opensource.org/licenses/bsd-3-clause).