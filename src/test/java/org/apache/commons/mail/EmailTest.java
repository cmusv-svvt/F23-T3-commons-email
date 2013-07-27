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
package org.apache.commons.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.mail.mocks.MockEmailConcrete;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test case for Email Class
 *
 * @since 1.0
 * @version $Id$
 */
public class EmailTest extends AbstractEmailTest
{
    /** valid email addresses */
    private static final String[] ARR_VALID_EMAILS =
        {
            "me@home.com",
            "joe.doe@apache.org",
            "someone_here@work-address.com.au"
        };

    /** mock for testing */
    private MockEmailConcrete email;

    @Before
    public void setUpEmailTest()
    {
        // reusable objects to be used across multiple tests
        email = new MockEmailConcrete();
    }

    @Test
    public void testGetSetDebug()
    {
        email.setDebug(true);
        assertTrue(email.isDebug());
        email.setDebug(false);
        assertFalse(email.isDebug());
    }

    @Test
    public void testGetSetSession() throws Exception
    {

        Properties properties = new Properties(System.getProperties());
        properties.setProperty(EmailConstants.MAIL_TRANSPORT_PROTOCOL, EmailConstants.SMTP);

        properties.setProperty(
            EmailConstants.MAIL_PORT,
            String.valueOf(getMailServerPort()));
        properties.setProperty(EmailConstants.MAIL_HOST, strTestMailServer);
        properties.setProperty(EmailConstants.MAIL_DEBUG, String.valueOf(false));

        Session mySession = Session.getInstance(properties, null);

        email.setMailSession(mySession);
        assertEquals(mySession, email.getMailSession());

    }

    @Test
    public void testGetSetAuthentication()
    {
        // setup
        String strUsername = "user.name";
        String strPassword = "user.pwd";
        email.setAuthentication(strUsername, strPassword);

        // this is cast into DefaultAuthenticator for convenience
        // and give us access to the getPasswordAuthentication fn
        DefaultAuthenticator retrievedAuth =
            (DefaultAuthenticator) email.getAuthenticator();

        // tests
        assertEquals(
            strUsername,
            retrievedAuth.getPasswordAuthentication().getUserName());
        assertEquals(
            strPassword,
            retrievedAuth.getPasswordAuthentication().getPassword());
    }

    @Test
    public void testGetSetAuthenticator()
    {
        // setup
        String strUsername = "user.name";
        String strPassword = "user.pwd";
        DefaultAuthenticator authenticator =
            new DefaultAuthenticator(strUsername, strPassword);
        email.setAuthenticator(authenticator);

        // this is cast into DefaultAuthenticator for convenience
        // and give us access to the getPasswordAuthentication fn
        DefaultAuthenticator retrievedAuth =
            (DefaultAuthenticator) email.getAuthenticator();

        // tests
        assertEquals(
                strUsername,
                retrievedAuth.getPasswordAuthentication().getUserName());
        assertEquals(
            strPassword,
            retrievedAuth.getPasswordAuthentication().getPassword());
    }

    @Test
    public void testGetSetCharset()
    {
        // test ASCII and UTF-8 charsets; since every JVM is required
        // to support these, testing them should always succeed.
        Charset set = Charset.forName("US-ASCII");
        email.setCharset(set.name());
        assertEquals(set.name(), email.getCharset());

        set = Charset.forName("UTF-8");
        email.setCharset(set.name());
        assertEquals(set.name(), email.getCharset());
    }

    @Test
    public void testSetContentEmptyMimeMultipart()
    {
        MimeMultipart part = new MimeMultipart();
        email.setContent(part);

        assertEquals(part, email.getContentMimeMultipart());
    }

    @Test
    public void testSetContentMimeMultipart()
    {
        MimeMultipart part = new MimeMultipart("abc123");
        email.setContent(part);

        assertEquals(part, email.getContentMimeMultipart());
    }

    @Test
    public void testSetContentNull() throws Exception
    {
        email.setContent(null);
        assertNull(email.getContentMimeMultipart());
    }

    @Test
    public void testSetContentObject()
    {
        // setup
        String testObject = "test string object";
        String testContentType = "";

        // ====================================================================
        // test (string object and valid content type)
        testObject = "test string object";
        testContentType = " ; charset=" + EmailConstants.US_ASCII;

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());

        // ====================================================================
        // test (null string object and valid content type)
        testObject = null;
        testContentType = " ; charset=" + EmailConstants.US_ASCII + " some more here";

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());

        // ====================================================================
        // test (string object and null content type)
        testObject = "test string object";
        testContentType = null;

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());

        // ====================================================================
        // test (string object and invalid content type)
        testObject = "test string object";
        testContentType = " something incorrect ";

        email.setContent(testObject, testContentType);
        assertEquals(testObject, email.getContentObject());
        assertEquals(testContentType, email.getContentType());
    }

    @Test
    public void testGetSetHostName()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            email.setHostName(testCharsValid[i]);
            assertEquals(testCharsValid[i], email.getHostName());
        }
    }

    @Test
    public void testGetSetSmtpPort()
    {
        email.setSmtpPort(1);
        assertEquals(
            1,
            Integer.valueOf(email.getSmtpPort()).intValue());

        email.setSmtpPort(Integer.MAX_VALUE);
        assertEquals(
                Integer.MAX_VALUE,
                Integer.valueOf(email.getSmtpPort()).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSmtpPortZero()
    {
        email.setSmtpPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSmptPortNegative()
    {
        email.setSmtpPort(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSmtpPortMinValue()
    {
        email.setSmtpPort(Integer.MIN_VALUE);
    }

    @Test
    public void testSetFrom() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "me@home.com"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {

            // set from
            email.setFrom(ARR_VALID_EMAILS[i]);

            // retrieve and verify
            assertEquals(arrExpected.get(i), email.getFromAddress());
        }
    }

    @Test
    public void testSetFromWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success (with charset set)
        // ====================================================================
        String testValidEmail = "me@home.com";

        InternetAddress inetExpected =
            new InternetAddress("me@home.com", "me@home.com", EmailConstants.ISO_8859_1);

        // set from
        email.setFrom(testValidEmail, testValidEmail, EmailConstants.ISO_8859_1);

        // retrieve and verify
        assertEquals(inetExpected, email.getFromAddress());

    }

    @Test
    public void testSetFrom2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String[] testEmailNames = {"Name1", "", null};
        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        arrExpected.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.setFrom(ARR_VALID_EMAILS[i], testEmailNames[i]);

            // retrieve and verify
            assertEquals(arrExpected.get(i), email.getFromAddress());

        }
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testSetFromBadEncoding() throws Exception {
        email.setFrom("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test    
    public void testAddTo() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.addTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test
    public void testAddToArray() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        //set To
        email.addTo(ARR_VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test
    public void testAddToWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(
            new InternetAddress(
                "me@home.com",
                "Name1",
                testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.addTo(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test
    public void testAddTo2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.addTo(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getToAddresses().size());
        assertEquals(arrExpected.toString(), email.getToAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddToBadEncoding() throws Exception
    {
        email.addTo("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testSetTo() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        List<InternetAddress> testEmailValid2 = new ArrayList<InternetAddress>();
        testEmailValid2.add(new InternetAddress("me@home.com", "Name1"));
        testEmailValid2.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        testEmailValid2.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        email.setTo(testEmailValid2);

        // retrieve and verify
        assertEquals(testEmailValid2.size(), email.getToAddresses().size());
        assertEquals(
                testEmailValid2.toString(),
                email.getToAddresses().toString());
    }

    @Test(expected = EmailException.class)
    public void testSetToNull() throws Exception
    {
        email.setTo(null);
    }

    @Test(expected = EmailException.class)
    public void testSetToEmpty() throws Exception
    {
        email.setTo(Collections.<InternetAddress>emptyList());
    }

    @Test
    public void testAddCc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.addCc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test
    public void testAddCcArray() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        //set Cc array
        email.addCc(ARR_VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test
    public void testAddCcWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(
            new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        // add valid ccs
        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            email.addCc(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test
    public void testAddCc2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.addCc(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getCcAddresses().size());
        assertEquals(arrExpected.toString(), email.getCcAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddCcBadEncoding() throws Exception
    {
        email.addCc("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testSetCc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        List<InternetAddress> testEmailValid2 = new ArrayList<InternetAddress>();
        testEmailValid2.add(new InternetAddress("Name1 <me@home.com>"));
        testEmailValid2.add(new InternetAddress("\"joe.doe@apache.org\" <joe.doe@apache.org>"));
        testEmailValid2.add(
                new InternetAddress("\"someone_here@work.com.au\" <someone_here@work.com.au>"));

        email.setCc(testEmailValid2);
        assertEquals(testEmailValid2, email.getCcAddresses());
    }

    @Test(expected = EmailException.class)
    public void testSetCcNull() throws Exception
    {
        email.setCc(null);
    }

    @Test(expected = EmailException.class)
    public void testSetCcEmpty() throws Exception
    {
        email.setCc(Collections.<InternetAddress>emptyList());
    }

    @Test
    public void testAddBcc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // add a valid bcc
            email.addBcc(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test
    public void testAddBccArray() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        // add a valid bcc
        email.addBcc(ARR_VALID_EMAILS);

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test
    public void testAddBccWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set bccs
            email.addBcc(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test
    public void testAddBcc2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};


        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set from
            email.addBcc(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getBccAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getBccAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddBccBadEncoding() throws Exception
    {
        email.addBcc("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testSetBcc() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        List<InternetAddress> testInetEmailValid = new ArrayList<InternetAddress>();
        testInetEmailValid.add(new InternetAddress("me@home.com", "Name1"));
        testInetEmailValid.add(
            new InternetAddress(
                "joe.doe@apache.org",
                "joe.doe@apache.org"));
        testInetEmailValid.add(
            new InternetAddress(
                "someone_here@work-address.com.au",
                "someone_here@work-address.com.au"));

        email.setBcc(testInetEmailValid);
        assertEquals(testInetEmailValid, email.getBccAddresses());
    }

    @Test(expected = EmailException.class)
    public void testSetBccNull() throws Exception
    {
        email.setBcc(null);
    }

    @Test(expected = EmailException.class)
    public void testSetBccEmpty() throws Exception
    {
        email.setBcc(Collections.<InternetAddress>emptyList());
    }

    @Test
    public void testAddReplyTo() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set replyTo
            email.addReplyTo(ARR_VALID_EMAILS[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getReplyToAddresses().toString());
    }

    @Test
    public void testAddReplyToWithEncoding() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        String testCharset = EmailConstants.ISO_8859_1;
        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1", testCharset));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set replyTo
            email.addReplyTo(ARR_VALID_EMAILS[i], testEmailNames[i], testCharset);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getReplyToAddresses().toString());
    }

    @Test
    public void testAddReplyTo2() throws Exception
    {
        // ====================================================================
        // Test Success
        // ====================================================================

        String[] testEmailNames = {"Name1", "", null};

        List<InternetAddress> arrExpected = new ArrayList<InternetAddress>();
        arrExpected.add(new InternetAddress("me@home.com", "Name1"));
        arrExpected.add(new InternetAddress("joe.doe@apache.org"));
        arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

        for (int i = 0; i < ARR_VALID_EMAILS.length; i++)
        {
            // set replyTo
            email.addReplyTo(ARR_VALID_EMAILS[i], testEmailNames[i]);
        }

        // retrieve and verify
        assertEquals(arrExpected.size(), email.getReplyToAddresses().size());
        assertEquals(
            arrExpected.toString(),
            email.getReplyToAddresses().toString());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testAddReplyToBadEncoding() throws Exception
    {
        email.addReplyTo("me@home.com", "me@home.com", "bad.encoding\uc5ec\n");
    }

    @Test
    public void testAddHeader()
    {
        // ====================================================================
        // Test Success
        // ====================================================================
        Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        for (Iterator<Map.Entry<String, String>> items = ht.entrySet().iterator(); items.hasNext();)
        {
            Map.Entry<String, String> entry = items.next();
            String strName = entry.getKey();
            String strValue = entry.getValue();
            email.addHeader(strName, strValue);
        }

        assertEquals(ht.size(), email.getHeaders().size());
        assertEquals(ht, email.getHeaders());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderEmptyName() throws Exception
    {
        email.addHeader("", "me@home.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderNullName() throws Exception
    {
        email.addHeader(null, "me@home.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderEmptyValue() throws Exception
    {
        email.addHeader("X-Mailer", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddHeaderNullValue() throws Exception
    {
        email.addHeader("X-Mailer", null);
    }

    @Test
    public void testSetHeaders()
    {
        Map<String, String> ht = new Hashtable<String, String>();
        ht.put("X-Priority", "1");
        ht.put("Disposition-Notification-To", "me@home.com");
        ht.put("X-Mailer", "Sendmail");

        email.setHeaders(ht);

        assertEquals(ht.size(), email.getHeaders().size());
        assertEquals(ht, email.getHeaders());
    }

    @Test
    public void testFoldingHeaders() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        final String headerValue = "1234567890 1234567890 123456789 01234567890 123456789 0123456789 01234567890 01234567890";
        email.addHeader("X-LongHeader", headerValue);
        
        assertTrue(email.getHeaders().size() == 1);
        // the header should not yet be folded -> will be done by buildMimeMessage()
        assertTrue(email.getHeaders().get("X-LongHeader").toString().indexOf("\r\n") == -1);
        
        email.buildMimeMessage();

        MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        
        String[] values = msg.getHeader("X-LongHeader");
        assertEquals(1, values.length);
        
        // the header should be split in two lines
        String[] lines = values[0].split("\\r\\n");
        assertEquals(2, lines.length);
        
        // there should only be one line-break
        assertTrue(values[0].indexOf("\n") == values[0].lastIndexOf("\n"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderEmptyValue() throws Exception
    {
        email.setHeaders(Collections.singletonMap("X-Mailer", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderNullValue() throws Exception
    {
        email.setHeaders(Collections.singletonMap("X-Mailer", (String) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderEmptyName() throws Exception
    {
        email.setHeaders(Collections.singletonMap("", "me@home.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHeaderNullName() throws Exception
    {
        email.setHeaders(Collections.singletonMap((String) null, "me@home.com"));
    }

    @Test
    public void testSetSubject()
    {

        for (int i = 0; i < testCharsValid.length; i++)
        {
            email.setSubject(testCharsValid[i]);
            assertEquals(testCharsValid[i], email.getSubject());
        }
    }

    @Test(expected = EmailException.class)
    public void testSendNoHostName() throws Exception
    {
        getMailServer();

        email = new MockEmailConcrete();
        email.send();
    }

    @Test
    public void testSendBadHostName()
    {
        try
        {
            getMailServer();

            email = new MockEmailConcrete();
            email.setSubject("Test Email #1 Subject");
            email.setHostName("bad.host.com");
            email.setFrom("me@home.com");
            email.addTo("me@home.com");
            email.addCc("me@home.com");
            email.addBcc("me@home.com");
            email.addReplyTo("me@home.com");

            email.setContent(
                    "test string object",
                    " ; charset=" + EmailConstants.US_ASCII);

            email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(e.getCause() instanceof ParseException);
            fakeMailServer.stop();
        }
    }

    @Test(expected = EmailException.class)
    public void testSendFromNotSet() throws Exception
    {
         getMailServer();

         email = new MockEmailConcrete();
         email.setHostName(strTestMailServer);
         email.setSmtpPort(getMailServerPort());

         email.send();
    }

    @Test(expected = EmailException.class)
    public void testSendDestinationNotSet() throws Exception
    {
        getMailServer();

        email = new MockEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("me@home.com");

        email.send();
    }

    @Test(expected = EmailException.class)
    public void testSendBadAuthSet() throws Exception
    {
        getMailServer();

        email = new MockEmailConcrete();
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom(strTestMailFrom);
        email.addTo(strTestMailTo);
        email.setAuthentication(null, null);

        email.send();
    }

    @Test
    public void testSendCorrectSmtpPortContainedInException()
    {
        try
        {
            getMailServer();

            email = new MockEmailConcrete();
            email.setHostName("bad.host.com");
            email.setSSLOnConnect(true);
            email.setFrom(strTestMailFrom);
            email.addTo(strTestMailTo);
            email.setAuthentication(null, null);
            email.send();
            fail("Should have thrown an exception");
        }
        catch (EmailException e)
        {
            assertTrue(e.getMessage().contains("bad.host.com:465"));
            fakeMailServer.stop();
        }
    }

    @Test
    public void testGetSetSentDate()
    {
        // with input date

        Date dtTest = Calendar.getInstance().getTime();
        email.setSentDate(dtTest);
        assertEquals(dtTest, email.getSentDate());

        // with null input (this is a fudge :D)
        email.setSentDate(null);

        Date sentDate = email.getSentDate();

        // Date objects are millisecond specific. If you have a slow processor,
        // time passes between the generation of dtTest and the new Date() in
        // getSentDate() and this test fails. Make sure that the difference
        // is less than a second...
        assertTrue(Math.abs(sentDate.getTime() - dtTest.getTime()) < 1000);
    }

    @Test
    public void testToInternetAddressArray() throws Exception
    {
        List<InternetAddress> testInetEmailValid = new ArrayList<InternetAddress>();

        testInetEmailValid.add(new InternetAddress("me@home.com", "Name1"));
        testInetEmailValid.add(
                new InternetAddress(
                        "joe.doe@apache.org",
                        "joe.doe@apache.org"));
        testInetEmailValid.add(
                new InternetAddress(
                        "someone_here@work-address.com.au",
                        "someone_here@work-address.com.au"));

        email.setBcc(testInetEmailValid);
        assertEquals(
                testInetEmailValid.size(),
                email.getBccAddresses().size());
    }

    @Test
    public void testSetPopBeforeSmtp()
    {
        // simple test (can be improved)
        boolean boolPopBeforeSmtp = true;
        String strHost = "mail.home.com";
        String strUsername = "user.name";
        String strPassword = "user.passwd";

        email.setPopBeforeSmtp(
            boolPopBeforeSmtp,
            strHost,
            strUsername,
            strPassword);

        // retrieve and verify
        assertEquals(boolPopBeforeSmtp, email.isPopBeforeSmtp());
        assertEquals(strHost, email.getPopHost());
        assertEquals(strUsername, email.getPopUsername());
        assertEquals(strPassword, email.getPopPassword());
    }

    /**
     * Test: When Email.setCharset() is called, a subsequent setContent()
     * should use that charset for text content types unless overridden
     * by the contentType parameter.
     * See https://issues.apache.org/jira/browse/EMAIL-1.
     *
     *
     * Case 1:
     * Setting a default charset results in adding that charset info to
     * to the content type of a text/based content object.
     * @throws Exception on any error
     */
    @Test
    public void testDefaultCharsetAppliesToTextContent() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        email.setContent("test content", "text/plain");
        email.buildMimeMessage();
        MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("text/plain; charset=ISO-8859-1", msg.getContentType());
    }

    /**
     * Case 2:
     * A default charset is overridden by an explicitly specified
     * charset in setContent().
     * @throws Exception on any error
     */
    @Test
    public void testDefaultCharsetCanBeOverriddenByContentType()
        throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        email.setContent("test content", "text/plain; charset=US-ASCII");
        email.buildMimeMessage();
        MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("text/plain; charset=US-ASCII", msg.getContentType());
    }

    /**
     * Case 3:
     * A non-text content object ignores a default charset entirely.
     * @throws Exception on any error
     */
    @Test
    public void testDefaultCharsetIgnoredByNonTextContent()
        throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        email.setContent("test content", "application/octet-stream");
        email.buildMimeMessage();
        MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("application/octet-stream", msg.getContentType());
    }

    @Test
    public void testCorrectContentTypeForPNG() throws Exception
    {
        email.setHostName(strTestMailServer);
        email.setSmtpPort(getMailServerPort());
        email.setFrom("a@b.com");
        email.addTo("c@d.com");
        email.setSubject("test mail");

        email.setCharset("ISO-8859-1");
        File png = new File("./target/test-classes/images/logos/maven-feather.png");
        email.setContent(png, "image/png");
        email.buildMimeMessage();
        MimeMessage msg = email.getMimeMessage();
        msg.saveChanges();
        assertEquals("image/png", msg.getContentType());
    }    
}
