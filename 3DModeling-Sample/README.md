# 3DModeling-Sample
English | [中文](https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/README_ZH.md)

## Table of Contents

 * [Introduction](#introduction)
 * [Project directory structure](#Project-directory-structure)
 * [More Scenarios](#more-scenarios)
 * [Getting Started](#getting-started)
    * [Supported Environment](#supported-environment)
 * [License](#license)


## Introduction
This sample code describes how to use the 3D Modeling Kit SDK, including the following modules:

### 3D Object Reconstruction
It includes image uploading, task query and model downloadling.

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureEN.png" width=320 title="capture page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelUploadEN.png" width=320 title="upload page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelDownloadEN.png" width=320 title="download page" border=2></td>
</tr></table>

### Material Generation
It includes image uploading, task query and material downloadling.

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MaterialCaptureEN.png" width=320 title="capture page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MaterialUploadEN.png" width=320 title="upload page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MaterialDownloadEN.png" width=320 title="download page" border=2></td>
</tr></table>

#### 鞋子建模
**鞋子建模推荐方式**
推荐拍摄设备
- 灯箱：背景简单纯色无反光，光照明亮均匀，效果更好。
- 转盘：需要围绕物体360°拍摄，转盘可以代替人旋转。
- 手机稳定器：手机与物体拍摄距离稳定，对焦准确无模糊，效果更好。

**拍摄步骤**
转盘放置灯箱中，保证灯箱简单纯色无反光，光照明亮均匀，鞋子放置在转盘上，随着转盘转动，均匀拍摄图片，如下所示需要拍摄5圈，共108张照片。
1. 手机0°放置，转盘转动一圈拍摄24张。

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureFirstStep.jpg" width=300 title="First Step" border=2></td>

2. 手机45°放置，转盘转动一圈拍摄24张。

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureSecondStep.jpg" width=300 title="Second Step" border=2></td>

3. 手机90°放置，相机拍向鞋窝，转盘转动半圈拍摄12张。

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureThirdStep.jpg" width=300 title="Third Step" border=2></td>

4. 鞋子侧放，手机0°~15°，转盘转动一圈拍摄24张。

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureFourthStep.jpg" width=300 title="Fourth Step" border=2></td>

5. 鞋子侧放，手机45°放置，转盘转动一圈拍摄24张。俯视45°拍摄时，保证图片可以同时看到鞋面和外鞋底。

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureFifthStep.jpg" width=300 title="Fifth Step" border=2></td>

### Motion Capture

It includes live video detection, video detection and photo detection.

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MotionCaptureLiveEN.png" width=320 title="Live page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MotionCaptureVideoEN.png" width=320 title="Video page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MotionCapturePhotoEN.png" width=320 title="Photo page" border=2></td>
</tr></table>

For details about the HMS Core 3D Modeling SDK, please refer to [HUAWEI 3D Modeling Kit](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/ml-introduction-4)

## Project directory structure

app

    |-- com.huawei.hms.modeling3d
        |-- ui
            |-- modelingui
            	|-- ScanActivity // Uploads images and triggers a 3D object reconstruction task.
            	|-- CaptureMaterialActivity // Uploads images and triggers a material generation task.
            	|-- HistoryModelDataFragment // Downloads a generated 3D object model.
            	|-- HistoryMaterialDataFragment // Downloads the generated texture maps.
        |-- utils
        	|-- skeleton
        		|-- LocalSkeletonProcessor // Motion capture engine
    	|-- Modeling3dDemo



## More Scenarios
HUAWEI 3D Modeling Kit allows your apps to easily leverage Huawei's long-term proven expertise in 3D Modeling Kit to support diverse 3D Modeling applications throughout a wide range of industries.
For more application scenarios, see: [Huawei 3D Modeling Service Integration Cases.](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/ml-case-banggood)

## Getting Started
 - Clone the code library to the local computer.

       git clone https://github.com/HMS-Core/hms-3d-modeling-demo.git

 - If you have not registered as a developer, [register and create an app in AppGallery Connect](https://developer.huawei.com/consumer/en/service/josp/agc/index.html).
 - Obtain the agconnect-services.json file from [Huawei Developers](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-agc-0000001050990353).
 - Replace the sample-agconnect-services.json file in the project.
 - Compile and run on an Android device or simulator.

Attention:

You can only use a custom package name to apply for the agconnect-services.json file.
In this way, you only need to change the value of applicationId in HUAWEI-HMS-3DModeling-Sample\app\build.gradle to the package name used in agconnect-services.json. Then, you can use cloud services of HUAWEI ML Kit.

## Supported Environments
Devices with Android 4.4 or later are recommended.


##  License
The 3DModeling-Sample have obtained the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).