package com.summer.ddns.service.impl;

import com.summer.ddns.conf.DdnsProperties;
import com.summer.ddns.service.IpService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
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
                log.info("开始获取ip,url" + url);
                String resText = restTemplate.getForObject(url, String.class);
                log.info("请求返回" + resText);
                ip = matchIp(resText);
                log.info("解析到Ip" + ip);
                if (ip != null) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取ip异常,url=" + url, e);
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
        }
        return ip;
    }
}
