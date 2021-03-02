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
          width="180px"
          >
        </el-table-column>
        <el-table-column
          prop="name"
          label="Anti-Pattern Name"
          width="300px"
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
        name: "Large Table In The Left",
        des: "Do not put the table with more records in the left when using JOIN. " +
          "The duplicate associated keys in large table will increase the calculation amount when " +
          "it is located in the left of JOIN in old hive version.",
        code: {
          apCode: "-- mrtest_50 is the larger table\nselect t1.name from mrtest_50 t1 join mrtest_10 t2 on t1.city = t2.city",
          fixedCode: "select t1.name from mrtest_10 t2 join mrtest_50 t1 on t1.city = t2.city",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Greedy Selection",
        des: "Using SELECT * in a query will get redundant result which also leads to poor performance.",
        code: {
          apCode: "select * from t1;",
          fixedCode: "Select target column explicitly",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Too Many Joins",
        des: "Do not use too many JOINs in a query. JOIN is an ineffective operation. You may replace it with PARTITION and so on.",
        code: {
          apCode: "select t1.a from t1 join t2 on t1.b = t2.b join t3 on t1.b=t3.b join t4 on t1.b=t4.b",
          fixedCode: "Reduce JOIN through optimizing business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Using HAVING",
        des: "You may misuse HAVING to filter in a query without GROUP BY operation. WHERE is a better option.",
        code: {
          apCode: "select col1,col2 from \n" +
            "(select t1.col1,t1.col2,t2.col3 from t1 join t2 on t1.id = t2.id having t1.col1 >100) as t3",
          fixedCode: "select col1,col2 from \n" +
            "(select t1.col1,t1.col2,t2.col3 from t1 inner join t2 on t1.id=t2.id where t1.col1>100) t3",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Misuse Of INTERVAL",
        des: "INTERVAL and DATE_SUB( ) have similar function and you may select only one of them.",
        code: {
          apCode: "select date_sub('2020-9-16', interval 10 day) from a join b on a.id = b.id;",
          fixedCode: "select date_sub('2020-9-16',10) from a inner join b on a.id=b.id",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "SELECT inconsistent with GROUP BY",
        des: "The columns after SELECT should not missed after GROUP BY.",
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
        des: "Calculating in predicates after ON or WHERE may cause redundant calculation in each comparison." +
          "Try to use subquery instead.",
        code: {
          apCode: "select t1.col1, t2.col2 from table1 as t1 join (select t3.col3 from t3 where t3.age - 3 > 18) \n" +
            "as t2 on t1.col1  = t2.col2;",
          fixedCode: "Try to use subquery instead",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Calling Functions In Predicate",
        des: "Calling functions In predicates after ON or WHERE may also cause redundant calculation in each comparison.",
        code: {
          apCode: "select t1.col1, t2.col2 from table1 as t1 join table2 as t2 on upper(t1.col1) = t2.col2;",
          fixedCode: "Optimize business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Misuse Of Aggregation Function",
        des: "GROUP BY must be used with aggregation function in SELECT.",
        code: {
          apCode: "select col1,col2 from \n" +
            "(select t1.col1,t1.col2,t2.col3 from t1 join t2 on t1.id = t2.id) as t1 group by col1,col2;",
          fixedCode: "Optimize business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Using ORDER BY",
        des: "Using ORDER BY in a query is discouraged in Hive. You may use SORT BY instead.",
        code: {
          apCode: "select t1.name,t2.age from t1 join t2 on t1.id = t2.id order by t2.age;",
          fixedCode: "Try to use SORT BY instead",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "JOIN In Subquery",
        des: "Join and Subquery are both ineffective operation. There may be better way for querying instead of " +
          "using them both at the same time.",
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
        name: "Creating Same Table",
        des: "Creating duplicate tables which has the same column property may waste the storage.",
        code: {
          apCode: "create table mrtest_51 (name String, age int, city int)",
          fixedCode: "Optimize business logic",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Querying Without Partition",
        des: "No partition filtering is used when querying on a partitioned table will waste the partition ability on fast searching.",
        code: {
          apCode: "select name from partitiontable;",
          fixedCode: "Use partiion search in this table.",
          canShow: true
        }
      },{
        info: "Statement Anti-pattern",
        name: "Data Skew",
        des: "Uneven or asymmetric data distribution may cause unacceptably long processing time.",
        code: {
          apCode: "select t1.name from mrtest_70kskew t1 join mrtest_70kskew t2 on t1.loc = t2.loc",
          fixedCode: "Solve the data skew problem through preprocessing the data before querying it.",
          canShow: true
        }
      },{
        info: "Configuration Anti-Pattern (C-AP)",
        name: "Disabled Column Pruner",
        des: "Enable column pruner to make sure only read the columns required by the query, and ignore other columns.",
        code: {
          apCode: "hive.optimize.cp=false",
          fixedCode: "hive.optimize.cp=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabled Partition Pruner",
        des: "Partition pruner takes advantage of partition query feature.",
        code: {
          apCode: "hive.optimize.pruner=false",
          fixedCode: "hive.optimize.pruner=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabled Output Compression",
        des: "Output compression can improve hadoop performance in the field of I/O.",
        code: {
          apCode: "mapred.compress.map.output=false",
          fixedCode: "mapred.compress.map.output=true",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabled Parallelization",
        des: "Parallelization let multiple jobs run at the same time.",
        code: {
          apCode: "set hive.exec.parallel=false;\nset hive.exec.parallel=false;",
          fixedCode: "set hive.exec.parallel=true;\nset hive.exec.parallel=true;",
          canShow: false
        }
      },{
        info: "Configuration Anti-pattern",
        name: "Disabled Cost Based Optimizer",
        des: "Cost based optimizer generates efficient execution plans by examining the tables and conditions specified " +
          "in the query.",
        code: {
          apCode: "hive.cbo.enable=false",
          fixedCode: "hive.cbo.enable=true",
          canShow: false
        }
      },{
        info: "Reduce Number Anti-pattern",
        name: "Unreasonable Number Of Reduce",
        des: "Set too many or too few Reduce tasks for a query may lead to long processing time and computing resources wasting." +
          "HAPDF can recommend the number of Reduce a JOIN query should occupy.",
        code: {
          apCode: "Select mrtest_50.name \n" +
            "From mrtest_50 \n" +
            "join mrtest_10 t2 on mrtest_50.city = t2.city;",
          fixedCode: "You can get the Reduce Recommendation by detecting it.",
          canShow: true
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
            rowspan: 14,
            colspan: 1
          };
        } else if(rowIndex<14) {
          return {
            rowspan: 0,
            colspan: 0
          };
        } else if(rowIndex===14){
          return {
            rowspan: 6,
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
