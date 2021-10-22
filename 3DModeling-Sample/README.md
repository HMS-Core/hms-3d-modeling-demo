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

#### Shoes Reconstruction
**Suggestions on Shoe Modeling**
Shooting Devices
- Light box: provides a simple and pure background that does not reflect light and ensures bright and even lighting, which brings a better modeling effect.
- Turntable: requires no human labor to turn around an object for image shooting.
- Gimbal: ensures stable distance between the phone and object, accurate focus, and clear images, which brings a better modeling effect.

**Shooting Steps**
Put the turntable in the light box in which the background is simple and does not reflect light, and the lighting is bright and even. Put the shoe on the turntable. As it turns five rounds at a fixed speed, take 108 images of the shoe in total.
1. Place the phone camera at an angle of zero degrees above the shoe's horizontal axis. Take 24 images as the turntable turns one round.。

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureFirstStep.jpg" width=300 title="First Step" border=2></td>

2. Place the phone camera at an angle of 45 degrees above the shoe's horizontal axis. Take 24 images as the turntable turns one round.

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureSecondStep.jpg" width=300 title="Second Step" border=2></td>

3. Place the phone camera at an angle of 90 degrees above the shoe's horizontal axis and make sure that the camera faces the insole. Take 12 images as the turntable turns half a round.

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureThirdStep.jpg" width=300 title="Third Step" border=2></td>

4. Put the shoe on its side. Place the phone camera at an angle of zero to 15 degrees above the shoe's horizontal axis. Take 24 images as the turntable turns one round.

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureFourthStep.jpg" width=300 title="Fourth Step" border=2></td>

5. Put the shoe on its side. Place the phone camera at an angle of 45 degrees above the shoe's horizontal axis. Take 24 images as the turntable turns one round. Ensure each image contains both the shoe upper and outsole.

<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/ModelCaptureFifthStep.jpg" width=300 title="Fifth Step" border=2></td>

### Material Generation
It includes image uploading, task query and material downloadling.

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MaterialCaptureEN.png" width=320 title="capture page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MaterialUploadEN.png" width=320 title="upload page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MaterialDownloadEN.png" width=320 title="download page" border=2></td>
</tr></table>

### Motion Capture

It includes live video detection, video detection and photo detection.

<table><tr>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MotionCaptureLive.png" width=320 title="Live page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MotionCaptureVideoE.png" width=320 title="Video page" border=2></td>
<td><img src="https://github.com/HMS-Core/hms-3d-modeling-demo/blob/master/3DModeling-Sample/resources/MotionCapturePhoto.png" width=320 title="Photo page" border=2></td>
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