环境
环境：httpclient+jsonpath+testng+mysql+ExtentReport(jdk8)

运行
IDE工具直接执行testng.xml(以testng形式运行)即可（ide工具需要先装好testng插件）。
maven执行：mvn test.
执行报告查看
testng.xml执行可视化报告：${workspace}/test-output/report.html
maven执行报告：${workspace}/target/test-output/report.html
api-config.xml配置
api请求根路径、请求头及初始化参数值可以在api-config上进行配置。

rootUrl: 必须的配置，api的根路径，在调用api时用于拼接，配置后，会在自动添加到用例中的url的前缀中。
headers: 非必须配置，配置后在调用api时会将对应的name:value值设置到所有请求的请求头中header-name:header-value。
			ps:后期因为token在headers中，所以增加了动态获取token的方法
params： 非必须配置，公共参数，通常放置初始化配置数据，所有用例执行前，会将params下所有的param配置进行读取并存储到公共参数池中，在用例执行时，使用特定的关键字(${param_name})可以获取。具体使用请参考下面的高级用法。
project_name: 项目名称，会在html报告中使用
<root>
	<rootUrl>http://172.18.3.113:8003</rootUrl>
	<headers>
		<!-- 换成自己实际的值 -->
		<header name="apikey" value="123456"></header>
	</headers>
	<params>
		<!-- 目前的项目不需要填写 -->
		<param name="param1" value="value1"></param>
	</params>
	<project_name>接口自动化测试报告
	</project_name>
</root>
api请求用例具体数据。除表头外，一行代表一个api用例。执行时会依次从左到右，从上到下执行。

run： 标记为‘1’时，该行数据会被读取执行。标记为‘0’时，该行数据不会被读取执行

description： 该用例描述，在报告中体现。

method： 该api测试用例的请求方法（暂只支持get,post）。上传文件时请填写为upload
			ps:现已将上传文件封装至post请求中

url： 该api测试用例的请求路径。

param： 请求方法为post时，body的内容（暂只支持json）。传参时请类似为：{"param1":"valu1","data":"param"}。上传文件时为{"param1":"valu1","file":"__bodyfile(文件相对路径)"}

verify： 对于api请求response数据的验证（可使用jsonPath进行校验）。校验多个使用“；”进行隔开。

save： 使用jsonPath对response的数据进行提取存储。

说明：

若配置文件(api-config.xml)中rootUrl为"http://xxx.com/" ，url的值为：“/apistore/aqiservice/citylist”，框架执行的时候会根据配置文件中rootUrl进行自动拼接为：http://xxx.com/apistore/aqiservice/citylist 。 若填写url填写为http作为前缀的值如：“http://www.xxx.com/s?w=test” 将不会进行拼接。

若verify填写值为：“$.errorCode=0;$.errorMessage=success”,则会校验返回值中$.errorCode的值为0，$.errorMessage的值为success，只要有一个校验错误，后面的其他校验项将停止校验。

若save值为：“id=$.userId;age=$.age”，接口实际返回内容为：“{"username":"chenwx","userId":"1000","age":"18"}”，则接口执行完成后，会将公共参数id的值存储为1000，age存储为18。公共参数可在后面的用例中进行使用。具体使用方法见下方高级用法。

高级用法
测试用例database表中可以使用‘${param_name}’占位符，在执行过程中如果判断含有占位符，则会将该值替换为公共参数里面的值，如果找不到将会报错。如：

//配置文件(api-config.xml)中params配置为：
<params>
    <param name="apikey" value="123456"></param>
</params>

//A用例执行返回为
{"username":"chenwx","userId":"1000","age":"18"}
//A用例的save值为：
username=$.username;id=$.userId

//此时若B用例中param值填写为：
{"key":"apikey","userId":"${id}","username":"${username}"}
//实际执行时会替换为：
{"key":"123456","userId":"1000","username":"username"}

测试报告：
reportng默认的用例展示是根据用例首字母排序，为了更直观的观察业务流，更改为以用例执行顺序展示