from ExtractTable import ExtractTable
import ssl
print(ssl.OPENSSL_VERSION)
et_sess = ExtractTable(api_key="edUN14QP1dsToeaK9yJo6OB8aP4DO1seK1syfhIM")        # Replace your VALID API Key here
print(et_sess.check_usage())        # Checks the API Key validity as well as shows associated plan usage 
table_data = et_sess.process_file(filepath="upload-dir/data.pdf", output_format="df", pages="all") # To process PDF, make use of pages ("1", "1,3-4", "all") params in the read_pdf function
print(table_data)        # Prints the extracted table data in the form of a Pandas DataFrame
