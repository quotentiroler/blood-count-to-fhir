from ExtractTable import ExtractTable
et_sess = ExtractTable(api_key="pebe0qJFTnUdgzzNSwlyYAAqMK6S6k95A8hxudgZ")        # Replace your VALID API Key here
print(et_sess.check_usage())        # Checks the API Key validity as well as shows associated plan usage 
table_data = et_sess.process_file(filepath="test.pdf", output_format="df")
#print(table_data)        # Prints the extracted table data in the form of a Pandas DataFrame
# To process PDF, make use of pages ("1", "1,3-4", "all") params in the read_pdf function
#table_data = et_sess.process_file(filepath=Location_of_PDF_with_Tables, output_format="df", pages="all")