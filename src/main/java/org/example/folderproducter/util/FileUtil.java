package org.example.folderproducter.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    // 列出目录中所有指定扩展名的文件
    public static List<File> listAllFiles(File folder, String[] extensions) {
        List<File> result = new ArrayList<>();
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    result.addAll(listAllFiles(file, extensions));
                } else {
                    for (String ext : extensions) {
                        if (file.getName().endsWith(ext)) {
                            result.add(file);
                        }
                    }
                }
            }
        }
        return result;
    }

    // 列出目录中所有子文件夹
    public static List<File> listAllFolders(File folder) {
        List<File> result = new ArrayList<>();
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    result.add(file);
                    result.addAll(listAllFolders(file));
                }
            }
        }
        return result;
    }
}
