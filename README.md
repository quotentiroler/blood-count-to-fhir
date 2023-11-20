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

## Test

The test class sometimes fails due to GPT4FREE output which is not always the same. But it should work at least 9 out of 10 times.

## Important

ExtractTables API is not free to use. The API key was charged with 50 credits for 2 USD. <br>
To try it out anyway, login with username and password "user" is required. It is recommended and sufficient to use the Test class instead. <br>
<br>
There is also a Class for OCR with Tesseract (OCR.java) which is open source, but the accuracy is too bad to be processed by the NLP at the current time.