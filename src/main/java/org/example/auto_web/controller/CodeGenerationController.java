package org.example.auto_web.controller;

import org.example.auto_web.pojo.dto.CodeGenerationRequest;
import org.example.auto_web.pojo.other.CodeFileResponse;
import org.example.auto_web.service.CodeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/code-generator")
public class CodeGenerationController {

    @Autowired
    private CodeGenerationService codeGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateCodeFile(@RequestBody CodeGenerationRequest request) {
        try {
            CodeFileResponse response = codeGenerationService.generateCodeFile(request);

            // 设置文件下载头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", response.getFileName());
            headers.add("X-File-Name", response.getFileName());
            headers.add("X-File-Size", response.getFileSize().toString());
            headers.add("X-Language", response.getLanguage());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response.getFileContent().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("生成代码文件失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
    @PostMapping("/preview")
    public ResponseEntity<byte[]> previewCodeFile(@RequestBody CodeGenerationRequest request) {
        try {
            CodeFileResponse response = codeGenerationService.generateCodeFile(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(response.getFileContent().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("生成代码文件失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/supported-languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        return ResponseEntity.ok(codeGenerationService.getSupportedLanguages());
    }
}