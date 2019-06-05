package com.summer.ddns.service.impl;

import com.summer.ddns.conf.DdnsProperties;
import com.summer.ddns.service.IpService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IpServiceImpl implements IpService {
    @Resource
    private DdnsProperties ddnsProperties;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public String getIp() {
        String[] urls = ddnsProperties.getIps();
        String ip = null;
        for (String url : urls) {
            try {
                String resText = restTemplate.getForObject(url, String.class);
                ip = matchIp(resText);
                if (ip != null) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ip;
    }

    private String matchIp(String text) {
        String regex = "\\d+\\.\\d+\\.\\d+\\.\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(text);
        boolean find = m.find();
        String ip = null;
        if (find) {
            ip = m.group(0);
            System.out.println("匹配到ip=" + ip);
        }
        return ip;
    }
}
