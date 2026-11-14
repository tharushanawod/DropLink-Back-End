
package com.dual.dev.account_service.storage;

import com.dual.dev.account_service.common.exception.BadRequestException;
import com.dual.dev.account_service.config.S3StorageProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverLicenseStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final S3Client s3Client;
    private final S3StorageProperties properties;


    public UploadedLicense upload(UUID driverId, MultipartFile file) {

        validateFile(file);

        FileType fileType = detectFileType(file);

        String fileName = createSafeFileName(
                file.getOriginalFilename(),
                fileType.extension
        );

        String objectKey = "drivers/" + driverId
                + "/license/" + UUID.randomUUID()
                + "." + fileType.extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(fileType.contentType)
                .contentLength(file.getSize())
                .build();

        try (InputStream inputStream = file.getInputStream()) {

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );

        } catch (IOException | SdkException exception) {

            throw new StorageOperationException(
                    "Unable to upload driver licence document.",
                    exception
            );
        }

        return new UploadedLicense(
                objectKey,
                fileName,
                fileType.contentType,
                file.getSize()
        );
    }


    public void delete(String objectKey) {

        if (objectKey == null || objectKey.isBlank()) {
            return;
        }

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .build()
        );
    }


    public void deleteQuietly(String objectKey) {

        try {
            delete(objectKey);
        } catch (RuntimeException exception) {
            log.warn("Could not clean up stored licence object.", exception);
        }
    }


    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Please select a licence document to upload."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "Licence document must not exceed 5 MB."
            );
        }
    }


    private FileType detectFileType(MultipartFile file) {

        byte[] header;

        try (InputStream inputStream = file.getInputStream()) {
            header = inputStream.readNBytes(8);
        } catch (IOException exception) {
            throw new StorageOperationException(
                    "Unable to read uploaded file.",
                    exception
            );
        }

        FileType detectedType;

        // PDF signature: %PDF-
        if (header.length >= 5
                && header[0] == '%'
                && header[1] == 'P'
                && header[2] == 'D'
                && header[3] == 'F'
                && header[4] == '-') {

            detectedType = new FileType("pdf", "application/pdf");

            // PNG signature
        } else if (header.length >= 8
                && header[0] == (byte) 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A) {

            detectedType = new FileType("png", "image/png");

            // JPEG signature
        } else if (header.length >= 3
                && header[0] == (byte) 0xFF
                && header[1] == (byte) 0xD8
                && header[2] == (byte) 0xFF) {

            detectedType = new FileType("jpg", "image/jpeg");

        } else {

            throw new BadRequestException(
                    "Only PDF, PNG and JPEG documents are supported."
            );
        }

        // Do not trust the browser-provided content type.
        String declaredType = file.getContentType();

        if (declaredType != null
                && !declaredType.equalsIgnoreCase("application/octet-stream")
                && !declaredType.equalsIgnoreCase(detectedType.contentType)) {

            throw new BadRequestException(
                    "The uploaded file content does not match its declared type."
            );
        }

        return detectedType;
    }


    private String createSafeFileName(String originalName, String extension) {

        if (originalName == null || originalName.isBlank()) {
            return "license." + extension;
        }

        // Remove any directory information from the supplied name.
        String name = originalName.replace('\\', '/');

        name = name.substring(name.lastIndexOf('/') + 1);

        // Remove control characters and unsafe filename characters.
        name = name.replaceAll("[\\p{Cntrl}]", "");
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");

        // Use the detected extension, not the supplied extension.
        int extensionIndex = name.lastIndexOf('.');

        if (extensionIndex > 0) {
            name = name.substring(0, extensionIndex);
        }

        if (name.isBlank()) {
            name = "license";
        }

        if (name.length() > 240) {
            name = name.substring(0, 240);
        }

        return name + "." + extension;
    }


    private record FileType(
            String extension,
            String contentType
    ) {
    }


    public record UploadedLicense(
            String objectKey,
            String fileName,
            String contentType,
            long size
    ) {
    }
}
