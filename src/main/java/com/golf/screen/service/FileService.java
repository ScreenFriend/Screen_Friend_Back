package com.golf.screen.service;

import com.golf.screen.error.CustomException;
import com.golf.screen.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 파일을 로컬 저장소에 저장하고 접근 가능한 웹 URL 경로를 반환합니다.
     */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 업로드 폴더 생성
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일명 중복을 방지하기 위한 UUID 처리
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFilename = UUID.randomUUID().toString() + extension;

            // 로컬 파일 저장
            Path targetLocation = uploadPath.resolve(savedFilename);
            Files.copy(file.getInputStream(), targetLocation);

            // 웹으로 리다이렉트 가능한 상대경로 반환
            return "/uploads/" + savedFilename;

        } catch (IOException ex) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 로컬 저장소에 있는 기존 프로필 파일을 삭제합니다.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }

        try {
            String filename = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadDir).resolve(filename).toAbsolutePath();

            File file = filePath.toFile();
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ex) {
            // 삭제 실패 예외는 서비스 흐름을 끊지 않기 위해 로그만 찍거나 무시할 수 있습니다.
            System.err.println("파일 삭제 실패: " + fileUrl + " - " + ex.getMessage());
        }
    }
}
