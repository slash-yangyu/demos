package com.keesail.order.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.*;

/**
 * @ClassName : ImageReview
 * @Description :鉴黄
 * @Author : yock
 * @Date: 2020/8/18
 */
public class ImageReview {
    /**
     * @param
     * @return pass-放行， forbid-封禁， check-人工审核
     * @throws Exception
     */
    public static void main(String[] args) {
        String ucloudUrl = "http://api.uai.ucloud.cn/v1/image/scan";
        String appId = "uaicensor-lzblmnyp";
        String uaicensorPublicKey = "IK93jFF24Ty4kIkti0RLJMBo0k9W0k3bX16Z1wZJOy";
        String uaicensorPrivateKey = "1RP9P4Eqm1I1lfZgtbUCugclbKyt9B5tBF9NKGtkdVdHmQ1nohqmsn98GTEYivv13";
        String urlAdd = "";
        //图片绝对路径
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        /**
         * 生成signature，首字母排序
         */
        String timestamp = System.currentTimeMillis() + "";
        SortedMap<Object, Object> packageParams = new TreeMap<>();
        packageParams.put("PublicKey", uaicensorPublicKey);
        packageParams.put("ResourceId", appId);
        packageParams.put("Timestamp", timestamp);
        packageParams.put("Url", urlAdd);
        String signature = null;
        try {
            signature = createSign(packageParams, uaicensorPrivateKey);
        } catch (Exception e) {
        }
        /**
         * 参数
         */
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("Scenes", "porn");
        param.add("Method", "url");
        param.add("Url", urlAdd);
        /**
         * headers 参数
         */
        headers.setContentType(MediaType.parseMediaType("multipart/form-data; charset=UTF-8"));
        headers.set("PublicKey", uaicensorPublicKey);
        headers.set("Signature", signature);
        headers.set("ResourceId", appId);
        headers.set("Timestamp", timestamp);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        ResponseEntity<String> responseEntity = rest.exchange(ucloudUrl, HttpMethod.POST, httpEntity, String.class);
        String body = responseEntity.getBody();
        System.err.println(body);
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.getInteger("RetCode") == 0) {
            String res = jsonObject.getJSONObject("Result").getJSONObject("Porn").getString("Suggestion");
        }
    }
    public static String createSign(SortedMap<Object, Object> packageParams,
                                    String privateKey) throws Exception {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v =  (String)entry.getValue();
            sb.append(k + v);
        }
        sb.append(privateKey);
        String sign = shaEncode(sb.toString());
        return sign;
    }
    public static String shaEncode(String inStr) throws Exception {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }

        byte[] byteArray = inStr.getBytes("UTF-8");
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}
