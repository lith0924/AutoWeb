package org.example.auto_web.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.auto_web.pojo.other.OperationStep;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeGenerationRequest {
    private String language; // "java", "python", "cpp"
    private List<OperationStep> steps;
    private String initialUrl;
    private String className; // 生成的类名
    private Boolean includeComments = true; // 是否包含注释
}
