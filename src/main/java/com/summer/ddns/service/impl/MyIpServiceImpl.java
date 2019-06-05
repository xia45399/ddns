package com.summer.ddns.service.impl;

import com.summer.ddns.service.IpService;
import com.summer.ddns.util.HttpUtil;
import org.springframework.stereotype.Service;

@Service
public class MyIpServiceImpl implements IpService {

    @Override
    public String getIp() throws Exception {
        String url = "";
        String result = HttpUtil.get(url);
        System.out.println("获取本机公网ip " + result);
        return result;
    }
}
