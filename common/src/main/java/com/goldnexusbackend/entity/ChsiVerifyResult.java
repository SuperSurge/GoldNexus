package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChsiVerifyResult {
    /** 是否认证通过 */
    private boolean valid;
    /** 姓名 */
    private String name;
    /** 毕业院校 */
    private String school;
    /** 专业 */
    private String major;
    /** 学历层次（如 本科/硕士/博士） */
    private String educationLevel;
    /** 毕业日期 */
    private LocalDate graduateDate;
}
