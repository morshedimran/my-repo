/**
 * personium.io
 * Copyright 2014 FUJITSU LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fujitsu.dc.test.jersey.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.fujitsu.dc.core.DcCoreAuthzException;
import com.fujitsu.dc.core.DcCoreException;
import com.fujitsu.dc.core.auth.OAuth2Helper;
import com.fujitsu.dc.test.categories.Integration;
import com.fujitsu.dc.test.categories.Regression;
import com.fujitsu.dc.test.categories.Unit;
import com.fujitsu.dc.test.jersey.DcException;
import com.fujitsu.dc.test.jersey.DcResponse;
import com.fujitsu.dc.test.jersey.DcRestAdapter;
import com.fujitsu.dc.test.jersey.DcRunner;
import com.fujitsu.dc.test.jersey.ODataCommon;
import com.fujitsu.dc.test.setup.Setup;
import com.fujitsu.dc.test.unit.core.UrlUtils;
import com.fujitsu.dc.test.utils.ResourceUtils;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * BoxURL取得 APIのテスト.
 */
@RunWith(DcRunner.class)
@Category({Unit.class, Integration.class, Regression.class })
public class BoxUrlTest extends ODataCommon {

    private static final Map<String, String> INIT_PARAMS = new HashMap<String, String>();
    static {
        INIT_PARAMS.put("com.sun.jersey.config.property.packages",
                "com.fujitsu.dc.core.rs");
        INIT_PARAMS.put("com.sun.jersey.spi.container.ContainerRequestFilters",
                "com.fujitsu.dc.core.jersey.filter.DcCoreContainerFilter");
        INIT_PARAMS.put("com.sun.jersey.spi.container.ContainerResponseFilters",
                "com.fujitsu.dc.core.jersey.filter.DcCoreContainerFilter");
        INIT_PARAMS.put("javax.ws.rs.Application",
                "com.fujitsu.dc.core.rs.DcCoreApplication");
        INIT_PARAMS.put("com.sun.jersey.config.feature.DisableWADL",
                "true");
    }

    /**
     * コンストラクタ.
     */
    public BoxUrlTest() {
        super(new WebAppDescriptor.Builder(INIT_PARAMS).build());
    }

    /**
     * 指定したschemaのBoxURLが取得できること.
     */
    @Test
    public final void 指定したschemaのBoxURLが取得できること() {
        try {
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot(Setup.TEST_CELL_SCHEMA1)),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, Setup.TEST_BOX1), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        }
    }

    /**
     * BoxURL取得でPOST以外のメソッドを指定した場合に405が返却されること.
     */
    @Test
    public final void BoxURL取得でPOST以外のメソッドを指定した場合に405が返却されること() {
        try {
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

            res = rest.del(UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, res.getStatusCode());
            DcCoreException e = DcCoreException.Misc.METHOD_NOT_ALLOWED;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        }
    }

    /**
     * schemaが空指定の場合に400が返却されること.
     */
    @Test
    public final void schemaが空指定の場合に400が返却されること() {
        try {
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1, ""), requestheaders);
            assertEquals(HttpStatus.SC_BAD_REQUEST, res.getStatusCode());
            DcCoreException e = DcCoreException.OData.QUERY_INVALID_ERROR.params("schema", "");
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        }
    }

    /**
     * URI形式でないschemaを指定した場合に400が返却されること.
     */
    @Test
    public final void URI形式でないschemaを指定した場合に400が返却されること() {
        try {
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1, "test"), requestheaders);
            assertEquals(HttpStatus.SC_BAD_REQUEST, res.getStatusCode());
            DcCoreException e = DcCoreException.OData.QUERY_INVALID_ERROR.params("schema", "test");
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        }
    }

    /**
     * 指定したschemaのBoxが存在しない場合に403が返却されること.
     */
    @Test
    public final void 指定したschemaのBoxが存在しない場合に403が返却されること() {
        try {
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.NECESSARY_PRIVILEGE_LACKING;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        }
    }

    /**
     * マスタートークンを使用してschema指定がない場合に403が返却されること.
     */
    @Test
    public final void マスタートークンを使用してschema指定がない場合に403が返却されること() {
        try {
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            DcCoreException e = DcCoreException.Auth.NECESSARY_PRIVILEGE_LACKING;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        }
    }

    /**
     * BoxRead権限のあるユーザーでschema指定がある場合に302が返却されること.
     */
    @Test
    public final void BoxRead権限のあるユーザーでschema指定がある場合に302が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * アクセス権限のないユーザーでschema指定がある場合に403が返却されること.
     */
    @Test
    public final void アクセス権限のないユーザーでschema指定がある場合に403が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account2", "password2");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.NECESSARY_PRIVILEGE_LACKING;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * ALL権限のあるユーザーでschema指定がある場合に302が返却されること.
     */
    @Test
    public final void ALL権限のあるユーザーでschema指定がある場合に302が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:all/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * 誰でも参照可能な設定でトークンを使用せずschema指定がある場合に302が返却されること.
     */
    @Test
    public final void 誰でも参照可能な設定でトークンを使用せずschema指定がある場合に302が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:all/>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * 誰でも参照可能な設定で参照権限ありのトークンを使用してschema指定がある場合に302が返却されること.
     */
    @Test
    public final void 誰でも参照可能な設定で参照権限ありのトークンを使用してschema指定がある場合に302が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:all/>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * 誰でも参照可能な設定で参照権限なしのトークンを使用してschema指定がある場合に302が返却されること.
     */
    @Test
    public final void 誰でも参照可能な設定で参照権限なしのトークンを使用してschema指定がある場合に302が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:all/>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:write/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * 誰でも参照可能な設定で不正トークンを使用してschema指定がある場合に302が返却されること.
     */
    @Test
    public final void 誰でも参照可能な設定で不正トークンを使用してschema指定がある場合に302が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:all/>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer test");

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * 不正トークンを使用してschema指定がある場合に401が返却されること.
     */
    @Test
    public final void 不正トークンを使用してschema指定がある場合に401が返却されること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer test");

            res = rest.getAcceptEncodingGzip(
                    UrlUtils.boxUrl(Setup.TEST_CELL1, UrlUtils.cellRoot("boxUrlTestSchema")),
                    requestheaders);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, res.getStatusCode());
            DcCoreException e = DcCoreAuthzException.TOKEN_PARSE_ERROR;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がなしかつクエリに指定されたスキーマのPublicトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がなしかつクエリに指定されたスキーマのPublicトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がnoneかつクエリに指定されたスキーマのPublicトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がnoneかつクエリに指定されたスキーマのPublicトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='none' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がpublicかつクエリに指定されたスキーマのPublicトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がpublicかつクエリに指定されたスキーマのPublicトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='public' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がconfidentialClientかつクエリに指定されたスキーマのConfidentialClientトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がconfidentialClientかつクエリに指定されたスキーマのConfidentialClientトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='confidential' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME);
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がなしかつクエリに指定されたスキーマでないPublicトークンを使用してクエリに指定されたスキーマのボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がなしかつクエリに指定されたスキーマでないPublicトークンを使用してクエリに指定されたスキーマのボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaAuthz(Setup.TEST_CELL_SCHEMA1));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がnoneかつクエリに指定されたスキーマでないPublicトークンを使用してクエリに指定されたスキーマのボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がnoneかつクエリに指定されたスキーマでないPublicトークンを使用してクエリに指定されたスキーマのボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='none' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";

            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaAuthz(Setup.TEST_CELL_SCHEMA1));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);

            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がpublicかつクエリに指定されたスキーマでないPublicトークンを使用してクエリに指定されたスキーマのボックスURLが取得できないこと.
     */
    @Test
    public final void スキーマ設定がpublicかつクエリに指定されたスキーマでないPublicトークンを使用してクエリに指定されたスキーマのボックスURLが取得できないこと() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='public' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaAuthz(Setup.TEST_CELL_SCHEMA1));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.SCHEMA_MISMATCH;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がconfidentialClientかつクエリに指定されたスキーマでないConfidentialClientトークンを使用してクエリに指定されたスキーマのボックスURLが取得できないこと.
     */
    @Test
    public final void スキーマ設定がconfidentialかつクエリに指定されたスキーマでないConfidentialClientトークンを使用してクエリに指定されたスキーマのボックスURLが取得できないこと() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='confidential' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaAuthz(Setup.TEST_CELL_SCHEMA1));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1,
                    UrlUtils.cellRoot("boxUrlTestSchema")), requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.SCHEMA_MISMATCH;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());
        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME);
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がnoneの場合にアクセストークンを使用してボックスURLが取得できないこと.
     */
    @Test
    public final void スキーマ設定がnoneの場合にアクセストークンを使用してボックスURLが取得できないこと() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='none' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.NECESSARY_PRIVILEGE_LACKING;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がpublicの場合にアクセストークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がpublicの場合にアクセストークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='public' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.NECESSARY_PRIVILEGE_LACKING;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がconfidentialの場合にアクセストークンを使用してボックスURLが取得できないこと.
     */
    @Test
    public final void スキーマ設定がconfidentialの場合にアクセストークンを使用してボックスURLが取得できないこと() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='confidential' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            String token = ResourceUtils.getMyCellLocalToken(Setup.TEST_CELL1, "account1", "password1");
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.NECESSARY_PRIVILEGE_LACKING;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            afterACLTest();
        }
    }

    /**
     * スキーマ設定をしていない場合にPublicトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定をしていない場合にPublicトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がnoneの場合にPublicトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がnoneの場合にPublicトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='none' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がpublicの場合にPublicトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がpublicの場合にPublicトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='public' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がconfidentialの場合にPublicトークンを使用してボックスURLが取得できないこと.
     */
    @Test
    public final void スキーマ設定がconfidentialの場合にPublicトークンを使用してボックスURLが取得できないこと() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='confidential' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken("client"));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_FORBIDDEN, res.getStatusCode());
            DcCoreException e = DcCoreException.Auth.INSUFFICIENT_SCHEMA_AUTHZ_LEVEL;
            checkErrorResponse(res.bodyAsJson(), e.getCode(), e.getMessage());

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell("client");
            afterACLTest();
        }
    }

    /**
     * スキーマ設定をしていない場合にConfidentialClientトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定をしていない場合にConfidentialClientトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME);
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がnoneの場合にConfidentialClientトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がnoneの場合にConfidentialClientトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='none' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME);
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がpublicの場合にConfidentialClientトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がpublicの場合にConfidentialClientトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='public' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME);
            afterACLTest();
        }
    }

    /**
     * スキーマ設定がconfidentialの場合にConfidentialClientトークンを使用してボックスURLが取得できること.
     */
    @Test
    public final void スキーマ設定がconfidentialの場合にConfidentialClientトークンを使用してボックスURLが取得できること() {
        try {
            String aclXml = String.format("<D:acl xmlns:D='DAV:' xmlns:dc='urn:x-dc1:xmlns'"
                    + " dc:requireSchemaAuthz='confidential' xml:base='%s/%s/__role/__/'>",
                    UrlUtils.getBaseUrl(), Setup.TEST_CELL1)
                    + "  <D:ace>"
                    + "    <D:principal>"
                    + "      <D:href>role1</D:href>"
                    + "    </D:principal>"
                    + "    <D:grant>"
                    + "      <D:privilege><D:read/></D:privilege>"
                    + "    </D:grant>"
                    + "  </D:ace>"
                    + "</D:acl>";
            beforeACLTest(aclXml);

            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;

            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, "Bearer "
                    + getSchemaToken(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME));

            res = rest.getAcceptEncodingGzip(UrlUtils.boxUrl(Setup.TEST_CELL1), requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
            assertEquals(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), res.getFirstHeader(HttpHeaders.LOCATION));

        } catch (DcException e) {
            fail(e.getMessage());
        } finally {
            deleteApplicationCell(OAuth2Helper.Key.CONFIDENTIAL_ROLE_NAME);
            afterACLTest();
        }
    }

    /**
     * スキーマ認証済みのトークンを取得.
     */
    private String getSchemaToken(String role) {
        String token = null;
        try {
            // アプリセルの作成
            createAppCell();

            // アカウント作成
            createAccountForAppCell();

            // ロール作成
            createRoleForAppCell(role);

            // アカウントとロールのリンク作成
            linkAccountRole(role);

            // スキーマ認証トークンを返却する
            token = getSchemaAuthz(null);

        } catch (DcException e) {
            fail("getConfidentialSchemaToken Fail : " + e.getMessage());
        }
        return token;
    }

    /**
     * アプリセルの削除.
     */
    private String deleteApplicationCell(String role) {
        String token = null;
        DcRestAdapter rest = new DcRestAdapter();
        HashMap<String, String> requestheaders = new HashMap<String, String>();
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

        // アカウントとロールのリンク削除
        unlinkAccountRole(role);

        // ロール削除
        deleteRoleForAppCell(role);

        // アカウント削除
        try {
            rest = new DcRestAdapter();
            rest.del(UrlUtils.cellCtl("boxUrlTestSchema", "Account", "account1"), requestheaders);
        } catch (DcException e) {
            System.out.println("boxUrlTestSchema/__ctl/Account('account1') delete Fail : " + e.getMessage());
        }

        // アプリセル削除
        try {
            rest = new DcRestAdapter();
            rest.del(UrlUtils.unitCtl("Cell", "boxUrlTestSchema"), requestheaders);

        } catch (DcException e) {
            System.out.println("boxUrlTestSchema delete Fail : " + e.getMessage());
        }
        return token;
    }

    /**
     * ACL関連のテスト前処理.
     * @param aclXml ACL設定情報
     */
    @SuppressWarnings("unchecked")
    private void beforeACLTest(String aclXml) {
        try {
            // Box作成
            DcRestAdapter rest = new DcRestAdapter();
            DcResponse res = null;
            HashMap<String, String> requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);
            JSONObject body = new JSONObject();
            body.put("Name", "boxUrlTest");
            body.put("Schema", UrlUtils.cellRoot("boxUrlTestSchema"));
            res = rest.post(UrlUtils.cellCtl(Setup.TEST_CELL1, "Box"), body.toJSONString(), requestheaders);
            assertEquals(HttpStatus.SC_CREATED, res.getStatusCode());

            // BoxACL設定
            rest = new DcRestAdapter();
            res = null;
            requestheaders = new HashMap<String, String>();
            requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);
            requestheaders.put("X-HTTP-Method-Override", "ACL");
            res = rest.post(UrlUtils.boxRoot(Setup.TEST_CELL1, "boxUrlTest"), aclXml, requestheaders);
            assertEquals(HttpStatus.SC_OK, res.getStatusCode());
        } catch (DcException e) {
            fail("beforeACLTest Fail : " + e.getMessage());
        }
    }

    /**
     * ACL関連のテスト後処理.
     */
    private void afterACLTest() {
        DcRestAdapter rest = new DcRestAdapter();
        DcResponse res = null;

        HashMap<String, String> requestheaders = new HashMap<String, String>();
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

        // Box削除
        try {
            res = rest.del(UrlUtils.cellCtl(Setup.TEST_CELL1, "Box", "boxUrlTest"), requestheaders);
            assertEquals(HttpStatus.SC_NO_CONTENT, res.getStatusCode());
        } catch (DcException e) {
            fail("afterACLTest Fail : " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void createAppCell() throws DcException {
        DcRestAdapter rest = new DcRestAdapter();
        DcResponse res = null;
        JSONObject body = new JSONObject();
        HashMap<String, String> requestheaders = new HashMap<String, String>();

        body.put("Name", "boxUrlTestSchema");
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);
        res = rest.post(UrlUtils.unitCtl("Cell"), body.toJSONString(), requestheaders);
        assertEquals(HttpStatus.SC_CREATED, res.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private void createAccountForAppCell() throws DcException {
        DcRestAdapter rest = new DcRestAdapter();
        DcResponse res = null;
        JSONObject body = new JSONObject();
        HashMap<String, String> requestheaders = new HashMap<String, String>();

        body.put("Name", "account1");
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);
        requestheaders.put("X-Dc-Credential", "password1");
        res = rest.post(UrlUtils.cellCtl("boxUrlTestSchema", "Account"), body.toJSONString(), requestheaders);
        assertEquals(HttpStatus.SC_CREATED, res.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private void createRoleForAppCell(String roleName) throws DcException {
        DcRestAdapter rest = new DcRestAdapter();
        DcResponse res = null;
        JSONObject body = new JSONObject();
        HashMap<String, String> requestheaders = new HashMap<String, String>();

        body.put("Name", roleName);
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);
        res = rest.post(UrlUtils.cellCtl("boxUrlTestSchema", "Role"), body.toJSONString(), requestheaders);
        assertEquals(HttpStatus.SC_CREATED, res.getStatusCode());
    }

    private void linkAccountRole(String roleName) throws DcException {
        DcRestAdapter rest = new DcRestAdapter();
        DcResponse res = null;
        HashMap<String, String> requestheaders = new HashMap<String, String>();

        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);
        String linkBody = String.format("{\"uri\":\"%s\"}",
                UrlUtils.getBaseUrl() + "/boxUrlTestSchema/__ctl/Role('" + roleName + "')");
        res = rest.post(UrlUtils.getBaseUrl() + "/boxUrlTestSchema/__ctl/Account('account1')/$links/_Role",
                linkBody, requestheaders);
        assertEquals(HttpStatus.SC_NO_CONTENT, res.getStatusCode());
    }

    private String getSchemaAuthz(String cell) throws DcException {
        if (cell == null) {
            cell = "boxUrlTestSchema";
        }
        DcRestAdapter rest = new DcRestAdapter();
        DcResponse res = null;
        HashMap<String, String> requestheaders = new HashMap<String, String>();

        // クライアントシークレット取得
        String authBody = "grant_type=password&username=account1&password=password1&dc_target="
                + UrlUtils.cellRoot(Setup.TEST_CELL1);
        res = rest.post(UrlUtils.auth(cell), authBody,
                requestheaders);
        assertEquals(HttpStatus.SC_OK, res.getStatusCode());

        // スキーマ認証
        authBody = "grant_type=password&username=account1&password=password1"
                + String.format("&client_id=%s", UrlUtils.cellRoot(cell))
                + String.format("&client_secret=%s", res.bodyAsJson().get("access_token"));
        res = rest.post(UrlUtils.auth(Setup.TEST_CELL1), authBody,
                requestheaders);
        assertEquals(HttpStatus.SC_OK, res.getStatusCode());
        return res.bodyAsJson().get("access_token").toString();
    }

    private void unlinkAccountRole(String roleName) {
        DcRestAdapter rest = new DcRestAdapter();
        HashMap<String, String> requestheaders = new HashMap<String, String>();
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

        // アカウントとロールのリンク削除
        try {
            rest.del(UrlUtils.getBaseUrl() + "/boxUrlTestSchema/__ctl/Account('account1')/$links/_Role('"
                    + roleName + "')", requestheaders);
        } catch (DcException e) {
            System.out.println("/boxUrlTestSchema/__ctl/Account('account1')/$links/_Role('"
                    + roleName + "') delete Fail : " + e.getMessage());
        }
    }

    private void deleteRoleForAppCell(String roleName) {
        DcRestAdapter rest = new DcRestAdapter();
        HashMap<String, String> requestheaders = new HashMap<String, String>();
        requestheaders.put(HttpHeaders.AUTHORIZATION, BEARER_MASTER_TOKEN);

        // ロール削除
        try {
            rest.del(UrlUtils.cellCtl("boxUrlTestSchema", "Role", roleName), requestheaders);
        } catch (DcException e) {
            System.out.println("/boxUrlTestSchema/__ctl/Role('" + roleName + "') delete Fail : " + e.getMessage());
        }

    }
}
