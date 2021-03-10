# DeFiHap
A tool for automatically detecting and fixing HiveQL anti-patterns. Application developers can leverage DeFiHap to create efficient, maintainable and accurate queries in big data applications.

## Architecture
![system overview](pic/System%20Overview.png) 

The architecture of DeFiHap is shown in the figure. It consis ts of three components. 

DeFiHap first implements a static analysis of AST, HiveQL data and  metadata to search for the latent statement and configuration anti-patterns.
Then, for statement anti-patterns, DeFiHap suggests the fix through a template based rewriting technique. 
And for configuration anti-patterns, DeFiHap employs a machine learning based approach to recommend the desired reducer settings for different join queries.

You can try a demo version of DeFiHap on http://202.120.40.28:50012

## HiveQL Anti-Patterns
We conduct an empirical study on the posts about HiveQL programming issues in Stack Overflow.
38 HiveQL anti-patterns have been identified, which are categorized as statement and configuration anti-patterns.
DeFiHap is able to detect 25 HiveQL anti-patterns and generates the fix suggestions for 17 of them, as shown in the [list](AP.md).


## Project Structure
There are three main component in this project:

* [UploadAndDetect](UploadAndDetect): A web interface for using DeFiHap.
* [StaticAnalysis](StaticAnalysis): Check HiveQL statement and configuration anti-patterns with HiveQL AST and so on.
* [DynamicAnalysis](DynamicAnalysis): Recommend the desired reducer settings for different join queries and check if there exists data skew.

## Install
* Package two maven projects: [staticCheck](StaticAnalysis) and 
[dynamicCheck](DynamicAnalysis/hivecheck), then deploy them. **Attention:** copy the [src](src) folder into your deploying path. 
* Deploy python flask api [pred_api](DynamicAnalysis/MLP/ReducePredict/pred_api.py). **Attention:** the model checkpoint is trained on our cluster for demostration, to support recommending number of reducers in your Hive cluster, you should replace [dataset](DynamicAnalysis/MLP/ReducePredict/all/) with your join logs and re-train the [MLP model](DynamicAnalysis/MLP/ReducePredict/HivePred.py).
* Run [client web interface](UploadAndDetect/UploadAndDetect) (a VUE frontend project) to use DeFiHap. Remember to set configuration in DeFiHap->Configurations->Set-Configuration first.

## Evaluation
 We evaluate DeFiHap on 110 real-world HiveQL statements collected non-duplicated HiveQL statements from Stack Overflow, to quantify its capabilities in processing HiveQL APs. Three widely used metrics are selected: precision, recall, and F1.
 
 The dataset contains [96 HiveQL statements](StaticAnalysis/src/main/java/myApplication/TestCase.java) for evaluating the technique of detecting and fixing HiveQL APs and [14 different join queries](DynamicAnalysis/MLP/ReducePredict/all/mlpTestQuery.md) for evaluating the technique of recommending reducer settings. (The recommended number is considered correct if the difference between its corresponding execution time and the optimal one is less than 2 seconds.)
 
 We build Hive tables and load synthetic data for each statement in our cluster. The server of DeFiHap runs on Hive 2.3.4 with default configurations in a 3-node Hadoop cluster, and the Hive metadata is stored in MySQL.
 
 Here are our evaluation results.

<center><table><thead><tr><th><span>Function</span></th><th><span>Precision</span></th><th><span>Recall</span></th><th><span>F1</span></th></tr></thead><tbody><tr><td><span>Detecting HiveQL APs</span></td><td><span>100%</span></td><td><span>96.88%</span></td><td><span>98.42%</span></td></tr><tr><td><span>Fixing HiveQL APs</span></td><td><span>92.11%</span></td><td><span>-</span></td><td><span>-</span></td></tr><tr><td><span>Recommending the reducer number</span></td><td><span>78.57%</span></td><td><span>-</span></td><td><span>-</span></td></tr></tbody></table></center>


