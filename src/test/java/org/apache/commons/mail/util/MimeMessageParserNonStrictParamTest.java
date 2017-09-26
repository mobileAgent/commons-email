/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParameterList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Testing the MimeMessageParser with some javax.mail strict settings turned off
 */
public class MimeMessageParserNonStrictParamTest
{

    @BeforeClass
    public static void hackParameterListSetup() throws Exception
    {
        setFinalStatic(ParameterList.class.getDeclaredField("parametersStrict"), false);
    }

    @AfterClass
    public static void resetParameterListSetup() throws Exception
    {
        setFinalStatic(ParameterList.class.getDeclaredField("parametersStrict"), true);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
      field.setAccessible(true);

      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

      field.set(null, newValue);
   }


    @Test
    public void testContentExtractionAsInputStream() throws Exception
    {
        final Session session = Session.getDefaultInstance(new Properties());
        final MimeMessage message = MimeMessageUtils.createMimeMessage(session,
            new File("./src/test/resources/eml/content-charset-parsing-anomaly.eml"));
        final MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

        mimeMessageParser.parse();

        assertEquals("Subject extraction failed", "Test", mimeMessageParser.getSubject());
        assertNotNull("Message parsing failed", mimeMessageParser.getMimeMessage());
        assertTrue("Multipart determination failed", mimeMessageParser.isMultipart());

        assertTrue("Html content must be extracted", mimeMessageParser.hasHtmlContent());
        String htmlContent = mimeMessageParser.getHtmlContent();
        assertNotNull("Html content must not be null", htmlContent);
        assertTrue("Html content must contain correct data", htmlContent.contains("This is the html text body."));
        assertEquals("Html charset extraction failed", "cp1252", mimeMessageParser.getHtmlCharset());

        assertTrue("Plain content must be extracted", mimeMessageParser.hasPlainContent());
        String plainContent = mimeMessageParser.getPlainContent();
        assertNotNull("Plain content must not be null", plainContent);
        assertTrue("Plain content must contain correct data", plainContent.contains("This is the plain text body."));
        assertEquals("Plain charset extraction failed", "charset=\"utf-8\"", mimeMessageParser.getPlainCharset());
    }
}
