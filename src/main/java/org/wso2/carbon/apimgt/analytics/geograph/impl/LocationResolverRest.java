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
package org.wso2.carbon.apimgt.analytics.geograph.impl;

import org.wso2.carbon.apimgt.analytics.geograph.api.Location;
import org.wso2.carbon.apimgt.analytics.geograph.api.LocationResolver;
import org.wso2.carbon.apimgt.analytics.geograph.holders.CacheHolder;
import org.wso2.carbon.apimgt.analytics.geograph.utils.RestUtil;

public class LocationResolverRest implements LocationResolver {
   private static boolean cacheEnabled;
   private static LRUCache<String, Location> cache;
    private static RestUtil restUtil;
    public String getCountry(String ip) {
        Location location = null;
        cacheEnabled = CacheHolder.getInstance().isCacheEnabled();
        restUtil = RestUtil.getInstance();
        if (cacheEnabled){
            cache = CacheHolder.getInstance().getIpResolveCache();
            location = cache.get(ip);
        }
        if (location == null) {
            location = restUtil.getLocation(ip);
            if (cacheEnabled){
                cache.put(ip, location);
            }
        }
        return location.getCountry();
    }

    public String getCity(String ip) {
        Location location = null;
        cacheEnabled = CacheHolder.getInstance().isCacheEnabled();
        restUtil = RestUtil.getInstance();
        if (cacheEnabled){
            cache = CacheHolder.getInstance().getIpResolveCache();
            location = cache.get(ip);
        }
        if (location == null) {
            location = restUtil.getLocation(ip);
            if (cacheEnabled){
                cache.put(ip, location);
            }
        }
        return location.getCity();
    }

    public LocationResolverRest() {

    }
}
