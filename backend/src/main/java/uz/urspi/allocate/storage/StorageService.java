package uz.urspi.allocate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uz.urspi.allocate.common.exception.BadRequestException;
import uz.urspi.allocate.config.StorageProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageProperties storageProperties;

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String uploadDir = storageProperties.getUploadDir();
            String datedSubDir = LocalDate.now().toString();
            Path targetDir = Paths.get(uploadDir, datedSubDir);
            Files.createDirectories(targetDir);

            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String storedFilename = UUID.randomUUID() + extension;

            Path targetPath = targetDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return Paths.get(datedSubDir, storedFilename).toString().replace("\\", "/");
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BadRequestException("Could not store file: " + e.getMessage());
        }
    }

    public void delete(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        try {
            Path path = Paths.get(storageProperties.getUploadDir(), relativePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete file {}", relativePath, e);
        }
    }
}
