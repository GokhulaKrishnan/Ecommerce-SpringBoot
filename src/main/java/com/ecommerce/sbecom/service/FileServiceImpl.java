package com.ecommerce.sbecom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    // Helper function
    public String uploadImage(String path, MultipartFile file) throws IOException {

        // Get the original filename from the given file
        String originalFilename = file.getOriginalFilename();

        // Creating a unique file name using UUID
        String uniqueFileName = UUID.randomUUID().toString();

        // Creating a new file name by adding the extension of original file name to the unique name
        String fileName = uniqueFileName.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));

        // Creating the filePath
        String filePath = path + File.separator + fileName;

        // Now we are going to create the destination folder if not exist
        // Here we are using the given path
        File folder = new File(path);

        // If not exist creating a new folder
        if(!folder.exists()){
            folder.mkdirs();
        }

        // Now copying the given file into the destination folder
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }
}
