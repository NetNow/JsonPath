package com.jayway.jsonpath;

import static org.junit.Assert.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.PathCompiler;
import com.jayway.jsonpath.internal.token.TokenStack;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class JacksonTest_Split extends BaseTest implements EvaluationCallback {
    
    private static final Logger log = LoggerFactory.getLogger(JacksonTest_Split.class);
    private List<Object> results = new ArrayList<Object>();
    
    public static Configuration JACKSON_CONFIGURATION_JSONNODE = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider())
               .mappingProvider(new JacksonMappingProvider())
               .options(EnumSet.noneOf(Option.class))
               .evaluationListener(Collections.<EvaluationListener>emptyList())
               .build();
    
    @Test
    public void json_Test() throws Exception {
        jsonSplit_Test(JACKSON_CONFIGURATION);
        results.clear();
        //jsonSplit_Test(JSON_SMART_CONFIGURATION);
        //results.clear();
        jsonSplitDcrum_Test(JACKSON_CONFIGURATION_JSONNODE);
        results.clear();
    }

    private void jsonSplit_Test(Configuration jsonProviderCfg) throws JsonParseException, IOException, Exception {

        String res = "json_opsview1.json";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(res)) {
             Path path = PathCompiler.compile("$.list[*]");
             
             TokenStack stack = new TokenStack(jsonProviderCfg);
                       
             JsonFactory factory = new JsonFactory();
             stack.registerPath(path);
             stack.read(factory.createParser(stream), this, false);
        }
        log.debug("results: " + results.size());
        assertTrue(results.size() == 96);
    }

    private void jsonSplitDcrum_Test(Configuration jsonProviderCfg) throws JsonParseException, IOException, Exception {

        String res = "dcrum_rest_0.json";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(res)) {
             Path path = PathCompiler.compile("$.formattedData[*]");
             
             TokenStack stack = new TokenStack(jsonProviderCfg);
           
             JsonFactory factory = new JsonFactory();
             stack.registerPath(path);
             stack.read(factory.createParser(stream), this, false);
        }
        log.debug("results: " + results.size());
        assertTrue(results.size() == 2);
        ArrayNode node = (ArrayNode)results.get(0);
        List<String> expected = Lists.newArrayList("02/08/2017 13:25","BPO Noida","40.0");
        checkNodeValues(node,expected);
        
        node = (ArrayNode)results.get(1);
        expected = Lists.newArrayList("02/08/2017 13:25","BPO Jaipur","59.0");
        checkNodeValues(node,expected);
    }
    
    private void checkNodeValues(ArrayNode node, List<String> expected) {
        int idx = 0;
        for (String str : expected) {
            assertTrue(node.get(idx).textValue().equals(str));
            idx++;
        }
    }

    @Override
    public void resultFound(Object source, Object obj, Path path) throws Exception {
        log.debug(source + ":" + obj.getClass().getSimpleName() + " : " + String.valueOf(obj));
        results.add(obj);
    }

    @Override
    public void resultFoundExit(Object source, Object obj, Path path) throws Exception {
        //log.debug(source + ":" + String.valueOf(obj));
    }
}
