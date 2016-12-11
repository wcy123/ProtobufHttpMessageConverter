package org.wcy123;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.wcy123.api.DataTypesTest;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.ListValue;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(
        classes = ConfigurationWithProtobufMessageConverterTest.Config.class)
@WebAppConfiguration
@Slf4j
public class ConfigurationWithProtobufMessageConverterTest {
    @Autowired
    ProtobufMessageConverter converter;

    @Test
    public void contextLoads() {}

    @Test
    public void main1() throws Exception {
        final DataTypesTest.Root root1 = getRoot();
        final MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        outputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_UTF8.toString());
        converter.write(root1, MediaType.APPLICATION_JSON_UTF8, outputMessage);
        log.info("convert to json = {} ", outputMessage.getBodyAsString());
        JSONAssert.assertEquals(expectedString(), outputMessage.getBodyAsString(), true);

        final MockHttpInputMessage inputMessage =
                new MockHttpInputMessage(expectedString().getBytes());
        inputMessage.getHeaders().add(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_UTF8.toString());
        final DataTypesTest.Root root2 =
                (DataTypesTest.Root) converter.read(DataTypesTest.Root.class, inputMessage);
        log.info("converted from json = {} ", root2);
        Assert.assertEquals(root1, root2);
    }

    private DataTypesTest.Root getRoot() throws UnsupportedEncodingException {
        final long currentTimeMillis = 1481452195123L;
        Instant instant = Instant.ofEpochMilli(currentTimeMillis);
        return DataTypesTest.Root.newBuilder()
                .setObj1(DataTypesTest.Obj1.newBuilder()
                        .setName("YourName")
                        .build())
                .setBase64(ByteString.copyFrom("abc123!?$*&()'-=@~", "UTF-8"))
                .setAnEnum(DataTypesTest.Root.AnEnum.FOO_BAR)
                .setABoolean(true)
                .setAString("hello world")
                .setAInt32(-32)
                .setAUint32(32)
                .setAFixed(64)
                .setAInt64(-64)
                .setAUint64(64)
                .setAFixed64(128)
                .setAFloat(1.0f)
                .setADouble(2.0)
                .setEitherString("Now It is a String, not an integer")
                .setAny(Any.pack(DataTypesTest.Point.newBuilder().setX(10).setY(20).build()))
                .putAMap("m1", ListValue.newBuilder()
                        .addValues(Value.newBuilder().setStringValue("h1").build())
                        .addValues(Value.newBuilder().setStringValue("h2").build())
                        .build())
                .putAMap("m2", ListValue.newBuilder().build())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build())
                .build();
    }

    private String expectedString() {
        // language=JSON
        return "{\n" +
                "  \"hello\": {\n" +
                "    \"name\": \"YourName\"\n" +
                "  },\n" +
                "  \"base64\": \"YWJjMTIzIT8kKiYoKSctPUB+\",\n" +
                "  \"aBoolean\": true,\n" +
                "  \"aString\": \"hello world\",\n" +
                "  \"aInt32\": -32,\n" +
                "  \"aUint32\": 32,\n" +
                "  \"aFixed\": 64,\n" +
                "  \"aInt64\": \"-64\",\n" +
                "  \"aUint64\": \"64\",\n" +
                "  \"aFixed64\": \"128\",\n" +
                "  \"aFloat\": 1.0,\n" +
                "  \"aDouble\": 2.0,\n" +
                "  \"eitherString\": \"Now It is a String, not an integer\",\n" +
                "  \"any\": {\n" +
                "    \"@type\": \"type.googleapis.com/org.wcy123.api.Point\",\n" +
                "    \"x\": 10,\n" +
                "    \"y\": 20\n" +
                "  },\n" +
                "  \"aMap\": {\n" +
                "    \"m1\": [\"h1\", \"h2\"],\n" +
                "    \"m2\": []\n" +
                "  },\n" +
                "  \"timestamp\": \"2016-12-11T10:29:55.123Z\"\n" +
                "} ";
    }

    @Configuration
    @ComponentScan
    public static class Config {

    }
}
