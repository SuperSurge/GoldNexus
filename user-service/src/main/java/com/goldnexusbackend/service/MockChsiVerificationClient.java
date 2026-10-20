package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.ChsiVerifyResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "chsi.verify.mock", havingValue = "true", matchIfMissing = true)
public class MockChsiVerificationClient implements ChsiVerificationClient {

    @Override
    public ChsiVerifyResult verifyByCode(String onlineCode) {
        ChsiVerifyResult result = new ChsiVerifyResult();
        result.setValid(true);
        // 姓名/学校/专业/学历当前无法从 PDF 识别，不填虚拟值
        result.setGraduateDate(LocalDate.now().minusMonths(3)); // 毕业日期用于应届年限判断，模拟刚毕业
        return result;
    }
}
