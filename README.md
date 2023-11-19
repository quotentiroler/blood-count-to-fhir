# pdf-to-national-fhir
Map blood values from pdf file with unkown structure to national fhir profiles
Starting with German NCD https://www.medizininformatik-initiative.de/Kerndatensatz/Modul_Laborbefund/BeschreibungModul.html

## What is happening here?

1. OCR via ExtractTables (will be replaced once OCR is trained) extracts tables from uploaded .pdf file
2. GPT4FREE converts the extracted tables to POJO .json
3. POJO gets converted into FHIR profiles

## Required Interpreters

Python <=3.9.13 (higher needs additional fixes)
Java 17 (maybe lower is possible)

## Routes

- GET "/" to see UI
- POST "/" for file upload
- GET "/blood" to run the 3 steps mentioned above on .pdf file in upload-dir
- POST "/blood" to convert POJO into FHIR profile
- GET "/files/{filename:.+}" to download uploaded file
- GET "test" to test the 2nd and 3rd step mentioned above
- GET /chat to test the GPT4FREE (needs message in Request Body)


## Test

The test class sometimes fails due to GPT4FREE output which is not always the same. 

## Important

ExtractTables API is not free to use. The API key was charged with 50 credits for 2 USD. 
To try it out anyway, login with generated security password and username "user" is required.
