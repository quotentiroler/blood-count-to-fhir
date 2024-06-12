package com.api;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.difuture.component.fhiretlutils.util.FHIRUtils;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BloodDetails {

    private static final Logger logger = LoggerFactory.getLogger(BloodDetails.class);
    private static String now = Instant.now().toString();

    private String practitioner;
    private String patient;
    private String orderId;
    private String date;
    private List<String> materials;
    private String erythrocytes;
    private String hemoglobin;
    private String hbeMch;
    private String mcv;
    private String hematocrit;
    private String mchc;
    private String rdwEry;
    private String platelets;
    private String leukocytes;
    private String gotAst;
    private String gptAlt;
    private String gammaGt;
    private String bilirubin;
    private String amylase;
    private String sodium;
    private String potassium;
    private String glucose;
    private String hbA1cAbsolute;
    private String hbA1cRelative;
    private String cholesterol;
    private String triglycerides;
    private String hdlCholesterol;
    private String ldlCholesterol;
    private String tshBasal;
    private String creatinine;
    private String gfr2005;
    private String gfr2009;
    private String urea;
    private String uricAcid;
    private String iron;
    private String ferritin;
    private String crp;
    private String vitaminD3;
    private String vitaminB12;
    private String folicAcid;
    private String nonHdlCholesterol;

    public DiagnosticReport toReport(List<Observation> observations) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        diagnosticReport.setMeta(new Meta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab"));

        Identifier befund = new Identifier().setSystem("http://mii-standort.example.de/fhir/NamingSystem/pid")
                .setValue(patient + "-" + now);
        Coding fillerV2 = new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "FILL", "Filler Identifier");
        befund.setType(new CodeableConcept().addCoding(fillerV2));
        diagnosticReport.addIdentifier(befund);

        diagnosticReport.setBasedOn(List.of(new Reference().setIdentifier(
                new Identifier().setValue(orderId).setSystem("http://mii-standort.example.de/fhir/NamingSystem/pid"))));

        diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        Coding loinclab = new Coding("http://loinc.org", "26436-6", "Laboratory studies (set)");
        Coding dss = new Coding("http://terminology.hl7.org/CodeSystem/v2-0074", "LAB",
                "Laboratory");
        diagnosticReport.setCategory(List.of(new CodeableConcept().addCoding(loinclab).addCoding(dss)));

        Coding loinclabReport = new Coding("http://loinc.org", "11502-2", "Laboratory report");
        diagnosticReport.setCode(new CodeableConcept().addCoding(loinclabReport));

        diagnosticReport.setSubject(new Reference().setIdentifier(new Identifier().setValue(patient)));

        try {
            DateTimeType effective = new DateTimeType(Date.valueOf(date));
            diagnosticReport.setEffective(effective);
        } catch (Exception e) {
            logger.warn("Invalid date value, setting to now");
            diagnosticReport.setEffective(new DateTimeType(Date.from(Instant.now())));
        }

        diagnosticReport.setPerformer(List.of(new Reference().setIdentifier(new Identifier().setValue(practitioner))));

        for (Observation o : observations) {
            diagnosticReport.addResult(new Reference().setIdentifier(o.getIdentifierFirstRep()));
        }
        return diagnosticReport;

    }

    public List<Observation> toObservations() {
        List<Observation> result = new ArrayList<>();

        if (erythrocytes != null) {
            try {
                Observation observation = createObservation("erythrocytes");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(erythrocytes)).setUnit("10*6/uL")
                        .setSystem("http://unitsofmeasure.org").setCode("pL");
                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept().addCoding(
                                new Coding("http://loinc.org", "26453-1", "Erythrocytes [#/volume] in Blood")));
                result.add(observation);

            } catch (Exception e) {
                logger.warn("Invalid erythrocytes value: " + erythrocytes);
            }
        }

        if (hemoglobin != null) {
            try {
                Observation observation = createObservation("hemoglobin");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(hemoglobin)).setUnit("g/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("g/dl");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept().addCoding(
                                new Coding("http://loinc.org", "718-7", "Hemoglobin [Mass/volume] in Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid hemoglobin value: " + hemoglobin);
            }
        }

        if (hbeMch != null) {
            try {
                Observation observation = createObservation("hbeMch");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(hbeMch)).setUnit("pg")
                        .setSystem("http://unitsofmeasure.org").setCode("pg");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("http://loinc.org", "785-6", "MCH [Entitic mass] by Automated count")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid hbeMch value: " + hbeMch);
            }
        }

        if (mcv != null) {
            try {
                Observation observation = createObservation("mcv");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(mcv)).setUnit("fl")
                        .setSystem("http://unitsofmeasure.org").setCode("fl");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "787-2", "MCV [Entitic volume] by Automated count")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid mcv value: " + mcv);
            }
        }

        if (hematocrit != null) {
            try {
                Observation observation = createObservation("hematocrit");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(hematocrit)).setUnit("%")
                        .setSystem("http://unitsofmeasure.org").setCode("%");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "20570-8",
                                        "Hematocrit [Volume Fraction] of Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid hematocrit value: " + hematocrit);
            }
        }

        if (mchc != null) {
            try {
                Observation observation = createObservation("mchc");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(mchc)).setUnit("g/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("g/dl");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "786-4", "MCHC [Mass/volume] by Automated count")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid mchc value: " + mchc);
            }
        }

        if (rdwEry != null) {
            try {
                Observation observation = createObservation("rdwEry");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(rdwEry)).setUnit("%")
                        .setSystem("http://unitsofmeasure.org").setCode("%");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "788-0",
                        "Erythrocyte distribution width [Ratio] by Automated count")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid rdwEry value: " + rdwEry);
            }
        }

        if (platelets != null) {
            try {
                Observation observation = createObservation("platelets");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(platelets)).setUnit("10*3/ul")
                        .setSystem("http://unitsofmeasure.org").setCode("10*3/ul");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "777-3",
                                "Platelets [#/volume] in Blood by Automated count")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid platelets value: " + platelets);
            }
        }

        if (leukocytes != null) {
            try {
                Observation observation = createObservation("leukocytes");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(leukocytes)).setUnit("10*3/ul")
                        .setSystem("http://unitsofmeasure.org").setCode("10*3/ul");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "6690-2",
                                        "Leukocytes [#/volume] in Blood by Automated count")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid leukocytes value: " + leukocytes);
            }
        }

        if (gotAst != null) {
            try {
                Observation observation = createObservation("gotAst");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(gotAst)).setUnit("U/l")
                        .setSystem("http://unitsofmeasure.org").setCode("U/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "1920-8",
                                "Aspartate aminotransferase [Enzymatic activity/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid gotAst value: " + gotAst);
            }
        }

        if (gptAlt != null) {
            try {
                Observation observation = createObservation("gptAlt");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(gptAlt)).setUnit("U/l")
                        .setSystem("http://unitsofmeasure.org").setCode("U/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "1742-6",
                                "Alanine aminotransferase [Enzymatic activity/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid gptAlt value: " + gptAlt);
            }
        }

        if (gammaGt != null) {
            try {
                Observation observation = createObservation("gammaGt");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(gammaGt)).setUnit("U/l")
                        .setSystem("http://unitsofmeasure.org").setCode("U/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "2324-2",
                                "Gamma glutamyl transferase [Enzymatic activity/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid gammaGt value: " + gammaGt);
            }
        }

        if (bilirubin != null) {
            try {
                Observation observation = createObservation("bilirubin");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(bilirubin)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(
                                new Coding("https://loinc.org/", "42719-5", "Bilirubin.total [Mass/volume] in Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid bilirubin value: " + bilirubin);
            }
        }

        if (amylase != null) {
            try {
                Observation observation = createObservation("amylase");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(amylase)).setUnit("U/l")
                        .setSystem("http://unitsofmeasure.org").setCode("U/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/1798-8", "1798-8",
                                "Amylase [Enzymatic activity/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid amylase value: " + amylase);
            }
        }

        if (sodium != null) {
            try {
                Observation observation = createObservation("sodium");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(sodium)).setUnit("mmol/l")
                        .setSystem("http://unitsofmeasure.org").setCode("mmol/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/2947-0", "2947-0",
                                "Sodium [Moles/volume] in Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid sodium value: " + sodium);
            }
        }

        if (potassium != null) {
            try {
                Observation observation = createObservation("potassium");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(potassium)).setUnit("mmol/l")
                        .setSystem("http://unitsofmeasure.org").setCode("mmol/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "6298-4", "Potassium [Moles/volume] in Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid potassium value: " + potassium);
            }
        }

        if (glucose != null) {
            try {
                Observation observation = createObservation("glucose");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(glucose)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "2345-7",
                                 "Glucose [Mass/volume]")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid glucose value: " + glucose);
            }
        }

        if (hbA1cAbsolute != null) {
            try {
                Observation observation = createObservation("hbA1cAbsolute");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(hbA1cAbsolute)).setUnit("%")
                        .setSystem("http://unitsofmeasure.org").setCode("%");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "4548-4",
                                "Hemoglobin A1c/Hemoglobin.total in Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid hbA1cAbsolute value: " + hbA1cAbsolute);
            }
        }

        if (hbA1cRelative != null) {
            try {
                Observation observation = createObservation("hbA1cRelative");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(hbA1cRelative)).setUnit("mmol/mol")
                        .setSystem("http://unitsofmeasure.org").setCode("mmol/mol");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "HBA1C",
                         "hbA1cRelative")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid hbA1cRelative value: " + hbA1cRelative);
            }
        }

        if (cholesterol != null) {
            try {
                Observation observation = createObservation("cholesterol");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(cholesterol)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "2093-3",
                                        "Cholesterol [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid cholesterol value: " + cholesterol);
            }
        }

        if (triglycerides != null) {
            try {
                Observation observation = createObservation("triglycerides");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(triglycerides)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept().addCoding(new Coding("http://loinc.org", "2571-8", "triglycerides")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid triglycerides value: " + triglycerides);
            }
        }

        if (hdlCholesterol != null) {
            try {
                Observation observation = createObservation("hdlCholesterol");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(hdlCholesterol)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept().addCoding(new Coding("http://loinc.org", "2085-9",
                                "Cholesterol in HDL [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid hdlCholesterol value: " + hdlCholesterol);
            }
        }

        if (ldlCholesterol != null) {
            try {
                Observation observation = createObservation("ldlCholesterol");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(ldlCholesterol)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept().addCoding(new Coding("http://loinc.org", "18262-6",
                                "Cholesterol in LDL [Mass/volume] in Serum or Plasma by Direct assay")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid ldlCholesterol value: " + ldlCholesterol);
            }
        }

        if (tshBasal != null) {
            try {
                Observation observation = createObservation("tshBasal");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(tshBasal)).setUnit("mU/l")
                        .setSystem("http://unitsofmeasure.org").setCode("mU/l");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "14999-7", "Thyrotropin [Units/volume] in Serum or Plasma --baseline")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid tshBasal value: " + tshBasal);
            }
        }

        if (creatinine != null) {
            try {
                Observation observation = createObservation("creatinine");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(creatinine)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "2160-0",
                                        "Creatinine [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid creatinine value: " + creatinine);
            }
        }

        if (gfr2005 != null) {
            try {
                Observation observation = createObservation("gfr2005");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(gfr2005))
                        .setUnit("mL/min/{1.73_m2}")
                        .setSystem("http://unitsofmeasure.org").setCode("mL/min/{1.73_m2}");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "77147-7",
                         "Glomerular filtration rate/1.73 sq M.predicted [Volume Rate/Area] in Serum, Plasma or Blood by Creatinine-based formula (MDRD)")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid gfr2005 value: " + gfr2005);
            }
        }

        if (gfr2009 != null) {
            try {
                Observation observation = createObservation("gfr2009");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(gfr2009))
                        .setUnit("mL/min/{1.73_m2}")
                        .setSystem("http://unitsofmeasure.org").setCode("mL/min/{1.73_m2}");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "62238-1",
                         "Glomerular filtration rate/1.73 sq M.predicted [Volume Rate/Area] in Serum, Plasma or Blood by Creatinine-based formula (CKD-EPI)")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid gfr2009 value: " + gfr2009);
            }
        }

        if (urea != null) {
            try {
                Observation observation = createObservation("urea");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(urea)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("http://loinc.org", "25549-7", "Urea [Moles/volume] in Body fluid")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid urea value: " + urea);
            }
        }

        if (uricAcid != null) {
            try {
                Observation observation = createObservation("uricAcid");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(uricAcid)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept().addCoding(new Coding("http://loinc.org", "3084-1", "uricAcid")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid uricAcid value: " + uricAcid);
            }
        }

        if (iron != null) {
            try {
                Observation observation = createObservation("iron");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(iron)).setUnit("ug/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("ug/dl");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept()
                                .addCoding(new Coding("https://loinc.org/", "2498-4",
                                        "Iron [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid iron value: " + iron);
            }
        }

        if (ferritin != null) {
            try {
                Observation observation = createObservation("ferritin");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(ferritin)).setUnit("ng/ml")
                        .setSystem("http://unitsofmeasure.org").setCode("ng/ml");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org/", "2276-4",
                                "Ferritin [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid ferritin value: " + ferritin);
            }
        }

        if (crp != null) {
            try {
                Observation observation = createObservation("crp");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(crp)).setUnit("mg/l")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/l");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept()
                                .addCoding(new Coding("https://loinc.org/", "1988-5", "C reactive protein [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid crp value: " + crp);
            }
        }

        if (vitaminD3 != null) {
            try {
                Observation observation = createObservation("vitaminD3");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(vitaminD3)).setUnit("ng/ml")
                        .setSystem("http://unitsofmeasure.org").setCode("ng/ml");

                observation.setValue(valueQuantity);
                observation.setCode(new CodeableConcept()
                        .addCoding(new Coding("https://loinc.org", "1989-3", "vitaminD3")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid vitaminD3 value: " + vitaminD3);
            }
        }

        if (vitaminB12 != null) {
            try {
                Observation observation = createObservation("vitaminB12");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(vitaminB12)).setUnit("pg/ml")
                        .setSystem("http://unitsofmeasure.org").setCode("pg/ml");

                observation.setValue(valueQuantity);
                observation
                        .setCode(new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "16695-9",
                                        "Cobalamin (Vitamin B12) [Mass/volume] in Blood")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid vitaminB12 value: " + vitaminB12);
            }
        }

        if (folicAcid != null) {
            try {
                Observation observation = createObservation("folicAcid");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(folicAcid)).setUnit("ng/ml")
                        .setSystem("http://unitsofmeasure.org").setCode("ng/ml");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept().addCoding(new Coding("http://loinc.org", "2284-8", "Folate [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid folicAcid value: " + folicAcid);
            }
        }

        if (nonHdlCholesterol != null) {
            try {
                Observation observation = createObservation("nonHdlCholesterol");
                Quantity valueQuantity = new Quantity().setValue(Double.parseDouble(nonHdlCholesterol)).setUnit("mg/dl")
                        .setSystem("http://unitsofmeasure.org").setCode("mg/dl");

                observation.setValue(valueQuantity);
                observation.setCode(
                        new CodeableConcept()
                                .addCoding(new Coding("http://loinc.org", "43396-1",
                                        "Cholesterol non HDL [Mass/volume] in Serum or Plasma")));
                result.add(observation);
            } catch (Exception e) {
                logger.warn("Invalid nonHdlCholesterol value: " + nonHdlCholesterol);
            }
        }

        return result;
    }

    private Observation createObservation(String name) {
        Observation observation = new Observation();
        observation.setMeta(new Meta().addProfile(
                "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab"));
        Identifier analyseBefundCode = new Identifier()
                .setSystem("http://mii-standort.example.de/fhir/NamingSystem/pid")
                .setValue(name + "-" + now);
        Coding observationInstanceV2 = new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "MR",
                "Medical record number");
        analyseBefundCode.setType(new CodeableConcept().addCoding(observationInstanceV2));
        observation.addIdentifier(analyseBefundCode);
        observation.setStatus(Observation.ObservationStatus.FINAL);
        Coding loincObservation = new Coding("http://loinc.org", "58410-2", "CBC panel - Blood by Automated count");
        Coding observationCategory = new Coding("http://terminology.hl7.org/CodeSystem/observation-category",
                "laboratory", "Laboratory");
        observation
                .setCategory(List.of(new CodeableConcept().addCoding(loincObservation).addCoding(observationCategory)));
        observation.setSubject(new Reference().setIdentifier(new Identifier().setValue(patient)));
        if (date != null) {
            DateTimeType effective = new DateTimeType(Date.valueOf(date));
            observation.setEffective(effective);
        } else {
          //  observation.getEffectiveDateTimeType().addExtension(FHIRUtils.UNKNOWN_EXTENSION);
        }
        observation.setPerformer(List.of(new Reference().setIdentifier(new Identifier().setValue(practitioner))));
        return observation;
    }

}
