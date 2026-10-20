package com.goldnexusbackend.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /** 上传文件到 MinIO，返回对象名（存 MySQL 用这个） */
    public String upload(MultipartFile file, String dir) {
        try {
            String ext = getExt(file.getOriginalFilename());
            String objectName = dir + "/" + UUID.randomUUID() + ext;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return objectName;
        } catch (Exception e) {
            log.error("上传文件到 MinIO 失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /** 从 MinIO 下载文件 */
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("从 MinIO 下载文件失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    private String getExt(String filename) {
        int i = filename == null ? -1 : filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : "";
    }
}
