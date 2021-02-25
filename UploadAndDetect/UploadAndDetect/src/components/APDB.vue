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
        style="width: 100%">
        <el-table-column
          prop="info"
          label="Information Required"
          width="180px"
          >
        </el-table-column>
        <el-table-column
          prop="name"
          label="Anti-Pattern Name"
          width="360px"
        >
        </el-table-column>
        <el-table-column
          prop="des"
          label="Description"
        >
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "APDB",
  data() {
    return {
      apData: [{
        info: "HiveQL Statement",
        name: "JOIN In Subquery",
        des: "Using JOIN in a subquery."
      },{
        info: "HiveQL Statement",
        name: "Large Table In The Left",
        des: "Put the table with more records in the left when using JOIN."
      },{
        info: "HiveQL Statement",
        name: "Too Many Joins",
        des: "Using too many JOINs in a query."
      },{
        info: "HiveQL Statement",
        name: "Calculation After Predicate",
        des: "Calculating after predicates like ON or WHERE."
      },{
        info: "HiveQL Statement",
        name: "Greedy Selection",
        des: "Using SELECT * in a query."
      },{
        info: "HiveQL Statement",
        name: "Using ORDER BY",
        des: "Using ORDER BY in a query."
      },{
        info: "HiveQL Statement",
        name: "Using HAVING",
        des: "Using HAVING to filter in a query."
      },{
        info: "HiveQL Statement",
        name: "Calling Functions After Predicate",
        des: "Calling functions after predicates like ON or WHERE."
      },{
        info: "HiveQL Statement",
        name: "SELECT inconsistent with GROUP BY",
        des: "The columns after SELECT are missed after GROUP BY."
      },{
        info: "HiveQL Statement",
        name: "Misuse Of INTERVAL",
        des: "Using INTERVAL after DATE_SUB( )."
      },{
        info: "HiveQL Statement",
        name: "Misuse Of Aggregation Function",
        des: "Aggregation functions is not used with GROUP BY."
      },{
        info: "HiveQL Statement and Schema",
        name: "Creating Same Table",
        des: "Create multiple tables instead of using partition table."
      },{
        info: "HiveQL Statement and Schema",
        name: "Querying Without Partition",
        des: "No partition filtering is used when querying on a partitioned table."
      },{
        info: "HiveQL Statement and Schema",
        name: "Data Skew",
        des: "Data distribution is uneven or asymmetric."
      },{
        info: "HiveQL Statement and Configuration",
        name: "Unreasonable Number Of Reduce",
        des: "Set too many or too few Reduce tasks for a query."
      },{
        info: "HiveQL Statement and Configuration",
        name: "Disabled Column Pruner",
        des: "Column pruner is not enabled."
      },{
        info: "HiveQL Statement and Configuration",
        name: "Disabled Partition Pruner",
        des: "Partition pruner is not enabled."
      },{
        info: "HiveQL Statement and Configuration",
        name: "Disabled Output Compression",
        des: "Output compression configuration is not enabled."
      },{
        info: "HiveQL Statement and Configuration",
        name: "Disabled Parallelization",
        des: "Parallelization is not enabled."
      },{
        info: "HiveQL Statement and Configuration",
        name: "Disabled Cost Based Optimizer",
        des: "Cost based optimizer is not enabled."
      }]
    }
  },
  methods: {
    spanMethod({ row, column, rowIndex, columnIndex }) {
      if (columnIndex === 0) {
        if (rowIndex === 0) {
          return {
            rowspan: 11,
            colspan: 1
          };
        } else if(rowIndex<11) {
          return {
            rowspan: 0,
            colspan: 0
          };
        } else if(rowIndex===11){
          return {
            rowspan: 3,
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
    }
  },
}
</script>

<style scoped>

</style>
