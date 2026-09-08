package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceRegisterForm {
    private Integer user_id;
    private MultipartFile id_card_file;
    private MultipartFile live_file;
}
