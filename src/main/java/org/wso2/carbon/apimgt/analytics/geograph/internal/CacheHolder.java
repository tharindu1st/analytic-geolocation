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
package org.wso2.carbon.apimgt.analytics.geograph.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.analytics.geograph.api.Location;
import org.wso2.carbon.apimgt.analytics.geograph.api.LocationResolver;
import org.wso2.carbon.apimgt.analytics.geograph.api.LocationResolverConstants;
import org.wso2.carbon.apimgt.analytics.geograph.exception.GeoLocationResolverException;
import org.wso2.carbon.apimgt.analytics.geograph.impl.LRUCache;
import org.wso2.carbon.apimgt.analytics.geograph.utils.DBUtil;
import org.wso2.carbon.apimgt.analytics.geograph.utils.RestUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.xml.StringUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CacheHolder {

    private static final CacheHolder instance = new CacheHolder();
    private static final LRUCache<String, Location> ipResolveCache = new LRUCache<String, Location>(1000);
    private static final LRUCache<String, Long> ipToLongCache = new LRUCache<String, Long>(1000);
    private static LocationResolver locationResolver;
    private String clazzName;
    private String dataSourceName;
    private static final Log log = LogFactory.getLog(CacheHolder.class);

    private CacheHolder() {
        InputStream in;
        String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "etc" + File.separator + "geolocation.xml";
        try {
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            String type = builder.getDocumentElement().getFirstChildWithName(new QName(LocationResolverConstants.Type))
                    .getText();
            clazzName = builder.getDocumentElement().getFirstChildWithName(new QName(LocationResolverConstants
                    .ImplClass))
                    .getText();
            dataSourceName = builder.getDocumentElement().getFirstChildWithName(new QName(LocationResolverConstants
                    .DataSource))
                    .getText();
            String restUrl = builder.getDocumentElement().getFirstChildWithName(new QName(LocationResolverConstants
                    .RestUrl))
                    .getText();
            if (!StringUtils.isEmpty(clazzName)) {
                locationResolver = (LocationResolver) Class.forName(clazzName).newInstance();
            }
            if (LocationResolverConstants.Types.Type_RDBMS.equals(type)) {
                if (!StringUtils.isEmpty(dataSourceName)) {
                    DBUtil.getInstance().setDataSourceName(dataSourceName);
                    DBUtil.initialize();
                } else if (LocationResolverConstants.Types.Type_REST.equals(type)) {
                    if (!StringUtils.isEmpty(restUrl)) {
                        RestUtil.getInstance().setRestUrl(restUrl);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Couldn't find the geolocation.xml in " + filePath, e);
        } catch (XMLStreamException e) {
            log.error("Couldn't read the geolocation.xml in " + filePath, e);
        } catch (ClassNotFoundException e) {
            log.error("Couldn't found Location Implementation from " + clazzName + " class.", e);
        } catch (GeoLocationResolverException e) {
            log.error("Couldn't initialize the data source from " + dataSourceName, e);
        } catch (IllegalAccessException e) {
            log.error("Couldn't access the Location Implementation from " + clazzName + " class.", e);
        } catch (InstantiationException e) {
            log.error("Couldn't initialize the Location Implementation from " + clazzName + " class.", e);
        }
    }

    public static CacheHolder getInstance() {
        return instance;
    }

    public LRUCache<String, Location> getIpResolveCache() {
        return ipResolveCache;
    }

    public LRUCache<String, Long> getIpToLongCache() {
        return ipToLongCache;
    }

    public LocationResolver getLocationResolver() {
        return locationResolver;
    }
}
