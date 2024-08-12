package com.api.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;

import com.api.BloodDetails;
import com.itextpdf.html2pdf.HtmlConverter;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;

import ca.uhn.fhir.context.FhirContext;

public class DataGenerator {

    BloodDetails bloodDetails;
    List<Observation> observations;
    DiagnosticReport report;

    private FhirContext fhirContext;

    public DataGenerator(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
        this.bloodDetails = new BloodDetails();
        bloodDetails.fillRandom();
        observations = bloodDetails.toObservations();
        report = bloodDetails.toReport(observations);
    }

    public void generateTestBundle() {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.COLLECTION);
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(report));
        int c = 1;
        for (Observation o : observations) {
            bundle.addEntry().setResource(o).setFullUrl("blood:tofhir:" + c++);
        }
        String output = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        String baseFilename = "test-output/report";
        String extension = ".json";
        File file = new File(baseFilename + extension);
        int counter = 1;
        while (file.exists()) {
            file = new File(baseFilename + "-" + counter + extension);
            counter++;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateTestTable() {
        StringBuilder output = new StringBuilder();
        output.append("Blood Test Report\n\n");
        output.append("| Attribute | Value |\n");
        output.append("|-----------|-------|\n");
        output.append("| Patient   | ").append(report.getSubject().getIdentifier().getValue()).append(" |\n");
        output.append("| Issued    | ").append(report.getIssued()).append(" |\n");
        output.append("| Practitioner | ").append(report.getPerformerFirstRep().getDisplay()).append(" |\n\n");
        output.append("Results:\n");
        output.append("| Test      | Value |\n");
        output.append("|-----------|-------|\n");
        for (Observation o : observations) {
            output.append("| ").append(o.getCode().getCodingFirstRep().getDisplay()).append(" | ")
                    .append(o.getValueQuantity().getValue()).append(" |\n");
        }
        String baseFilename = "test-output/table";
        String extension = ".txt";
        File file = new File(baseFilename + extension);
        int counter = 1;
        while (file.exists()) {
            file = new File(baseFilename + "-" + counter + extension);
            counter++;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(output.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateTestHTMLTable() {
        StringBuilder output = new StringBuilder();
        output.append("<html><head><title>Blood Test Report</title></head><body>");
        output.append("<h1>Blood Test Report</h1>");
        output.append("<table><tr><th>Attribute</th><th>Value</th></tr>");
        output.append("<tr><td>Patient</td><td>").append(report.getSubject().getIdentifier().getValue())
                .append("</td></tr>");
        output.append("<tr><td>Issued</td><td>").append(report.getIssued());
        output.append("<tr><td>Practitioner</td><td>").append(report.getPerformerFirstRep().getDisplay()).append("</td></tr></table>");
        output.append("<h2>Results:</h2>");
        output.append("<table><tr><th>Test</th><th>Value</th></tr>");
        for (Observation o : observations) {
            output.append("<tr><td>").append(o.getCode().getCodingFirstRep().getDisplay()).append("</td><td>")
                    .append(o.getValueQuantity().getValue()).append("</td></tr>");
        }
        output.append("</table></body></html>");
        String baseFilename = "test-output/table";
        String extension = ".html";
        File file = new File(baseFilename + extension);
        int counter = 1;
        while (file.exists()) {
            file = new File(baseFilename + "-" + counter + extension);
            counter++;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(output.toString());
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void convertHtmlToPdfInFolder() {
        File folder = new File("test-output");
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("The folder 'test-output' does not exist or is not a directory.");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));
        if (files == null || files.length == 0) {
            System.out.println("No HTML files found in the 'test-output' folder.");
            return;
        }

        for (File htmlFile : files) {
            String pdfFilename = htmlFile.getAbsolutePath().replace(".html", ".pdf");
            File pdfFile = new File(pdfFilename);
            try {
                HtmlConverter.convertToPdf(htmlFile, pdfFile);
                System.out.println("Converted " + htmlFile.getName() + " to " + pdfFile.getName());
            } catch (IOException e) {
                System.err.println("Failed to convert " + htmlFile.getName() + " to PDF.");
                e.printStackTrace();
            }
        }
    }

    public void generateAll() {
        generateTestBundle();
        generateTestTable();
        generateTestHTMLTable();
    }

    public void reset() {
        bloodDetails.fillRandom();
        observations = bloodDetails.toObservations();
        report = bloodDetails.toReport(observations);
    }

}
