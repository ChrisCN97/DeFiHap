# HAPDF
A tool for automatically detecting and fixing HiveQL Anti-Patterns.

## Introduction
![system overview](pic/System%20Overview.png)

As shown in the figure, the workflow of HAPDF is mainly divided into two phases: detection and fix. HAPDF takes the HiveQL
statement and Hive configurations entered by the user as input. In the detection phase, HAPDF first converts the HiveQL 
statement into an abstract syntax tree (AST), then it traverses the AST to detect anti-patterns based on the preset detecting 
rules. In the fix phase, HAPDF constructs the HiveQL statement template according to the predefined statement generating 
gramma and replaces the wrong syntax or semantics with the correct ones based on the detection result.

You can try it at http://202.120.40.28:50012

## Project Structure
There are three main component in this project:

* [UploadAndDetect](UploadAndDetect): A webapp for calling HAPDF function.
* [StaticAnalysis](StaticAnalysis): Check static antipatterns with HiveQL 
AST and so on.
* [DynamicAnalysis](DynamicAnalysis): Recommend Reduce number in JOIN operation
and check if the operation is data skewing.

## Install
* Package two maven projects: [staticCheck](StaticAnalysis) and 
[dynamicCheck](DynamicAnalysis/hivecheck), then deploy them. 
**Attention:** copy the [src](src) folder into your deploying path.
* Run [client webapp](UploadAndDetect/UploadAndDetect) (a VUE frontend project) to use HAPDF. 
Remember to set configuration in HAPDF->Configuration->Set-Configuration first.

