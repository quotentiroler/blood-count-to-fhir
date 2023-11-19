package com.api;

import ca.uhn.fhir.context.FhirContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
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
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class BloodController {

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

  @PostMapping("/blood")
  String toBundleString(@RequestBody BloodDetails bloodDetails) {
    Bundle bundle = new Bundle();
    bundle.setType(BundleType.COLLECTION);

    List<Observation> obs = bloodDetails.tObservations();
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

  @GetMapping("/chat")
  public String getChat(@RequestBody String message) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "python3",
        resolveResourcePathToString("GPT4FREE.py"),
        message);
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();
    List<String> results = readProcessOutput(process.getInputStream());
    return results.toString();
  }

  @GetMapping("/blood")
  public String getBlood() throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "python3",
        resolveResourcePathToString("OCR.py"));
    // processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();
    List<String> table = readProcessOutput(process.getInputStream());
    process.destroy();

    String command = "Please convert this: "
        + table.toString()
        + "  into BloodDetails object with attributes:" +
        Files.readString(
            resolveResourcePath("command.txt"))
        + " formatted as json in the right order. Please mind that \"Blutzucker\" means glucose.";

    String result = getChat(command);
    result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
    result = fixJson(result);
    BloodDetails bloodDetails = new ObjectMapper().readValue(result, BloodDetails.class);
    storageService.deleteAll();
    return toBundleString(bloodDetails);
  }

  @GetMapping("/test")
  public String test() throws IOException {

    String command = "Please convert this: "
        + Files.readString(
            resolveResourcePath("testinput.txt"))
        + "  into BloodDetails object with attributes:" +
        Files.readString(
            resolveResourcePath("command.txt"))
        + " formatted as valid json in the right order.";

    ProcessBuilder processBuilder = new ProcessBuilder(
        "python3",
        resolveResourcePathToString("GPT4FREE.py"),
        command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    List<String> table = readProcessOutput(process.getInputStream());
    process.destroy();
    String result = table.toString();
    result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
    result = fixJson(result);
    BloodDetails bloodDetails = new ObjectMapper().readValue(result, BloodDetails.class);
    return toBundleString(bloodDetails);
  }

  private String fixJson(String json) {
    String trimmedJson = json.replaceAll("\\s+", "");
    String first = IntStream.range(0, trimmedJson.length())
        .filter(i -> trimmedJson.charAt(i) != ',' || trimmedJson.charAt(i + 1) == '\"')
        .mapToObj(i -> Character.toString(trimmedJson.charAt(i)))
        .collect(Collectors.joining(""));
    String second = IntStream.range(0, first.length())
        .filter(i -> first.charAt(i) != ',' || first.charAt(i - 1) == '\"' || first.charAt(i - 1) == ']' || Character.isDigit(first.charAt(i - 1)))
        .mapToObj(i -> Character.toString(first.charAt(i)))
        .collect(Collectors.joining(""));
    return second.replace("null", "\"\",");
  }

  private List<String> readProcessOutput(InputStream inputStream)
      throws IOException {
    try (
        BufferedReader output = new BufferedReader(
            new InputStreamReader(inputStream))) {
      return output.lines().collect(Collectors.toList());
    }
  }

  private Path resolveResourcePath(String filename) {
    File file = new File("src/main/resources/" + filename);
    return file.toPath();
  }

  private String resolveResourcePathToString(String filename) {
    File file = new File("src/main/resources/" + filename);
    return file.getAbsolutePath();
  }
}
