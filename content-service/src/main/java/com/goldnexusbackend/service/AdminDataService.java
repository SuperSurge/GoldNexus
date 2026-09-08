package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.Data;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.AdminDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDataService {
    private final AdminDataMapper adminDataMapper;
    Res res = new Res();

    @Transactional
    public Res getData(){
        log.info("获取数据总览申请");

        Data data = new Data();
        data.setUserCount(adminDataMapper.userCount());
        data.setLoanProductCount(adminDataMapper.loanProductCount());
        data.setLoanApplicationCount(adminDataMapper.loanApplicationCount());
        data.setToBeCheckedApplicationCount(adminDataMapper.toBeCheckedApplicationCount());

        res.setCode(200);
        res.setMsg("数据总览获取成功");
        log.info("");
        res.setData(data);
        return res;

    }
}
