/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.streaming.service;

import com.stratio.streaming.commons.constants.ColumnType;
import com.stratio.streaming.commons.messages.ColumnNameTypeValue;
import com.stratio.streaming.commons.messages.StratioStreamingMessage;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SolrOperationsService {

    private static final Logger log = LoggerFactory.getLogger(SolrOperationsService.class);

    private String solrHosts;

    private String dataDir;

    private Boolean isCloud;

    public SolrOperationsService(String solrHosts, String dataDir, Boolean isCloud) {
        this.solrHosts = solrHosts;
        this.dataDir = dataDir;
        this.isCloud = isCloud;
    }

    public void createCore(StratioStreamingMessage message) throws IOException, URISyntaxException, SolrServerException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
        String core = message.getStreamName();
        String dataPath = this.dataDir + '/' + core + "/data";
        String confPath = this.dataDir + '/' + core + "/conf";
        createDirs(dataPath, confPath);
        createSolrConfig(confPath);
        createSolrSchema(message.getColumns(), confPath);
        SolrClient solrClient = getSolrclient(core);
        CoreAdminRequest.Create createCore = new CoreAdminRequest.Create();
        createCore.setDataDir(dataPath);
        createCore.setInstanceDir(dataDir + '/' + core);
        createCore.setCoreName(core);
        createCore.setSchemaName("schema.xml");
        createCore.setConfigName("solrconfig.xml");
        if (solrClient instanceof CloudSolrClient) {
            ((CloudSolrClient)solrClient).uploadConfig(Paths.get(confPath), core);
        }
        solrClient.request(createCore);
    }

    public void createDirs(String dataPath, String confPath) {
        File dataDir = new File(dataPath);
        File dir = new File(this.dataDir);
        File confDir = new File(confPath);
        // Comprobar resultado de esta operación
        dir.mkdirs();
        dataDir.mkdirs();
        confDir.mkdirs();
    }

    public void createSolrConfig(String confPath) throws URISyntaxException, IOException {
        FileUtils.copyFile(new File(ClassLoader.getSystemResource("./solr-config/solrconfig.xml").toURI()), new File(confPath + "/solrconfig.xml"));
    }

    public void createSolrSchema(List<ColumnNameTypeValue> columns, String confpath) throws ParserConfigurationException, URISyntaxException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new File(ClassLoader.getSystemResource("./solr-config/schema.xml").toURI()));
        NodeList nodes = doc.getElementsByTagName("schema");
        for (ColumnNameTypeValue column: columns) {
            Element field = doc.createElement("field");
            field.setAttribute("name", column.getColumn());
            field.setAttribute("type", streamingToSolr(column.getType()));
            field.setAttribute("indexed", "true");
            field.setAttribute("stored", "true");
            nodes.item(0).appendChild(field);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult streamResult =  new StreamResult(new File(confpath+"/schema.xml"));
        transformer.transform(source, streamResult);
    }

    private String streamingToSolr(ColumnType column) {
            switch (column) {
                case BOOLEAN:
                    return "boolean";
                case DOUBLE:
                    return "double";
                case FLOAT:
                    return "float";
                case INTEGER:
                    return "int";
                case LONG:
                    return "long";
                case STRING:
                    return "string";
                default:
                    throw new RuntimeException("Unsupported Column type");
            }
    }

    public List<String> getCoreList() throws IOException, SolrServerException {
        SolrClient solrClient = getSolrclient(null);
        CoreAdminRequest coreAdminRequest = new CoreAdminRequest();
        coreAdminRequest.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = coreAdminRequest.process(solrClient);

        List<String> coreList = new ArrayList<String>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            coreList.add(cores.getCoreStatus().getName(i));
        }
        return coreList;
    }

    private SolrClient getSolrclient(String core) {
        SolrClient solrClient;
        if (isCloud) {
            solrClient = new CloudSolrClient(solrHosts);
        } else {
            solrClient = new HttpSolrClient("http://" + solrHosts + "/solr");
        }
        return solrClient;
    }

}
