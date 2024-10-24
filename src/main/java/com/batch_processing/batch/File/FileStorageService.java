package com.batch_processing.batch.File;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Service

public class FileStorageService {

    @Value("${application.file.uploads.csv-file-path}")
    private String fileUploadPath;

    public String saveFile(
            @NotNull MultipartFile sourceFile,
            @NotNull String TableName

    ) {

        final String fileUploadSubPath = "Data" + File.separator + TableName;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(@NotNull MultipartFile sourceFile, @NotNull String fileUploadSubPath) {
        final String finalUploadPath = fileUploadSubPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                log.warn("Folder Not created");
                return null;
            }
        }

        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "." + fileExtension;
        Path targePath = Paths.get(targetFilePath);
        try {
            Files.write(targePath, sourceFile.getBytes());
            log.info("file saved at " + targetFilePath);
            return targetFilePath;

        } catch (IOException e) {
            log.error("File was not saved", e);
        }
        return null;

    };

    private String getFileExtension(String fileName) {

        if (fileName == null || fileName.isEmpty()) {
            return "";

        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();

    };

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }

}
