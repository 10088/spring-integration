/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.file.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mark Fisher
 * @author Marius Bogoevici
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class FileOutboundChannelAdapterParserTests {

    @Autowired
    @Qualifier("simpleAdapter")
    EventDrivenConsumer simpleAdapter;

    @Autowired
    @Qualifier("adapterWithCustomNameGenerator")
    EventDrivenConsumer adapterWithCustomNameGenerator;

    @Autowired
    @Qualifier("adapterWithDeleteFlag")
    EventDrivenConsumer adapterWithDeleteFlag;

    @Autowired
    @Qualifier("adapterWithOrder")
    EventDrivenConsumer adapterWithOrder;

    @Autowired
    @Qualifier("adapterWithCharset")
    EventDrivenConsumer adapterWithCharset;


    @Test
    public void simpleAdapter() {
        DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(simpleAdapter);
        FileWritingMessageHandler handler = (FileWritingMessageHandler)
                adapterAccessor.getPropertyValue("handler");
        DirectFieldAccessor handlerAccessor = new DirectFieldAccessor(handler);
        File expected = new File(System.getProperty("java.io.tmpdir"));
        File actual = (File) handlerAccessor.getPropertyValue("destinationDirectory");
        assertEquals(expected, actual);
        assertTrue(handlerAccessor.getPropertyValue("fileNameGenerator") instanceof DefaultFileNameGenerator);
        assertEquals(Boolean.FALSE, handlerAccessor.getPropertyValue("deleteSourceFiles"));
    }

    @Test
    public void adapterWithCustomFileNameGenerator() {
        DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapterWithCustomNameGenerator);
        FileWritingMessageHandler handler = (FileWritingMessageHandler)
                adapterAccessor.getPropertyValue("handler");
        DirectFieldAccessor handlerAccessor = new DirectFieldAccessor(handler);
        File expected = new File(System.getProperty("java.io.tmpdir"));
        File actual = (File) handlerAccessor.getPropertyValue("destinationDirectory");
        assertEquals(expected, actual);
        assertTrue(handlerAccessor.getPropertyValue("fileNameGenerator") instanceof CustomFileNameGenerator);
    }

    @Test
    public void adapterWithDeleteFlag() {
        DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapterWithDeleteFlag);
        FileWritingMessageHandler handler = (FileWritingMessageHandler)
                adapterAccessor.getPropertyValue("handler");
        DirectFieldAccessor handlerAccessor = new DirectFieldAccessor(handler);
        assertEquals(Boolean.TRUE, handlerAccessor.getPropertyValue("deleteSourceFiles"));
    }

    @Test
    public void adapterWithOrder() {
        DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapterWithOrder);
        FileWritingMessageHandler handler = (FileWritingMessageHandler)
                adapterAccessor.getPropertyValue("handler");
        DirectFieldAccessor handlerAccessor = new DirectFieldAccessor(handler);
        assertEquals(555, handlerAccessor.getPropertyValue("order"));
    }

    @Test
    public void adapterWithAutoStartupFalse() {
        DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapterWithOrder);
        assertEquals(Boolean.FALSE, adapterAccessor.getPropertyValue("autoStartup"));
    }

    @Test
    public void adapterWithCharset() {
        DirectFieldAccessor adapterAccessor = new DirectFieldAccessor(adapterWithCharset);
             FileWritingMessageHandler handler = (FileWritingMessageHandler)
                adapterAccessor.getPropertyValue("handler");
        DirectFieldAccessor handlerAccessor = new DirectFieldAccessor(handler);
        assertEquals(Charset.forName("UTF-8"), handlerAccessor.getPropertyValue("charset"));
    }

}
