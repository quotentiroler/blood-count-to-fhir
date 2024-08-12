package com.api;

import ca.uhn.fhir.context.FhirContext;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.api.storage.StorageException;
import com.api.storage.StorageService;
import com.api.util.DataGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class BloodController {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(BloodController.class);

  private FhirContext fhirContext;
  private StorageService storageService;

  public BloodController(FhirContext fhirContext, StorageService storageService) {
    this.fhirContext = fhirContext;
    this.storageService = storageService;
  }

  @GetMapping("/")
  public ModelAndView get(ModelAndView model) throws IOException {
    storageService.init();
    storageService.deleteAll();

    //load files, not necessary because we delete all files
    model.addObject("files", storageService.loadAll().map(
        path -> {
          return MvcUriComponentsBuilder.fromMethodName(BloodController.class,
              "serveFile", path.getFileName().toString()).build().toUri().toString();
        })
        .filter((c) -> c != null)
        .collect(Collectors.toList()));
        
    model.setViewName("upload.html");
    return model;
  }

  @GetMapping("/files/{filename:.+}")
  @ResponseBody
  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

    Resource file = storageService.loadAsResource(filename);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }

  @PostMapping("/")
  public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes, ModelAndView model)
       {

    try {
      storageService.store(file);
    } catch (StorageException | InterruptedException e) {
      redirectAttributes.addFlashAttribute("message",
          "You failed to upload " + file.getOriginalFilename() + " => " + e.getMessage());
      model.setViewName("redirect:/");
      return model;
    }
    model.setViewName("redirect:/blood");
    return model;
  }

  @Operation(summary = "Convert BloodDetails POJOs to FHIR Bundle")
  @PostMapping("/blood")
  String toBundleString(@RequestBody BloodDetails bloodDetails) {
    Bundle bundle = new Bundle();
    bundle.setType(BundleType.COLLECTION);

    List<Observation> obs = bloodDetails.toObservations();
    logger.info("Mapped {} observations", obs.size());
    DiagnosticReport dr = bloodDetails.toReport(obs);
    bundle.addEntry().setResource(dr).setFullUrl("blood:tofhir:1");
    if (obs.isEmpty())
      return "No observations";
    int c = 2;
    for (Observation o : obs) {
      bundle.addEntry().setResource(o).setFullUrl("blood:tofhir:"+c++);
    }
    return fhirContext.newJsonParser().setPrettyPrint(false).encodeResourceToString(bundle);
  }

  @Operation(summary = "Test the GPT lib")
  @GetMapping("/chat")
  public String getChat(@RequestBody String message) throws IOException {
    Process process = AppUtils.startNLP(message);
    List<String> results = AppUtils.readProcessOutput(process.getInputStream());
    return results.toString();
  }

  @Operation(summary = "Run the app on a PDF file in ./upload-dir")
  @GetMapping("/blood")
  public String getBlood() throws IOException {
    //Use ExtractTables
    Process process = AppUtils.startOCR();
    List<String> table = AppUtils.readProcessOutput(process.getInputStream());
        String command = "Please convert this: "
        + table.toString()
        + "  into BloodDetails object with attributes:" +
        Files.readString(
            AppUtils.resolveResourcePath("command.txt"))
        + " formatted as json in the right order.";
    String result = getChat(command);
    result = AppUtils.fixJson(result);
    BloodDetails bloodDetails = new ObjectMapper().readValue(result, BloodDetails.class);
    storageService.deleteAll();
    return toBundleString(bloodDetails);
  }

  @Operation(summary = "Generate Test Data")
  @GetMapping("/generate")
  public void generateTestData() {
    DataGenerator dg = new DataGenerator(fhirContext);
    for (int i = 0; i < 100; i++) {
        dg.generateAll();
        dg.reset();
    }
    dg.convertHtmlToPdfInFolder();
  }

  @Operation(summary = "Test the API")
  @GetMapping("/test")
  public String test() throws IOException {
    String command = "Please convert this: "
        + Files.readString(
            AppUtils.resolveResourcePath("testinput.txt"))
        + "  into BloodDetails object with attributes:" +
        Files.readString(
            AppUtils.resolveResourcePath("command.txt"))
        + " formatted as json in the right order.";
    String result = getChat(command);
    result = AppUtils.fixJson(result);
    BloodDetails bloodDetails = new ObjectMapper().readValue(result, BloodDetails.class);
    return toBundleString(bloodDetails);
  }

}
