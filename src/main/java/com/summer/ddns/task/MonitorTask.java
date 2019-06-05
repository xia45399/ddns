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
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class MonitorTask {
    @Resource
    private ApplicationContext appContext;
    @Resource
    private DdnsProperties ddnsProperties;

    private DefaultAcsClient client;
    private DescribeSubDomainRecordsResponse.Record record;
    private String aliIp;

    @Scheduled(fixedDelay = 60000 * 30)
    public void a() {
        if (aliIp == null) {
            initAliIp();
        }
        monitor();
    }

    private void initAliIp() {
        System.out.println("开始获取云解析ip");
        while (aliIp == null) {
            try {
                aliIp = getAliIp();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
        System.out.println("获取云解析ip=" + aliIp);
    }

    private void monitor() {
        System.out.println("执行监控服务开始");
        try {
            String localIp = getLocalIp();
            if (!localIp.equals(aliIp)) {
                if (update(localIp)) {
                    aliIp = localIp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行监控服务结束");
    }

    private String getLocalIp() {
        Map<String, IpService> ipServiceMap = appContext.getBeansOfType(IpService.class);
        Collection<IpService> list = ipServiceMap.values();
        for (IpService service : list) {
            try {
                return service.getIp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
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
