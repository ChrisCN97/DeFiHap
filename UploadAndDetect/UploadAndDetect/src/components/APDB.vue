<template>
  <div>
    <el-card shadow="never">
      <div slot="header">
        <span style="font-size: 18px">Anti-pattern Dictionary</span>
      </div>
      <el-table
        :data="apData"
        :span-method="spanMethod"
        border
        style="width: 100%;">
        <el-table-column
          prop="info"
          label="Anti-Pattern Type"
          width="170px"
          >
        </el-table-column>
        <el-table-column
          prop="name"
          label="Anti-Pattern Name"
          width="280px"
        >
        </el-table-column>
        <el-table-column
          prop="des"
          label="Description"
          style="word-break: keep-all;"
        >
        </el-table-column>
        <el-table-column
          prop="code"
          label="Code Example"
          width="130px"
        >
          <template slot-scope="scope">
            <div style="text-align: center">
              <el-button type="info" size="small" @click='editShow(scope.row,scope.$index)'>Show Code</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog
      title="Code Example"
      :visible.sync="isDialogVisible">
        <div>
          <el-card shadow="never">
            <div slot="header">
              <span>Anti-pattern Code</span>
            </div>
            <pre>{{ editObj.code.apCode }}</pre>
          </el-card>
          <el-card shadow="never" style="margin-top: 10px">
            <div slot="header">
              <span>Fix Suggestion</span>
            </div>
            <pre>{{ editObj.code.fixedCode }}</pre>
          </el-card>
        </div>
        <span slot="footer">
          <el-button
            v-if="editObj.code.canShow"
            style="border-color: rgb(45 123 199); background-color: rgb(45 123 199)"
            type="info" @click="toDetect(editObj.code.apCode)">
            Detect it in HAPDF
          </el-button>
          <el-button type="info" @click="isDialogVisible = false">Close</el-button>
        </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "APDB",
  data() {
    return {
      apData: [{
        info: "Statement Anti-Pattern (S-AP)",
        name: "Large Table on the Left",
        des: "Putting table with more records on the left of JOIN.",
        code: {
          apCode: "-- mrtest_50 is the larger table\nselect t1.name from mrtest_50 t1 join mrtest_10 t2 on t1.city = t2.city",
          fixedCode: "select t1.name from mrtest_10 t2 join mrtest_50 t1 on t1.city = t2.city",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Greedy Selection",
        des: "Using SELECT * which could retrieve redundant result.",
        code: {
          apCode: "select * from t1;",
          fixedCode: "Select target column explicitly",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Too Many Joins",
        des: "Using more than one JOIN operation.",
        code: {
          apCode: "select t1.a from t1 join t2 on t1.b = t2.b join t3 on t1.b=t3.b join t4 on t1.b=t4.b",
          fixedCode: "Reduce JOIN through optimizing business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Misusing HAVING",
        des: "Using HAVING without GROUP BY.",
        code: {
          apCode: "select col1,col2 from \n" +
            "(select t1.col1,t1.col2,t2.col3 from t1 join t2 on t1.id = t2.id having t1.col1 >100) as t3",
          fixedCode: "select col1,col2 from \n" +
            "(select t1.col1,t1.col2,t2.col3 from t1 inner join t2 on t1.id=t2.id where t1.col1>100) t3",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Misusing INTERVAL",
        des: "Combining INTERVAL and DATE_SUB( ) for date query.",
        code: {
          apCode: "select date_sub('2020-9-16', interval 10 day) from a join b on a.id = b.id;",
          fixedCode: "select date_sub('2020-9-16',10) from a inner join b on a.id=b.id",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "SELECT Inconsistent with GROUP BY",
        des: "Missing selected columns after GROUP BY.",
        code: {
          apCode: "select t3.col1,t3.col2,sum(t3.col1) \n" +
            "from (select t1.col1,t2.col2 from t1 join t2 on t1.id = t2.id) as t3 group by t3.col2;",
          fixedCode: "select t3.col1,t3.col2,sum(t3.col1)\n" +
            "from (select t1.col1,t2.col2 from t1 inner join t2 on t1.id=t2.id) t3 group by t3.col2,t3.col1",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Calculation In Predicate",
        des: "Calculating in predicates after ON or WHERE.",
        code: {
          apCode: "select t1.col1, t2.col2 from table1 as t1 join (select t3.col3 from t3 where t3.age - 3 > 18) \n" +
            "as t2 on t1.col1  = t2.col2;",
          fixedCode: "Try to use subquery instead",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Calling Functions In Predicate",
        des: "Calling functions in predicates after ON or WHERE.",
        code: {
          apCode: "select t1.col1, t2.col2 from table1 as t1 join table2 as t2 on upper(t1.col1) = t2.col2;",
          fixedCode: "Optimize business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "No Aggregation Function",
        des: "Lack of aggregation function in a query using GROUP BY.",
        code: {
          apCode: "select col1,col2 from \n" +
            "(select t1.col1,t1.col2,t2.col3 from t1 join t2 on t1.id = t2.id) as t1 group by col1,col2;",
          fixedCode: "Optimize business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Using ORDER BY",
        des: "Using ORDER BY instead of SORT BY.",
        code: {
          apCode: "select t1.name,t2.age from t1 join t2 on t1.id = t2.id order by t2.age;",
          fixedCode: "Try to use SORT BY instead",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "JOIN In Subquery",
        des: "Using Join in the sub-query.",
        code: {
          apCode: "select a.* from tbl1 a \n" +
            "inner join \n" +
            "(select ... from somethingelse union b select ... from anotherthing c)  d \n" +
            "on a.key1 = d.key1 \n" +
            "and a.key2 = b.key2 \n" +
            "where a.condition=1;",
          fixedCode: "create var_temp as select ... from somethingelse \n" +
            "b union select ... from anotherthing c and \n" +
            "then select a.* from something a inner join \n" +
            "from var_temp b \n" +
            "where a.key1=b.key1 \n" +
            "and a.key2=b.key2 \n" +
            "where a.condition=1;",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "Creating Duplicate Table",
        des: "Creating a table which has the same column property of another table in the database.",
        code: {
          apCode: "create table mrtest_51 (name String, age int, city int)",
          fixedCode: "Optimize business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Querying without Partition",
        des: "Querying on a partitioned table without using partition filter.",
        code: {
          apCode: "select name from partitiontable;",
          fixedCode: "Use partiion search in this table.",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Data Skew",
        des: "Querying on a dataset with a non-uniform distribution.",
        code: {
          apCode: "select t1.name from mrtest_70kskew t1 join mrtest_70kskew t2 on t1.loc = t2.loc",
          fixedCode: "Solve the data skew problem through preprocessing the data before querying it,\n" +
            "like setting hive.groupby.skewindata=true.",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Using String Matching",
        des: "Using string matching in a HiveQL.",
        code: {
          apCode: "select count(*) from olap_b_dw_hotelorder_f where create_date_wid not regexp '\\\\d{8}'",
          fixedCode: "Use search tools for hadoop.",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "THEN inconsistent with ELSE",
        des: "The types are inconsistent between the data in THEN and ELSE.",
        code: {
          apCode: "SELECT  ID, \n" +
            "        CASE WHEN col_a = 0 THEN 0\n" +
            "        ELSE (col_b / col_a) END AS math_is_fun\n" +
            "FROM    (/* derived query*/) AS x ;",
          fixedCode: "SELECT  ID, \n" +
            "        CASE WHEN col_a = 0 THEN 0.0\n" +
            "        ELSE (col_b / col_a) END AS math_is_fun\n" +
            "FROM    (/* derived query*/) AS x ;",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "No Window Function",
        des: "Normalize column data without window function.",
        code: {
          apCode: "select a.ID, \n" +
            "(((a.count1-min(a.count1))/(max(a.count1)-min(a.count1))),\n" +
            "(((a.count2-min(a.count2))/(max(a.count2)-min(a.count2)))\n" +
            "from table1 as a;",
          fixedCode: "select ID, \n" +
            "(count1-min(count1) over())/(max(count1) over()-min(count1) over()),\n" +
            "(count2-min(count2) over())/(max(count2) over() -min(count2) over())\n" +
            "from table1",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "Array Going Out of Bounds",
        des: "Repeated fields after group by or join on cause the array to go out of bounds.",
        code: {
          apCode: "SELECT *\n" +
            "FROM table_1 AS T1\n" +
            "LEFT JOIN table_2 AS T2 ON T1.col_A = T2.col_A\n" +
            "AND T1.col_A = T2.col_A",
          fixedCode: "Remove duplicate fields",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "Misusing Quotes",
        des: "Improper use of single and double quotes in Hive script.",
        code: {
          apCode: "hive -e 'select msg, count(*) as cnt from table where msg like “%abcd%” \n" +
            "group by msg order by cnt desc ;' | sed 's/[\\t]/,/g' > table.csv",
          fixedCode: "hive -e \"select msg, count(*) as cnt from table where msg like '%abcd%' \n" +
            "group by msg order by cnt desc ;\" | sed 's/[\\t]/,/g' > table.csv",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "Too Many count(distinct)",
        des: "Use count(distinct) in large tables frequently.",
        code: {
          apCode: "select count( distinct cookie )\n" +
            "from weblogs\n" +
            "where dt <= ${today}\n" +
            "  and dt >= ${90daysAgo};",
          fixedCode: "Replace it with sum...group by",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "JOIN with NULL",
        des: "JOIN between large tables, one of the tables has null or 0 values, or a certain number of values is large.",
        code: {
          apCode: "",
          fixedCode: "Null value does not participate in association, or \n" +
            "turn the empty key into a string plus a random number",
          canShow: false
        }
      },{
        info: "Statement Anti-pattern",
        name: "JOIN Different Types",
        des: "JOIN between different data types.",
        code: {
          apCode: "select count( distinct cookie )\n" +
            "from weblogs\n" +
            "where dt <= ${today}\n" +
            "  and dt >= ${90daysAgo};",
          fixedCode: "Replace it with sum...group by",
          canShow: false
        }
      },{
        info: "Configuration Anti-Pattern (C-AP)",
        name: "Disabling Column Pruner",
        des: "Not enabling column pruner configuration item.",
        code: {
          apCode: "hive.optimize.cp=false",
          fixedCode: "hive.optimize.cp=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Partition Pruner",
        des: "Not enabling partition pruner configuration item.",
        code: {
          apCode: "hive.optimize.pruner=false",
          fixedCode: "hive.optimize.pruner=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Output Compression",
        des: "Not enabling output compression configuration item.",
        code: {
          apCode: "mapred.compress.map.output=false",
          fixedCode: "mapred.compress.map.output=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Parallelization",
        des: "Not enabling parallelization configuration item.",
        code: {
          apCode: "set hive.exec.parallel=false;\nset hive.exec.parallel=false;",
          fixedCode: "set hive.exec.parallel=true;\nset hive.exec.parallel=true;",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Cost based Optimizer",
        des: "Not enabling cost based optimizer (CBO) configuration item.",
        code: {
          apCode: "hive.cbo.enable=false",
          fixedCode: "hive.cbo.enable=true",
          canShow: false
        }
      },{
        info: "Reduce Number Anti-pattern",
        name: "Inappropriate Number of Reducers",
        des: "Setting too many or too few reducers for a JOIN operation.",
        code: {
          apCode: "Select mrtest_50.name \n" +
            "From mrtest_50 \n" +
            "join mrtest_10 t2 on mrtest_50.city = t2.city;",
          fixedCode: "You can get the Reduce Recommendation by detecting it.",
          canShow: true
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Inappropriate Container Size",
        des: "Set container size setting inappropriately.",
        code: {
          apCode: "Container size sets improperly",
          fixedCode: "",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Improper Merge File Size",
        des: "The merge file size is set improperly, too large or too small",
        code: {
          apCode: "hive.merge.size.per.task sets improperly",
          fixedCode: "",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Failed Terminal Initialization",
        des: "Hive started, terminal initialization failed.",
        code: {
          apCode: "export HADOOP_USER_CLASSPATH_FIRST=false",
          fixedCode: "export HADOOP_USER_CLASSPATH_FIRST=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "ExecutorService Rejection",
        des: "Task is rejected by executorService.",
        code: {
          apCode: "",
          fixedCode: "Set hive.server2.thrift.max.worker.threads=1, hive.server2.thrift.min.worker.threads=1",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Insert without Dynamic Partition",
        des: "Inserting the partition table without setting dynamic partition.",
        code: {
          apCode: "",
          fixedCode: "Set hive.exec.dynamic.partition.mode=nonstrict",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Partial Aggregation",
        des: "Partial aggregation function on the map side is not enabled.",
        code: {
          apCode: "hive.map.aggr=false",
          fixedCode: "hive.map.aggr=true\n" +
            "hive.groupby.mapaggr.checkinterval=100000",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Map Join",
        des: "Small tables join large tables, automatically try map join is not enabled.",
        code: {
          apCode: "hive.auto.convert.join=false",
          fixedCode: "hive.auto.convert.join=true\n" +
            "hive.mapjoin.smalltable.filesize=25000000",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabling Small File Merging",
        des: "Automatic merging of small files is not enabled, and too many small files are generated.",
        code: {
          apCode: "hive.map.aggr=false",
          fixedCode: "hive.map.aggr=true\n" +
            "hive.groupby.mapaggr.checkinterval=100000",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Inappropriate Number of Mappers",
        des: "Setting too many or too few mappers for a JOIN operation.",
        code: {
          apCode: "",
          fixedCode: "Method 1: Reduce the number of maptasks by merging small files, mainly for data sources.\n" +
            "Method 2: Reduce the time for the MapReduce program to start and shut down the jvm process by setting the \n" +
            "way to reuse the jvm process: (set mapred.job.reuse.jvm.num.tasks=5) means that the map task reuses the \n" +
            "same jvm.",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Unreasonable partition settings",
        des: "Set partition improperly.",
        code: {
          apCode: "",
          fixedCode: "When the two data are relatively large, when you often filter and query according to a certain \n" +
            "field, you need to create a partition table according to the filter field",
          canShow: false
        }
      }
      ],
      listIndex: 0,
      editObj: {
        code: {
          apCode: "",
          fixedCode: ""
        }
      },
      isDialogVisible: false,
    }
  },
  methods: {
    spanMethod({ row, column, rowIndex, columnIndex }) {
      if (columnIndex === 0) {
        if (rowIndex === 0) {
          return {
            rowspan: 22,
            colspan: 1
          };
        } else if(rowIndex<22) {
          return {
            rowspan: 0,
            colspan: 0
          };
        } else if(rowIndex===22){
          return {
            rowspan: 16,
            colspan: 1
          };
        } else {
          return {
            rowspan: 0,
            colspan: 0
          };
        }
      }
    },
    editShow(row,_index){
      this.listIndex=_index;
      this.editObj=row;
      this.isDialogVisible=true;
    },
    toDetect(code){
      this.$store.commit('setHiveQL', code);
      this.$router.push({
        path: `/detect`,
      })
    }
  },
}
</script>

<style>
.el-table .cell {
  white-space: pre-line;
  word-break: keep-all;
}
</style>
