/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.services.dynamodb.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDBMapperIntegrationTestBase;
import software.amazon.awssdk.services.dynamodb.datamodeling.ConversionSchemas;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbAutoGeneratedKey;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbHashKey;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbMapper;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbMapperConfig;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbMappingException;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbRangeKey;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbVersionAttribute;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Tests of exception handling
 */
public class ExceptionHandlingIntegrationTest extends DynamoDBMapperIntegrationTestBase {

    @Test(expected = DynamoDbMappingException.class)
    public void testNoTableAnnotation() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.save(new NoTableAnnotation());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testNoTableAnnotationLoad() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.load(NoTableAnnotation.class, "abc");
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testNoDefaultConstructor() {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        NoDefaultConstructor obj = new NoDefaultConstructor("" + startKey++, "abc");
        util.save(obj);
        util.load(NoDefaultConstructor.class, obj.getKey());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testNoHashKeyGetter() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.save(new NoKeyGetterDefined());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testNoHashKeyGetterLoad() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.load(NoKeyGetterDefined.class, "abc");
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testPrivateKeyGetter() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.save(new PrivateKeyGetter());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testPrivateKeyGetterLoad() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.load(PrivateKeyGetter.class, "abc");
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testPrivateKeySetter() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.save(new PrivateKeySetter());
    }

    /*
     * To trigger this error, we need for a service object to be present, so
     * we'll insert one manually.
     */
    @Test(expected = DynamoDbMappingException.class)
    public void testPrivateKeySetterLoad() throws Exception {
        Map<String, AttributeValue> attr = new HashMap<String, AttributeValue>();
        attr.put(KEY_NAME, AttributeValue.builder().s("abc").build());
        dynamo.putItem(PutItemRequest.builder().tableName("aws-java-sdk-util").item(attr).build());
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.load(PrivateKeySetter.class, "abc");
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testPrivateSetterLoad() throws Exception {
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        PrivateSetter object = new PrivateSetter();
        object.setStringProperty("value");
        util.save(object);
        util.load(PrivateSetter.class, object.getKey());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testOverloadedSetter() {
        OverloadedSetter obj = new OverloadedSetter();
        obj.setKey("" + startKey++);
        obj.setAttribute("abc", "123");
        DynamoDbMapper mapper = new DynamoDbMapper(dynamo);
        mapper.save(obj);

        mapper.load(OverloadedSetter.class, obj.getKey());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testWrongTypeForSetter() {
        WrongTypeForSetter obj = new WrongTypeForSetter();
        obj.setKey("" + startKey++);
        obj.setAttribute(123);
        DynamoDbMapper mapper = new DynamoDbMapper(dynamo);
        mapper.save(obj);

        mapper.load(WrongTypeForSetter.class, obj.getKey());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testWrongDataType() {
        Map<String, AttributeValue> attr = new HashMap<String, AttributeValue>();
        attr.put("integerProperty", AttributeValue.builder().s("abc").build());
        attr.put(KEY_NAME, AttributeValue.builder().s("" + startKey++).build());
        dynamo.putItem(PutItemRequest.builder().tableName("aws-java-sdk-util").item(attr).build());
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.load(NumericFields.class, attr.get(KEY_NAME).s());
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testWrongDataType2() {
        Map<String, AttributeValue> attr = new HashMap<String, AttributeValue>();
        attr.put("integerProperty", AttributeValue.builder().ns("1", "2", "3").build());
        attr.put(KEY_NAME, AttributeValue.builder().s("" + startKey++).build());
        dynamo.putItem(PutItemRequest.builder().tableName("aws-java-sdk-util").item(attr).build());
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.load(NumericFields.class, attr.get(KEY_NAME).s());
    }

    // Complex types are not supported by the V1 conversion schema
    @Test(expected = DynamoDbMappingException.class)
    public void testComplexTypeFailure() {
        DynamoDbMapperConfig config = new DynamoDbMapperConfig(ConversionSchemas.V1);
        DynamoDbMapper util = new DynamoDbMapper(dynamo, config);

        ComplexType complexType = new ComplexType("" + startKey++, new ComplexType("" + startKey++, null));
        util.save(complexType);
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testUnsupportedHashKeyType() {
        ComplexType complexType = new ComplexType("" + startKey++, new ComplexType("" + startKey++, null));
        ComplexHashKeyType obj = new ComplexHashKeyType();
        obj.setKey(complexType);
        obj.setAttribute("abc");
        DynamoDbMapper util = new DynamoDbMapper(dynamo);
        util.save(obj);
    }

    // Lists are not supported by the V1 conversion schema.
    @Test(expected = DynamoDbMappingException.class)
    public void testNonsetCollection() {
        DynamoDbMapperConfig config = new DynamoDbMapperConfig(ConversionSchemas.V1);
        DynamoDbMapper mapper = new DynamoDbMapper(dynamo, config);

        NonSetCollectionType obj = new NonSetCollectionType();
        obj.setKey("" + startKey++);
        obj.setBadlyMapped(new ArrayList<String>());
        obj.getBadlyMapped().add("abc");
        mapper.save(obj);
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testFractionalVersionAttribute() {
        FractionalVersionAttribute obj = new FractionalVersionAttribute();
        obj.setKey("" + startKey++);
        obj.setVersion(0d);
        DynamoDbMapper mapper = new DynamoDbMapper(dynamo);
        mapper.save(obj);
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testAutoGeneratedIntegerHashKey() {
        AutoGeneratedIntegerKey obj = new AutoGeneratedIntegerKey();
        obj.setValue("fdgfdsgf");
        DynamoDbMapper mapper = new DynamoDbMapper(dynamo);
        mapper.save(obj);
    }

    @Test(expected = DynamoDbMappingException.class)
    public void testAutoGeneratedIntegerRangeKey() {
        AutoGeneratedIntegerRangeKey obj = new AutoGeneratedIntegerRangeKey();
        obj.setKey("Bldadsfa");
        obj.setValue("fdgfdsgf");
        DynamoDbMapper mapper = new DynamoDbMapper(dynamo);
        mapper.save(obj);
    }

    public static class NoTableAnnotation {

        private String key;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class NoDefaultConstructor {

        private String key;
        private String attribute;

        public NoDefaultConstructor(String key, String attribute) {
            super();
            this.key = key;
            this.attribute = attribute;
        }

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class NoKeyGetterDefined {

        @SuppressWarnings("unused")
        private String key;
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class PrivateKeyGetter {

        private String key;

        @SuppressWarnings("unused")
        @DynamoDbHashKey
        private String getKey() {
            return key;
        }

        @SuppressWarnings("unused")
        private void setKey(String key) {
            this.key = key;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class PrivateKeySetter {

        private String key;

        @DynamoDbHashKey
        @DynamoDbAutoGeneratedKey
        public String getKey() {
            return key;
        }

        @SuppressWarnings("unused")
        private void setKey(String key) {
            this.key = key;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class PrivateSetter {

        private String key;
        private String stringProperty;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String stringProperty() {
            return stringProperty;
        }

        private void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class OverloadedSetter {

        private String key;
        private String attribute;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute, String unused) {
            this.attribute = attribute;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class WrongTypeForSetter {

        private String key;
        private String attribute;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(Integer attribute) {
            this.attribute = String.valueOf(attribute);
        }

    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class NumericFields {

        private String key;
        private Integer integerProperty;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Integer getIntegerProperty() {
            return integerProperty;
        }

        public void setIntegerProperty(Integer integerProperty) {
            this.integerProperty = integerProperty;
        }

    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class ComplexType {

        public String key;
        public ComplexType type;

        public ComplexType(String key, ComplexType type) {
            super();
            this.key = key;
            this.type = type;
        }

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public ComplexType getType() {
            return type;
        }

        public void setType(ComplexType type) {
            this.type = type;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class ComplexHashKeyType {

        private ComplexType key;
        private String attribute;

        @DynamoDbHashKey
        public ComplexType getKey() {
            return key;
        }

        public void setKey(ComplexType key) {
            this.key = key;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class NonSetCollectionType {

        private String key;
        private List<String> badlyMapped;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getBadlyMapped() {
            return badlyMapped;
        }

        public void setBadlyMapped(List<String> badlyMapped) {
            this.badlyMapped = badlyMapped;
        }
    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class FractionalVersionAttribute {

        private String key;
        private Double version;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDbVersionAttribute
        public Double getVersion() {
            return version;
        }

        public void setVersion(Double version) {
            this.version = version;
        }

    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class AutoGeneratedIntegerKey {

        private Integer key;
        private String value;

        @DynamoDbHashKey
        @DynamoDbAutoGeneratedKey
        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String value() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @DynamoDbTable(tableName = "aws-java-sdk-util")
    public static class AutoGeneratedIntegerRangeKey {

        private String key;
        private Integer rangekey;
        private String value;

        @DynamoDbHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDbAutoGeneratedKey
        @DynamoDbRangeKey
        public Integer getRangekey() {
            return rangekey;
        }

        public void setRangekey(Integer rangekey) {
            this.rangekey = rangekey;
        }

        public String value() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
