package org.example.auto_web.pojo.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeFileResponse {
    private String fileName;
    private String fileContent;
    private String language;
    private Long fileSize;
}
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class CodeFileResponse {
//    private String fileName;
//    private String fileContent;
//    private String language;
//    private Long fileSize;
//    private Boolean success;
//    private String message;
//
//    // 成功响应的静态工厂方法
//    public static CodeFileResponse success(String fileName, String fileContent, String language) {
//        CodeFileResponse response = new CodeFileResponse();
//        response.setFileName(fileName);
//        response.setFileContent(fileContent);
//        response.setLanguage(language);
//        response.setFileSize((long) fileContent.getBytes(StandardCharsets.UTF_8).length);
//        response.setSuccess(true);
//        response.setMessage("生成成功");
//        return response;
//    }
//
//    // 错误响应的静态工厂方法
//    public static CodeFileResponse error(String message) {
//        CodeFileResponse response = new CodeFileResponse();
//        response.setSuccess(false);
//        response.setMessage(message);
//        return response;
//    }
//}