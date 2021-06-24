# 3DModelingKit-Sample
[![License](https://img.shields.io/badge/Docs-hmsguides-brightgreen)](https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/ml-introduction-4)

中文 | [English](https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/README.md)
## 目录

 * [介绍](#介绍)
 * [工程目录结构](#工程目录结构)
 * [更多场景](#更多场景)
 * [运行步骤](#运行步骤)
 * [支持的环境](#支持的环境)
 * [许可证](#许可证)


## 介绍
本示例代码目的是为了介绍3D Modeling Kit SDK的使用，其中包含以下两个能力：

### 3D物体建模
其中包括：上传采集图片、查询建模进度、下载模型。

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/resources/ModelCapture.png" width=300 title="capture page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/resources/ModelUpload.png" width=300 title="upload page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/resources/ModelDownload.png" width=300 title="download page" border=2></td>
</tr></table>


### PBR材质生成
其中包括：上传采集图片，查询材质生成进度、下载材质。

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/resources/MaterialCapture.png" width=320 title="capture page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/resources/MaterialUpload.png" width=320 title="upload page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModelingKit-Sample/resources/MaterialDownload.png" width=320 title="download page" border=2></td>
</tr></table>


详细介绍请参考[华为3D建模服务SDK](https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/ml-introduction-4)。

## 工程目录结构
app

    |-- com.huawei.hms.modeling3d
        |-- ui
            |-- modelingui
            	|-- ScanActivity // 3D建模采集上传
            	|-- CaptureMaterialActivity // 材质生成采集上传
            	|-- HistoryModelDataFragment // 3D建模下载
            	|-- HistoryMaterialDataFragment // 材质生成下载
    	|-- Modeling3dDemo

## 更多场景
华为3D建模服务（HMS 3D Modeling Kit） 提供3D建模套件，为开发者应用3D建模能力开发各类应用提供优质体验。
更多应用场景，可参考：[华为3D建模服务集成案例](https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/ml-case-banggood)。

## 运行步骤
 - 将本代码库克隆到本地。

       git clone https://github.com/HMS-Core/hms-3d-modeling-demo.git

 - 如果您还没有注册成为开发者，请在[AppGalleryConnect上注册并创建应用](https://developer.huawei.com/consumer/cn/service/josp/agc/index.html)。
 - agconnect-services.json文件请从[华为开发者社区](https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides/config-agc-0000001050990353)网站申请获取。
 - 替换工程中的sample-agconnect-services.json文件。
 - 编译并且在安卓设备或模拟器上运行。

注意：

该项目中的package name不能用于申请agconnect-services.json，您可以使用自定义package name来申请agconnect-services.json。
您只需将应用级build.gradle中的applicationId修改为与所申请的agconnect-services.json相同的package name，即可体验3D Modeling Kit云侧服务。

## 支持的环境
推荐使用Android 4.4及以上版本的设备。

##  许可证
此示例代码已获得[Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0)。