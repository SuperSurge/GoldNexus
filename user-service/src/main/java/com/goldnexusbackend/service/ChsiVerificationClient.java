package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.ChsiVerifyResult;

public interface ChsiVerificationClient {
    ChsiVerifyResult verifyByCode(String onlineCode);
}
