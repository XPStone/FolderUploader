package org.example.folderproducter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FastGptUploadService {

    // 获取fastgpt的URL
    @Value("${fastgpt.api.url}")
    private String apiUrl;

    // 获取API
    @Value("${fastgpt.api.token}")
    private String apiCookie;

    // 用于发送HTTP请求的模板：RestTemplate可以在返回时把json转换为Map
    @Autowired
    private RestTemplate restTemplate;

    /*
    * 创建文件夹并返回其ID
    * */
    public String createFolder(String datasetId, String parentId, String name) {
        String url = apiUrl + "/api/core/dataset/collection/create";
        // 创建Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", apiCookie);

        // 创建Body请求体
        Map<String, Object> body = new HashMap<>();
        body.put("datasetId", datasetId);
        body.put("parentId", parentId);
        body.put("name", name);
        body.put("type", "folder");
        body.put("metadata", new HashMap<>()); // 空的元数据

        // 创建POST类型的HTTP请求，发送POST请求并接受响应
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // 如果响应码是200，返回id
        if (response.getStatusCodeValue() == 200 && response.getBody() != null) {
            return (String) response.getBody().get("data");
        }
        return null;
    }

    /*
    * 上传文件到指定文件夹
    * 数据库：datasetId
    * 父文件夹：parentId
    * */
    // 上传文件到指定的文件夹
    public String uploadFile(String datasetId, String parentId, File file) throws IOException {
        String url = apiUrl + "/api/core/dataset/collection/create/localFile"; // FastGPT上传文件的API URL

        // 创建Http头：包括类型和认证信息
        HttpHeaders headers = new HttpHeaders(); // 创建HTTP头
        headers.setContentType(MediaType.MULTIPART_FORM_DATA); // 设置头的内容类型为MULTIPART
        headers.set("Cookie", apiCookie); // 设置头的认证信息

        //创建文件资源部分file
        FileSystemResource resource = new FileSystemResource(file) {
            // 重写getnameFile方法防止出现乱码
            @Override
            public String getFilename() {
                try {
                    // 使用URL编码来确保文件名不会出现乱码
                    return URLEncoder.encode(super.getFilename(), StandardCharsets.UTF_8.toString());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to encode filename", e);
                }
            }
        };


        // 创建请求体数据部分data
        Map<String, Object> data = new HashMap<>();
        data.put("datasetId", datasetId); // 设置数据集ID
        data.put("parentId", parentId); // 设置父文件夹ID
        data.put("trainingType", "chunk"); // 设置训练类型
        data.put("chunkSize", 512); // 设置chunk大小
        data.put("chunkSplitter", ""); // 设置chunk分割器
        data.put("qaPrompt", ""); // 设置qa提示
        data.put("metadata", new HashMap<>()); // 设置空的元数据

        // 创建多部分请求体file与data作为body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource); // 添加文件部分
        body.add("data", new HttpEntity<>(data)); // 添加数据部分

        // 创建HTTP请求
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        // 发送POST请求并接收响应
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody(); // 返回响应体
    }
}

