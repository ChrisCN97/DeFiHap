# DeFiHap
A tool for automatically detecting and fixing HiveQL Anti-Patterns. Application developers can leverage DeFiHap to create efficient, maintainable and accurate queries in big data applications.

## Architecture
![system overview](pic/System%20Overview.png) 

The architecture of DeFiHap is shown in the figure. DeFiHap is mainly divided into two phases: detection and fix. DeFiHap takes the HiveQL statement and Hive configurations entered by the user as input. In the detection phase, DeFiHap first converts the HiveQL 
statement into an abstract syntax tree (AST), then it traverses the AST to detect anti-patterns based on the preset detecting 
rules. The fix phase is further divided into two phases: fixing HiveQL statement APs and fixing HiveQL configuration APs. When fixing HiveQL statement APs, DeFiHap uses an S-AP fix engine to fix the detected statement APs through rewriting a given HiveQL statement. Instead of generating the target statement from scratches, the S-AP fix engine constructs statement templates to revises a small part of the elements via a sequence editor. When fixing HiveQL configuration APs, DeFiHap suggests a general configuration recommendation for common configuration APs, and employs a trained MLP model to recommend the reducer number for JOIN queries.

You can try it at http://202.120.40.28:50012

## HiveQL Anti-Patterns

## Project Structure
There are three main component in this project:

* [UploadAndDetect](UploadAndDetect): A webapp for calling DeFiHap function.
* [StaticAnalysis](StaticAnalysis): Check static antipatterns with HiveQL 
AST and so on.
* [DynamicAnalysis](DynamicAnalysis): Recommend Reduce number in JOIN operation
and check if the operation is data skewing.

## Install
* Package two maven projects: [staticCheck](StaticAnalysis) and 
[dynamicCheck](DynamicAnalysis/hivecheck), then deploy them.**Attention:** copy the [src](src) folder into your deploying path. 
* Deploy python flask api [pred_api](DynamicAnalysis/MLP/ReducePredict/pred_api.py). **Note:** the model checkpoint is trained on our cluster for demostration, to support recommending number of reducers in your Hive cluster, you should replace [dataset](DynamicAnalysis/MLP/ReducePredict/all/) with your join logs and re-train the [MLP model](DynamicAnalysis/MLP/ReducePredict/HivePred.py).
* Run [client webapp](UploadAndDetect/UploadAndDetect) (a VUE frontend project) to use DeFiHap. 
Remember to set configuration in DeFiHap->Configuration->Set-Configuration first.

## Evaluation
 We evaluate DeFiHap on 110 real-world HiveQL statements collected non-duplicated HiveQL statements from StackOverflow. to quantify its capabilities in processing HiveQL APs.Three widely used metrics are selected: precision, recall, and F1.

 The dataset contains 14 different join queries for evaluating the technique of recommending reducer settings(the recommended number is considered correct if its corresponding execution time differs from the optimal one within 2 seconds). 

 We build Hive tables and load synthetic data for each statement in our cluster.The server of DeFiHap runs on Hive 2.3.4 with default configurations in a 3-node Hadoop cluster, and the Hive metadata is stored in MySQL.

 Here are our evaluation results.

<table style="margin:auto"><thead><tr><th><span>Function</span></th><th><span>Precision</span></th><th><span>Recall</span></th><th><span>F1</span></th></tr></thead><tbody><tr><td><span>Detecting HiveQL APs</span></td><td><span>100%</span></td><td><span>96.88%</span></td><td><span>98.42%</span></td></tr><tr><td><span>Fixing HiveQL APs</span></td><td><span>92.11%</span></td><td><span>-</span></td><td><span>-</span></td></tr><tr><td><span>Recommending the reducer number</span></td><td><span>78.57%</span></td><td><span>-</span></td><td><span>-</span></td></tr></tbody></table>


