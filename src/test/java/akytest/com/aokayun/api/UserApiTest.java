package akytest.com.aokayun.api;
import com.alibaba.fastjson.JSON;
import com.aokayun.api.beans.ApiDataBean;
import com.aokayun.api.configs.ApiConfig;
import com.aokayun.api.listeners.AutoTestListener;
import com.aokayun.api.listeners.RetryListener;
import com.aokayun.api.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.annotations.Optional;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
@Listeners({AutoTestListener.class,RetryListener.class})
public class UserApiTest extends TestBase{
    /**
     * api请求跟路径
     */
    private static String rootUrl;

    private static String TOKEN;

    private static String contentType;

    /**
     * 跟路径是否以‘/’结尾
     */
    private static boolean rooUrlEndWithSlash = false;

    /**
     * 所有公共header，会在发送请求的时候添加到http header上
     */
    private static Header[] publicHeaders;

    /**
     * 是否使用form-data传参 会在post与put方法封装请求参数用到
     */
    private static boolean requestByFormData = false;

    /**
     * 配置
     */
    private static ApiConfig apiConfig;

    /**
     * 所有api测试用例数据
     */
    protected List<ApiDataBean> dataList = new ArrayList<ApiDataBean>();

    private static HttpClient client;

    /**
     * 初始化测试数据
     *
     * @throws Exception
     */
    @Parameters("envName")
    @BeforeSuite
    /*
    文件路径配置
     */
    public void init(@Optional("api-config.xml") String envName) throws Exception {
        String configFilePath = Paths.get(System.getProperty("user.dir"), envName).toString();
        ReportUtil.log("api config path:" + configFilePath);
        apiConfig = new ApiConfig(configFilePath);
        // 获取基础数据
        rootUrl = apiConfig.getRootUrl();
        rooUrlEndWithSlash = rootUrl.endsWith("/");

        // 读取 param，并将值保存到公共数据map
        Map<String, String> params = apiConfig.getParams();
        setSaveDates(params);

        List<Header> headers = new ArrayList<Header>();
        apiConfig.getHeaders().forEach((key, value) -> {
            Header header = new BasicHeader(key, value);
            if (!requestByFormData && key.equalsIgnoreCase("content-type") && value.toLowerCase().contains("form-data")) {
                requestByFormData = true;
            }
            headers.add(header);
        });
        publicHeaders = headers.toArray(new Header[headers.size()]);
        client = new SSLClient();
        client.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); // 请求超时
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000); // 读取超时
    }

    @Parameters({ "excelPath", "sheetName" })
    @BeforeTest
    public void readData(@Optional("case/api-data.xlsx") String excelPath, @Optional("User") String sheetName) throws DocumentException {
        dataList = readExcelData(ApiDataBean.class, excelPath.split(";"),
                sheetName.split(";"));
    }

    /**
     * 过滤数据，run标记为1的执行。
     *
     * @return
     * @throws DocumentException
     */
    @DataProvider(name = "apiDatas")
    public Iterator<Object[]> getApiData(ITestContext context)
            throws DocumentException {
        List<Object[]> dataProvider = new ArrayList<Object[]>();
        for (ApiDataBean data : dataList) {
            if (data.isRun()) {
                dataProvider.add(new Object[]{data});
            }
        }
        return dataProvider.iterator();
    }

    @Test(dataProvider = "apiDatas")
    public void apiTest(ApiDataBean apiDataBean) throws Exception {
        ReportUtil.log("--- 开始测试 ---");
        if (apiDataBean.getUrl().startsWith("http")) {
            ReportUtil.log("接口地址：" + apiDataBean.getUrl());
        } else{
            ReportUtil.log("接口地址：" + rootUrl+apiDataBean.getUrl());
        }
        if (apiDataBean.getSleep() > 0) {
            // sleep休眠时间大于0的情况下进行暂停休眠
            ReportUtil.log(String.format("sleep %s seconds",
                    apiDataBean.getSleep()));
            Thread.sleep(apiDataBean.getSleep() * 1000);
        }
        String apiParam = buildRequestParam(apiDataBean);
        String paramType = getParamType(apiDataBean);
        // 封装请求方法
        HttpUriRequest method = parseHttpRequest(apiDataBean.getUrl(),
                apiDataBean.getMethod(), apiParam, paramType);
        String responseData;
        try {
            // 执行
            HttpResponse response = client.execute(method);

            int responseStatus = response.getStatusLine().getStatusCode();
            //从response中获取token信息；------start
            Header[] allHeaders = response.getAllHeaders();
            String s1 = response.getEntity().getContent().toString();
            Header[] tokens = response.getHeaders("iot-token");
//            System.out.println(tokens[0]);
            if (tokens != null && tokens.length > 0)
                TOKEN = tokens[0].toString().replace("iot-token:", "");
            ReportUtil.log("token:" + TOKEN);


            //从response中获取token信息；------end
            ReportUtil.log("返回状态码：" + responseStatus);
            if (apiDataBean.getStatus() != 0) {
                Assert.assertEquals(responseStatus, apiDataBean.getStatus(), "返回状态码与预期不符合!");
            }

//			else {
//				// 非2开头状态码为异常请求，抛异常后会进行重跑
//				if (200 > responseStatus || responseStatus >= 300) {
//					ReportUtil.log("返回状态码非200开头："+EntityUtils.toString(response.getEntity(), "UTF-8"));
//					throw new ErrorRespStatusException("返回状态码异常："
//							+ responseStatus);
//				}
//			}
            HttpEntity respEntity = response.getEntity();
            Header respContentType = response.getFirstHeader("Content-Type");
            if (respContentType != null && respContentType.getValue() != null
                    && (respContentType.getValue().contains("download") || respContentType.getValue().contains("octet-stream"))) {
                String conDisposition = response.getFirstHeader(
                        "Content-disposition").getValue();
                String fileType = conDisposition.substring(
                        conDisposition.lastIndexOf("."),
                        conDisposition.length());
                String filePath = "download/" + RandomUtil.getRandom(8, false)
                        + fileType;
                InputStream is = response.getEntity().getContent();
                Assert.assertTrue(FileUtil.writeFile(is, filePath), "下载文件失败。");
                // 将下载文件的路径放到{"filePath":"xxxxx"}进行返回
                responseData = "{\"filePath\":\"" + filePath + "\"}";
            } else {
//				responseData = odeUtil.decodeUnicode(EntityUtils
//						.toString(respEntity));
                responseData = EntityUtils.toString(respEntity, "UTF-8");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            method.abort();
        }
        // 输出返回数据log
        ReportUtil.log("响应数据:" + responseData);
        // 验证预期信息
        verifyResult(responseData, apiDataBean.getVerify(),
                apiDataBean.isContains());

        // 对返回结果进行提取保存。
        saveResult(responseData, apiDataBean.getSave());
    }

    private String buildRequestParam(ApiDataBean apiDataBean) {
        // 分析处理预参数 （函数生成的参数）
        String preParam = buildParam(apiDataBean.getPreParam());
        savePreParam(preParam);// 保存预存参数 用于后面接口参数中使用和接口返回验证中
        // 处理参数
        String apiParam = buildParam(apiDataBean.getParam());
        return apiParam;
    }


    private String getParamType(ApiDataBean apiDataBean) {

        return apiDataBean.getType();
    }

    /**
     * 封装请求方法
     */
    private HttpUriRequest parseHttpRequest(String url, String method, String param, String paramType) throws UnsupportedEncodingException {
        // 处理url
        url = parseUrl(url);
        ReportUtil.log("请求方式:" + method);
        ///ReportUtil.log("接口地址:" + url);
        ///ReportUtil.log("参数:" + param.replace("\r\n", "").replace("\n", ""));
        if ("post".equalsIgnoreCase(method)) {
            // 封装post方法
            HttpPost postMethod = new HttpPost(url);

            if (StringUtils.isNotBlank(paramType)) {
                postMethod.addHeader("Content-Type", paramType);
                //根据Content-Type 判断参数提交方式
                //1.表单提交
                Map<String, String> paramMap = JSON.parseObject(param, HashMap.class);
                if (paramType.contains("application/x-www-form-urlencoded")) {
                    //封装参数

                    //设置参数
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    for (Iterator iter = paramMap.keySet().iterator(); iter.hasNext(); ) {
                        String name = (String) iter.next();
                        String value = String.valueOf(paramMap.get(name));
                        nvps.add(new BasicNameValuePair(name, value));

                        System.out.println(name +"-"+value);
                    }
                    postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
                }
                //2.json提交
                if (paramType.contains("application/json")) {
                    //封装参数
                    String charSet = "UTF-8";
                    StringEntity entity = new StringEntity(param, charSet);
                    postMethod.setEntity(entity);
                }
                //3.MultipartEntity提交
                if (paramType.contains("multipart/form-data")) {
                    //如果请求头的content-type的值包含form-data时采用MultipartEntity形式
                    HttpEntity entity = parseEntity(param, true);
                    postMethod.setEntity(entity);
                }
            }
            postMethod.setHeaders(publicHeaders);
            if (StringUtils.isNotBlank(TOKEN)) {
                postMethod.addHeader("iot-token", TOKEN);
            }
            System.out.println();
            return postMethod;
        } else if ("put".equalsIgnoreCase(method)) {
            // 封装put方法
            HttpPut putMethod = new HttpPut(url);
            putMethod.setHeaders(publicHeaders);
            if (StringUtils.isNotBlank(TOKEN)) {
                putMethod.addHeader("iot-token", TOKEN);
            }
            if (StringUtils.isNotBlank(paramType)) {
                putMethod.addHeader("Content-Type", paramType);
            }
            HttpEntity entity = parseEntity(param, requestByFormData);
            putMethod.setEntity(entity);
            return putMethod;
        } else if ("delete".equalsIgnoreCase(method)) {
            // 封装delete方法
            HttpDelete deleteMethod = new HttpDelete(url);
            if (StringUtils.isNotBlank(paramType)) {
                deleteMethod.addHeader("Content-Type", paramType);
            }
            deleteMethod.setHeaders(publicHeaders);
            if (StringUtils.isNotBlank(TOKEN)) {
                deleteMethod.addHeader("iot-token", TOKEN);
            }

            return deleteMethod;
        } else {
            // 封装get方法
            HttpGet getMethod = new HttpGet(url);
            getMethod.setHeaders(publicHeaders);
            if (StringUtils.isNotBlank(TOKEN)) {
                getMethod.addHeader("iot-token", TOKEN);
            }
            if (StringUtils.isNotBlank(paramType)) {
                getMethod.addHeader("Content-Type", paramType);
            }
            return getMethod;
        }
    }

    /**
     * 格式化url,替换路径参数等。
     *
     * @param shortUrl
     * @return
     */
    private String parseUrl(String shortUrl) {
        // 替换url中的参数
        shortUrl = getCommonParam(shortUrl);
        if (shortUrl.startsWith("http")) {
            return shortUrl;
        }
        if (rooUrlEndWithSlash == shortUrl.startsWith("/")) {
            if (rooUrlEndWithSlash) {
                shortUrl = shortUrl.replaceFirst("/", "");
            } else {
                shortUrl = "/" + shortUrl;
            }
        }
        return rootUrl + shortUrl;
    }

    /**
     * 格式化参数，如果是from-data格式则将参数封装到MultipartEntity否则封装到StringEntity
     *
     * @param param    参数
     * @param formData 是否使用form-data格式
     * @return
     * @throws UnsupportedEncodingException
     */
    private HttpEntity parseEntity(String param, boolean formData) throws UnsupportedEncodingException {
        if (formData) {
            Map<String, String> paramMap = JSON.parseObject(param,
                    HashMap.class);
            MultipartEntity multiEntity = new MultipartEntity();
            for (String key : paramMap.keySet()) {
                String value = paramMap.get(key);
                Matcher m = funPattern.matcher(value);
                if (m.matches() && m.group(1).equals("bodyfile")) {
                    value = m.group(2);
                    multiEntity.addPart(key, new FileBody(new File(value)));
                } else {
                    multiEntity.addPart(key, new StringBody(paramMap.get(key)));
                }
            }
            return multiEntity;
        } else {
            return new StringEntity(param, "UTF-8");
        }
    }

}
