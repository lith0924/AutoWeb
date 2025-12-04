# AutoWeb
## 简介
简化了Web自动化操作，对selenium的一些常用自动化语句进行了封装，提供了可视化界面，可以零代码实现Web自动化测试，自动化任务，自动化爬虫，支持动态的复杂操作，支持多步骤捆绑循环操作，xpath中可以使用变量，变量支持表达式，覆盖了绝大多数自动化web场景，支持导出cookie，在操作过程中导入cookie绕过部分网站登录，可以把操作步骤导出json，导入json回写，导出均采用路径存储，无需配置复杂的数据存储环境，开箱即用，也可以根据操作步骤生成相应selenium的学习源码，支持java，python，c++，go

## 功能速览

前端页面使用gemini3生成，简洁干净，方便操作，可以拖拽修改步骤顺序

### 首页-添加操作步骤
支持多种类型操作，包括切换iframe，点击，输入，获取标签内容，导航跳转，获取当前url，键盘按键模拟，模拟键盘输入，关闭标签页，浏览器后退，遍历点击，遍历输入，遍历获取内容，复杂捆绑操作，循环任务，添加子任务，处理弹窗，导入cookie等等

可以导出json来保存操作步骤，也可以共享某个网站的自动化流程，如电商商品上架？

<img width="839" height="714" alt="9c2c8c6970d25de03c4c88e78f1770a" src="https://github.com/user-attachments/assets/b5a5a2ff-c88b-4a8f-93c4-e4c52e0489ba" />

### 导出cookie到指定目录

结合导入cookie操作使用，在部分策略的网站可以绕过登录

<img width="856" height="273" alt="0aa79df467981caf2845d291431cb1e" src="https://github.com/user-attachments/assets/21b130b4-83dd-43aa-8251-60c42f655cbe" />


### 根据步骤生成源码

基于在首页添加的操作步骤，生成一份功能相同的源码，添加依赖后可以直接运行

<img width="833" height="713" alt="963716ad8d1757a25d716bbf7133bc4" src="https://github.com/user-attachments/assets/9e450127-2831-4859-8516-7f326ec1269a" />


### 可以查看操作日志，获取标签内容

<img width="831" height="709" alt="e1401395b75adc9b5a050cf63b84b3d" src="https://github.com/user-attachments/assets/3e29ade7-2aa5-42a5-8992-dd9caf17ba94" />

## 如何使用？

1.安装jdk

2.配置maven

3.下载与chrome浏览器版本适配的chrome driver

4.启动项目，访问8082端口，http://localhost:8082

## 配置文件

application.yml，配置文件几乎什么也没有

必须修改的：谷歌驱动路径（driver-path），改为你自己的路径

其它参数：

  是否开启浏览器无头模式
  
  操作完成是否关闭浏览器
