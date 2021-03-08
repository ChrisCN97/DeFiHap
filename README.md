# DeFiHap
A tool for automatically detecting and fixing HiveQL Anti-Patterns.

## Introduction
![system overview](pic/System%20Overview.png)

We develop DeFiHap towards automatically detecting and fixing APs in HiveQL applications. Application developers can leverage DeFiHap to create efficient, maintainable and accurate queries in big data applications.

The architecture of DeFiHap is shown in the figure. DeFiHap is mainly divided into two phases: detection and fix. DeFiHap takes the HiveQL statement and Hive configurations entered by the user as input. In the detection phase, DeFiHap first converts the HiveQL 
statement into an abstract syntax tree (AST), then it traverses the AST to detect anti-patterns based on the preset detecting 
rules. The fix phase is further divided into two phases: fixing HiveQL statement APs and fixing HiveQL configuration APs. When fixing HiveQL statement APs, DeFiHap uses an S-AP fix engine to fix the detected statement APs through rewriting a given HiveQL statement. Instead of generating the target statement from scratches, the S-AP fix engine constructs statement templates to revises a small part of the elements via a sequence editor. When fixing HiveQL configuration APs, DeFiHap suggests a general configuration recommendation for common configuration APs, and employs a trained MLP model to recommend the reducer number for JOIN queries.

You can try it at http://202.120.40.28:50012

## Project Structure
There are three main component in this project:

* [UploadAndDetect](UploadAndDetect): A webapp for calling DeFiHap function.
* [StaticAnalysis](StaticAnalysis): Check static antipatterns with HiveQL 
AST and so on.
* [DynamicAnalysis](DynamicAnalysis): Recommend Reduce number in JOIN operation
and check if the operation is data skewing.

## Install
* Package two maven projects: [staticCheck](StaticAnalysis) and 
[dynamicCheck](DynamicAnalysis/hivecheck), then deploy them. 
**Attention:** copy the [src](src) folder into your deploying path.
* Run [client webapp](UploadAndDetect/UploadAndDetect) (a VUE frontend project) to use DeFiHap. 
Remember to set configuration in DeFiHap->Configuration->Set-Configuration first.

