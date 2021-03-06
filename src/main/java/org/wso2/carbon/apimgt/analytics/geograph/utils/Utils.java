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

import org.wso2.carbon.apimgt.analytics.geograph.impl.LRUCache;
import org.wso2.carbon.apimgt.analytics.geograph.holders.CacheHolder;

public class Utils {
        public static final LRUCache<String,Long> cache = CacheHolder.getInstance().getIpToLongCache();
    public static long getIpV4ToLong(String ipAddress) {
        Long ipToLong = cache.get(ipAddress);
        if (ipToLong == null){
            String[] ipAddressInArray = ipAddress.split("\\.");
            long result = 0;
            int i = 0;
            for (String ipChunk : ipAddressInArray) {
                int power = 3 - i;
                int ip = Integer.parseInt(ipChunk);
                result += ip * Math.pow(256, power);
                i++;
            }
            cache.put(ipAddress,result);
            ipToLong = result;
        }
        return ipToLong;
    }
}
