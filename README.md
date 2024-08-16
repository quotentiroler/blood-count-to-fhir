# pdf-to-national-fhir
Map pdf file with unkown structure to NCD starting with German NCD for Blood Count Values https://www.medizininformatik-initiative.de/Kerndatensatz/Modul_Laborbefund/BeschreibungModul.html

## What is happening here?

1. OCR extracts tables from uploaded .pdf file
2. GPT4FREE converts the extracted tables to POJO .json
3. POJO gets converted into FHIR profiles

## Required Interpreters

- Python3 with dependencies as defined in requirements.txt
- Java 17 with dependencies as defined in pom.xml

## Routes

- GET / to see UI
- POST / for file upload
- GET /blood to run the 3 steps mentioned above on .pdf file in upload-dir
- POST /blood" to convert POJO into FHIR profile
- GET /files/{filename:.+} to download uploaded file
- GET /test to test the 2nd and 3rd step mentioned above
- GET /chat to test the GPT4FREE (needs message in Request Body)
- GET /swagger-ui/index.html and actuator
- GET /generate to generate test data
- 
## Test

The test class does not include OCR testing. The OCR already happened when the tests are being run. The tests expect more than 20, but less than 30 values to be mapped. Why? Because the input PDF file had very bad quality as well as the original piece of paper that was scanned using a mobile phone camera. After testing different gpt-APIs, it turned out that no API can map more than 30 values in the given scenario accurately. Therefore, if more than 30 values are mapped, at least few of them are faulty and thus, test#3 fails. This shows also that even in fault-prone scenarios, the mapping can be completed and some GPT-APIs manage to exclude faulty values without any additional configuration, but not all of them. <br>

The test class says nothing about the accuracy when other input data with different quality is provided. If the input data has better quality, more values would be expected to be mapped (ideally, all of them). The input data can be improved by either using a proper scanner instead of a mobile phone camera, and/or by creating a new OCR-lib that is trained for this purpose. Tesseract OCR is already included in the repository, but the algorithm yet has to be trained since it is too inaccurate to be used at this point.

## Important

ExtractTables API is not free to use. The API key was charged with 50 credits for 2 USD. <br>
To try it out anyway, login with username and password "user" is required. It is recommended and sufficient to use the Test class instead. <br>
