package org.example.auto_web.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.auto_web.pojo.other.OperationStep;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteRequest {
    private String url; // 目标网址
    private List<OperationStep> steps; // 操作步骤列表
}