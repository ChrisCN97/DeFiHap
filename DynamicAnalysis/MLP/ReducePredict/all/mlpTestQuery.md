* The template of join queries for test: ```Select t1.c1, t2.c2 from t1 join t2 on t1.key1, t2.key2;```

* The properties of [test data](joinMlpTrainTest_L.csv) are shown below:

Records of t1 | Number of kinds in key1|Records of t2|Number of kinds in key2
:--:| :--: | :--: | :--:
5000|20|30000|16
5000|20|55000|14
10000|14|30000|16
10000|14|40000|22
10000|14|65000|16
15000|4|40000|22
20000|16|20000|16
20000|16|50000|6
25000|18|35000|23
30000|16|50000|6
35000|23|35000|23
40000|22|40000|22
40000|22|65000|16
50000|6|50000|6