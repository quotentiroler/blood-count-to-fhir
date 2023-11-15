package com.api;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.util.Proxys;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BloodController {

  @Autowired
  FhirContext fhirContext;

  @PostMapping("/blood")
  String toBundle(@RequestBody BloodDetails bloodDetails) {
    Bundle bundle = new Bundle();
    bundle.setType(BundleType.COLLECTION);

    List<Observation> obs = bloodDetails.tObservations();
    DiagnosticReport dr = bloodDetails.toReport(obs);
    bundle.addEntry().setResource(dr);
    if (obs.isEmpty()) return "No observations";
    for (Observation o : obs) {
      bundle.addEntry().setResource(o);
    }
    IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
    return parser.encodeResourceToString(bundle);
  }

  @GetMapping("/blood")
  public String getBlood() throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(
      "python3",
      resolvePythonScriptPath("OCR.py")
    );
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();
    List<String> results = readProcessOutput(process.getInputStream());
    return results.toString();
  }

  @GetMapping("/chat")
  public String chat() {
    //Proxy proxy = Proxys.http("127.0.0.1", 8080);

    ChatGPT chatGPT = ChatGPT
      .builder()
      .apiKey("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")
      .apiHost("https://api.openai.com/") //反向代理地址
      .build()
      .init();

    Message system = Message.ofSystem("system");
    Message message = Message.of("message");
    ChatCompletion chatCompletion = ChatCompletion
      .builder()
      .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
      .messages(Arrays.asList(system, message))
      .maxTokens(3000)
      .temperature(0.9)
      .build();
    ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
    Message res = response.getChoices().get(0).getMessage();
    return res.toString();
  }

  private List<String> readProcessOutput(InputStream inputStream)
    throws IOException {
    try (
      BufferedReader output = new BufferedReader(
        new InputStreamReader(inputStream)
      )
    ) {
      return output.lines().collect(Collectors.toList());
    }
  }

  private String resolvePythonScriptPath(String filename) {
    File file = new File("src/main/resources/" + filename);
    return file.getAbsolutePath();
  }
}
