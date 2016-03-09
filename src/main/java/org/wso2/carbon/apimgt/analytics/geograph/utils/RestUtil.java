/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.analytics.geograph.utils;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.wso2.carbon.apimgt.analytics.geograph.api.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RestUtil {
    private static final RestUtil instance = new RestUtil();
    CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    Gson gson = new Gson();
    private static final Log log = LogFactory.getLog(RestUtil.class);
    private  static String restUrl;
    private RestUtil(){

    }

    public static RestUtil getInstance() {
        return instance;
    }
    public  void setRestUrl(String restUrl) {
        RestUtil.restUrl = restUrl;
    }
    public Location getLocation(String ip) {
        String apiInvocation = restUrl+"/location/" + ip;
        HttpUriRequest get = new HttpGet(apiInvocation);
        CloseableHttpResponse serviceResponse = null;
        BufferedReader bufferedReader = null;
        Location location = null;
        try {
            serviceResponse = httpclient.execute(get);
            bufferedReader = new BufferedReader(new InputStreamReader(serviceResponse.getEntity().getContent()));
            location = gson.fromJson(bufferedReader, Location.class);
            if (location == null) {
                location = new Location("", "");
            }
        } catch (IOException e) {
            log.error("Couldn't send rest request to url", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the buffers reader.", e);
                }
            }
            if (serviceResponse != null) {
                try {
                    serviceResponse.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the Response", e);
                }
            }
        }
        return location;
    }
}
