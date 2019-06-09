package com.summer.ddns.task;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.summer.ddns.conf.DdnsProperties;
import com.summer.ddns.service.IpService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Log4j2
public class MonitorTask {
    @Resource
    private DdnsProperties ddnsProperties;

    @Resource
    private IpService ipService;

    private DefaultAcsClient client;
    private DescribeSubDomainRecordsResponse.Record record;
    private String aliIp;

    @Scheduled(fixedDelay = 60000 * 30)
    public void a() {
        while (aliIp == null) {
            initAliIp();
        }
        monitor();
    }

    private void initAliIp() {
        log.info("开始获取云解析ip");
        try {
            aliIp = getAliIp();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        log.info("获取云解析ip=" + aliIp);
    }

    private void monitor() {
        log.info("执行监控服务开始");
        try {
            String localIp = ipService.getIp();
            if (!aliIp.equals(localIp)) {
                if (update(localIp)) {
                    aliIp = localIp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("执行监控服务结束");
    }

    private String getAliIp() throws ClientException {
        String regionId = ddnsProperties.getRegionId();
        String accessKeyId = ddnsProperties.getAccessKeyId();
        String secret = ddnsProperties.getSecret();
        String domainName = ddnsProperties.getDomainName();
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, secret);
        client = new DefaultAcsClient(profile);
        DescribeSubDomainRecordsRequest recordsRequest = new DescribeSubDomainRecordsRequest();
        recordsRequest.setSubDomain(domainName);
        DescribeSubDomainRecordsResponse recordsResponse = client.getAcsResponse(recordsRequest);
        List<DescribeSubDomainRecordsResponse.Record> list = recordsResponse.getDomainRecords();
        record = list.get(0);
        return record.getValue();
    }


    private boolean update(String ip) {
        UpdateDomainRecordRequest recordRequest = new UpdateDomainRecordRequest();
        recordRequest.setRecordId(record.getRecordId());
        recordRequest.setRR(record.getRR());
        recordRequest.setType(record.getType());
        recordRequest.setValue(ip);
        try {
            client.getAcsResponse(recordRequest);
            return true;
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}
