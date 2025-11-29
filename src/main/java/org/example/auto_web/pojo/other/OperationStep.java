package org.example.auto_web.pojo.other;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.auto_web.pojo.enums.OperationType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationStep {
    private OperationType type;
    private String xpath;
    private String value;
    private Integer startIndex;
    private Integer endIndex;
    private Integer iterations;
    private List<OperationStep> subSteps;
    private Map<String, Object> parameters;

    private Long waitBeforeMs;  // 操作前等待时间（毫秒）
    private Long waitAfterMs;   // 操作后等待时间（毫秒）

    private String remark;      // 步骤备注

    private Boolean acceptAlert; // 是否接受弹窗
    private String alertText;    // 弹窗输入文本

    private String filePath;    // 文件路径,用于导入Cookie和保存文本

//    private String placeholder; // 自定义替代符，如 {i}, {index} 等
    private Integer increment;  // 替代符每次增量，默认为1
}