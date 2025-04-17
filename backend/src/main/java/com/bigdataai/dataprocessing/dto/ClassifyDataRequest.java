package com.bigdataai.dataprocessing.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 数据分类请求 DTO
 */
public class ClassifyDataRequest {

    @NotEmpty(message = "特征列表不能为空")
    private List<String> features;

    @NotNull(message = "参数映射不能为空")
    private Map<String, Object> params;

    // Getters and Setters
    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}