package com.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.LoggerFactory;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;


/*
 * This class is used to extract text from a pdf file
 * It uses the Tesseract OCR engine which is free and open source
 */
public class OCR {

    public static String doOCR() throws IOException {

        PDDocument document = PDDocument.load(new File("upload-dir/data.pdf"));
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        ITesseract tesseract = new Tesseract();

        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("deu");

        StringBuilder bld = new StringBuilder();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

            try {
                bld.append(tesseract.doOCR(bufferedImage));
            } catch (TesseractException ex) {
                LoggerFactory.getLogger(OCR.class.getName()).error("Error");
            }
        }
        document.close();
        return bld.toString();
    }

}
