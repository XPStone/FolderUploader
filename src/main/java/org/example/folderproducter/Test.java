package org.example.folderproducter;
import com.aspose.words.*;

public class Test {
    public static void main(String[] args) {
        try {
            // 指定输入和输出文件的路径
            String inputFilePath = "C:\\Users\\xxxs20243\\Desktop\\LocalFatsGPT\\PCS-9563_500kW_变更说明.doc";
            String outputFilePath = "C:\\Users\\xxxs20243\\Desktop\\LocalFatsGPT\\PCS-9563_500kW_变更说明.docx";

            // 加载输入的 DOC 文件
            Document doc = new Document(inputFilePath);

            // 保存为 DOCX 格式
            doc.save(outputFilePath);

            System.out.println("DOC 文件已成功转换为 DOCX 文件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
