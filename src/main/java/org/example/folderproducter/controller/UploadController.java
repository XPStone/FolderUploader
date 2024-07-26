package org.example.folderproducter.controller;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.folderproducter.service.FastGptUploadService;
import org.example.folderproducter.util.FileUtil;
import org.example.folderproducter.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.aspose.words.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController // 特性不需要手动将返回对象转换为 JSON。Spring Boot 会使用 Jackson 等序列化库自动处理这一过程。
@RequestMapping("/api/upload") // 映射到 /api/upload 路径的控制器
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired // 自动注入 FastGptUploadService 服务
    private FastGptUploadService uploadService;

    /*
    * Handler:
    * 传入文件路径folderPath与数据库datasetId
    * 递归上传文件内容
    * */
    @PostMapping("/folder") // 映射到 /api/upload/folder 的 POST 请求
    public ResponseVO uploadFolder(@RequestParam String folderPath, @RequestParam String datasetId) throws IOException {
        File folder = new File(folderPath); // 获取文件夹路径
        uploadFolderRecursive(folder, datasetId, null); // 递归上传文件夹内容
        return new ResponseVO(200, "Upload completed");
    }

    /*
    * 用于递归上传文件夹中的内容
    * @folder：传入文件夹名
    * @datasetId：数据库名
    * @parentId，父文件夹名
    * */
    private void uploadFolderRecursive(File folder, String datasetId, String parentId) throws IOException {
        // 创建文件夹并获取其 ID
        logger.info("Creating folder: {}", folder.getAbsolutePath());
        String folderId = uploadService.createFolder(datasetId, parentId, folder.getName());

        // 获取当前文件夹中的所有word文件,并上传,如果后缀为doc则转换为docx
        File[] wordFiles = folder.listFiles((dir, name) -> name.endsWith(".docx") || name.endsWith(".doc"));

        if (wordFiles != null) {
            for (File wordFile : wordFiles) {
                // 1-如果
                if (wordFile.getName().endsWith(".doc")) {
                    // 将 .doc 文件转换为 .docx 文件
                    String inputFilePath = wordFile.getAbsolutePath();
                    String outputFilePath = inputFilePath.substring(0, inputFilePath.lastIndexOf('.')) + ".docx";
                    File convertedFile = new File(outputFilePath);

                    try {
                        // 使用 Aspose.Words 进行转换
                        Document doc = new Document(inputFilePath);
                        doc.save(outputFilePath);

                        // 删除原有的 .doc 文件
                        if (!wordFile.delete()) {
                            logger.warn("Failed to delete original .doc file: {}", wordFile.getAbsolutePath());
                        }

                        // 上传转换后的文件
                        logger.info("Uploading converted file: {}", convertedFile.getAbsolutePath());
                        uploadService.uploadFile(datasetId, folderId, convertedFile);
                    } catch (Exception e) {
                        logger.error("Failed to convert and upload .doc file: {}", wordFile.getAbsolutePath(), e);
                    }
                } else {
                    logger.info("Uploading file: {}", wordFile.getAbsolutePath());
                    uploadService.uploadFile(datasetId, folderId, wordFile);
                }
            }
        }

        // 获取所有子文件夹中的内容并上传
        File[] subFolders = folder.listFiles(File::isDirectory);
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                uploadFolderRecursive(subFolder, datasetId, folderId);
            }
        }
    }

//    //将docfile转换为docxfile
//    private File convertDocToDocx(File docFile) throws IOException {
//        try (FileInputStream fis = new FileInputStream(docFile);
//             HWPFDocument doc = new HWPFDocument(fis);
//             XWPFDocument docx = new XWPFDocument()) {
//
//            // 复制内容
//            Range range = doc.getRange();
//            docx.createParagraph().createRun().setText(range.text());
//
//            // 创建新的 .docx 文件
//            String docxFilePath = docFile.getAbsolutePath().replace(".doc", ".docx");
//            File docxFile = new File(docxFilePath);
//
//            try (FileOutputStream fos = new FileOutputStream(docxFile)) {
//                docx.write(fos);
//            }
//
//            // 删除原始 .doc 文件
//            if (!docFile.delete()) {
//                logger.warn("Failed to delete original .doc file: {}", docFile.getAbsolutePath());
//            }
//
//
//            return docxFile;
//        }
//    }
}
