package myApplication;

import gen.HplsqlBaseListener;
import gen.HplsqlParser;
import hiveUtils.HiveUtil;
import mysqlUtils.MysqlUtil;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.SyntaxTree;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import otherUtils.FromJoinTable;
import otherUtils.OrderByCondition;
import otherUtils.SelectStmt;
import webAPI.ReturnMessageEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFixListener extends HplsqlBaseListener {
    private int joinNum = 0;
    private List<String> selectItemList;
    private List<Integer> groupByFlag = new ArrayList<>();
    private List<Integer> currentSelectListNum = new ArrayList<>();
    private Map<String,String> aliasTableName = new HashMap<>();

    private STGroup group = new STGroupFile("src/main/java/test.stg");
    private ST testST = group.getInstanceOf("select_stmt");
    // Considering that there may be subselect statements, a list is used to store all occurrences of select_stmt
    private List<SelectStmt> selectStmtList = new ArrayList<>();
    private SelectStmt selectStmt = new SelectStmt();  // HQL repair, regenerate Select statement
    public ReturnMessageEntity returnMessageEntity = new ReturnMessageEntity();

    public APDB apDB = new APDB();

    @Override
    public void enterProgram(HplsqlParser.ProgramContext ctx){
        selectItemList = new ArrayList<>();
        selectStmtList.add(selectStmt);
    }

    //anti-pattern: Don't use too many JOIN
    @Override
    public void exitProgram(HplsqlParser.ProgramContext ctx){
        //Determine whether the number of joins exceeds one
//        System.out.println("There is "+joinNum+" join");
        if(joinNum > 1){
            returnMessageEntity.addSuggestion(apDB.getSug("Too many join"));
            returnMessageEntity.setJoinParams(null);
        }
        joinNum = 0;

        // Test fix
        testST.add("stmt",selectStmt);
        String res = regexCheck(testST.render());
        // Add a reminder when it is completely correct
        if(returnMessageEntity.getFixedSuggestions().size() == 0){
            returnMessageEntity.addSuggestion(apDB.getSug("Correct HQL"));
        }

        if(!apDB.isAllFixed()){
            res = null;
        }

        System.out.println("-Fixed HiveQL:\n"+res+"\n");
        returnMessageEntity.setFixedHiveql(res);
    }

    public String regexCheck(String s){
        s =dateSubIntervalCheck(s);
        return s;
    }

    // Use interval in date_sub()
    public String dateSubIntervalCheck(String s){
        s = s.toLowerCase();
        String pattern = "(date_\\S+?\\s*\\(.+?,\\s*)(interval\\s*'*(\\d*)'*\\s*day(s)?)\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);
        boolean printFlag = false;
        while(m.find( )) {
            if(!printFlag){
                returnMessageEntity.addSuggestion(apDB.getSug("Interval"));
                printFlag = true;
            }
            s = s.replaceFirst(m.group(2), m.group(3));
        }

        return s;
    }

    //anti-pattern: The selected column is not in the group by
    @Override
    public void enterSelect_stmt(HplsqlParser.Select_stmtContext ctx){
        currentSelectListNum.add(0);
    }

    //anti-pattern: The selected column is not in the group by
    @Override
    public void exitSelect_stmt(HplsqlParser.Select_stmtContext ctx){
        if(groupByFlag.size() > 0){
            // if(selectItemList.size() != 0){
            if(currentSelectListNum.get(currentSelectListNum.size()-1) != 0){
                returnMessageEntity.addSuggestion(apDB.getSug("Col in group"));
            }
            // Repair statement to add group by condition
            for(String s:selectItemList){
                selectStmt.addGroupByCondition(s);
            }

            groupByFlag.remove(groupByFlag.size() - 1);
            currentSelectListNum.remove(currentSelectListNum.size()-1);
        }
        else{
            for(int i = 0;i < currentSelectListNum.get(currentSelectListNum.size()-1);i++){
                selectItemList.remove(selectItemList.size() - 1);
            }
            currentSelectListNum.remove(currentSelectListNum.size()-1);
        }
    }

    //anti-pattern: The selected column is not in the group by
    @Override
    public void enterSelect_list_item(HplsqlParser.Select_list_itemContext ctx) {
        // For each select item, add it to selectItemList
        if(ctx.expr() == null){
            return;
        }
        if(ctx.expr().expr_agg_window_func() == null) {
            selectItemList.add(ctx.expr().getText());
            currentSelectListNum.set(currentSelectListNum.size()-1,currentSelectListNum.get(currentSelectListNum.size()-1) + 1);
        }
        // In order to repair, construct select item
        String alias = null;
        if(ctx.select_list_alias() != null){
            alias = ctx.select_list_alias().ident().getText();
        }
        selectStmt.addColumn(ctx.expr().getText(), alias);
    }

    // anti-pattern:
    // When conditions permit, the table with few entries is not placed on the left side of join,
    // or the table with many entries is placed on the right side
    @Override
    public void enterFrom_clause(HplsqlParser.From_clauseContext ctx) {
        // Reset the number of joins
//        joinNum = 0;
        String tableName1 = "";
        String tableName2 = "";
        String alias1 = "";
        String alias2 = "";
        // Join operation
        if(ctx.from_join_clause().size() != 0) {
            // Both tables are ordinary tables
            if(ctx.from_table_clause().from_table_name_clause() != null && ctx.from_join_clause(0).from_table_clause().from_table_name_clause() != null){
                tableName1 = ctx.from_table_clause().from_table_name_clause().table_name().getText();
                if(ctx.from_table_clause().from_table_name_clause().from_alias_clause() != null){
                    alias1 = ctx.from_table_clause().from_table_name_clause().from_alias_clause().ident().getText();
                }else{
                    alias1 = null;
                }
                aliasTableName.put(alias1,tableName1);

                // Repair process to build table token
                FromJoinTable tempTable1 = new FromJoinTable();
                tempTable1.setNameAlias(new String[]{tableName1,alias1});
                selectStmt.addTable(tempTable1);
                tableName2 = ctx.from_join_clause(0).from_table_clause().from_table_name_clause().table_name().getText();
                if(ctx.from_join_clause(0).from_table_clause().from_table_name_clause().from_alias_clause() != null){
                    alias2 = ctx.from_join_clause(0).from_table_clause().from_table_name_clause().from_alias_clause().ident().getText();
                }else{
                    alias2 = null;
                }
                FromJoinTable tempTable2 = new FromJoinTable();
                tempTable2.setNameAlias(new String[]{tableName2,alias2});
                aliasTableName.put(alias2,tableName2);

                // Repair process to build table token
                tempTable2.setJoinType(ctx.from_join_clause(0).from_join_type_clause().getText());
                tempTable2.setOnCondition(getAllBoolExpr(ctx.from_join_clause(0).bool_expr()));
//                selectStmt.addTable(tempTable2);


                if(!MysqlUtil.compareTwoTableRowNum(tableName1, tableName2)){
                    //System.out.println(tableName1 + " " + tableName2);
                    //System.out.println(MysqlUtil.compareTwoTableRowNum(tableName1,tableName2));
                    returnMessageEntity.addSuggestion(apDB.getSug("Less left join"));
                    selectStmt.setDataImbalanced(true);
                };

            }
            // In case of subselect
            else if(ctx.from_table_clause().from_table_name_clause() != null){
                tableName1 = ctx.from_table_clause().from_table_name_clause().table_name().getText();
                if(ctx.from_table_clause().from_table_name_clause().from_alias_clause() != null){
                    alias1 = ctx.from_table_clause().from_table_name_clause().from_alias_clause().ident().getText();
                }else{
                    alias1 = null;
                }
                aliasTableName.put(alias1,tableName1);

                FromJoinTable tempTable1 = new FromJoinTable();
                tempTable1.setNameAlias(new String[]{tableName1,alias1});
                selectStmt.addTable(tempTable1);
            }
        }
        // Only one table exists
        else if(ctx.from_table_clause().from_table_name_clause() != null){
            tableName1 = ctx.from_table_clause().from_table_name_clause().table_name().getText();
            if(ctx.from_table_clause().from_table_name_clause().from_alias_clause() != null){
                alias1 = ctx.from_table_clause().from_table_name_clause().from_alias_clause().ident().getText();
            }
            else{
                alias1 = null;
            }
            aliasTableName.put(alias1,tableName1);

            // Repair process to build table token
            FromJoinTable tempTable1 = new FromJoinTable();
            tempTable1.setNameAlias(new String[]{tableName1,alias1});
            selectStmt.addTable(tempTable1);
        }
        else if(ctx.from_table_clause().from_subselect_clause() != null){
            String subSelectAlias;
            if (ctx.from_table_clause().from_subselect_clause().from_alias_clause() != null) {
                subSelectAlias = ctx.from_table_clause().from_subselect_clause().from_alias_clause().ident().getText();
            } else {
                subSelectAlias = null;
            }
            FromJoinTable tempTable = new FromJoinTable();
            SelectStmt currSelectStmt = new SelectStmt();
            selectStmtList.add(currSelectStmt);
            tempTable.setSubSelectStmt(currSelectStmt);
            tempTable.setSubSelectAlias(subSelectAlias);
            tempTable.setSubSelect(true);
            selectStmt.addTable(tempTable);
            selectStmt = currSelectStmt;
        }

        tableName = ctx.getStop().getText();
    }

    @Override
    public void exitFrom_clause(HplsqlParser.From_clauseContext ctx) {
//        // Determine whether the number of joins exceeds one
//        System.out.println("There is "+joinNum+" join");
//        if(joinNum > 1){
//            System.out.println("不要过多使用join");
//        }
//        joinNum = 0;
    }

    //anti-pattern: Excessive use of join
    //anti-pattern: Do not perform operations in the join clause
    //anti-pattern: Join fields of different data types
    Boolean hasJoin = false;
    @Override
    public void enterFrom_join_clause(HplsqlParser.From_join_clauseContext ctx) {
        hasJoin = true;
        // Accumulate the number of joins
        joinNum = joinNum + 1;

        // Fix the structure from_join_clause
        if(ctx.from_table_clause().from_table_name_clause() != null) {
            String tableName = ctx.from_table_clause().from_table_name_clause().table_name().getText();
            String tableAlias;
            if (ctx.from_table_clause().from_table_name_clause().from_alias_clause() != null) {
                tableAlias = ctx.from_table_clause().from_table_name_clause().from_alias_clause().ident().getText();
            } else {
                tableAlias = null;
            }
            FromJoinTable tempTable = new FromJoinTable();
            tempTable.setNameAlias(new String[]{tableName, tableAlias});
            tempTable.setJoinType(ctx.from_join_type_clause().getText());
            tempTable.setOnCondition(getAllBoolExpr(ctx.bool_expr()));
            selectStmt.addTable(tempTable);

            // Fix the order of the two tables
            if(selectStmt.getDataImbalanced() != null) {
                selectStmt.switchJoinTables();
            }
        }
        else if(ctx.from_table_clause().from_subselect_clause() != null){
            String subSelectAlias;
            if (ctx.from_table_clause().from_subselect_clause().from_alias_clause() != null) {
                subSelectAlias = ctx.from_table_clause().from_subselect_clause().from_alias_clause().ident().getText();
            } else {
                subSelectAlias = null;
            }
            FromJoinTable tempTable = new FromJoinTable();
            SelectStmt currSelectStmt = new SelectStmt();
            selectStmtList.add(currSelectStmt);
            tempTable.setSubSelectStmt(currSelectStmt);
            tempTable.setSubSelectAlias(subSelectAlias);
            tempTable.setSubSelect(true);
            tempTable.setJoinType(ctx.from_join_type_clause().getText());
            tempTable.setOnCondition(getAllBoolExpr(ctx.bool_expr()));
            selectStmt.addTable(tempTable);
            selectStmt = currSelectStmt;
        }



        if(ctx.T_ON() != null) {
            // Determine whether there is  + / - / * / / in join clause
            HplsqlParser.Bool_expr_binaryContext boolBinaryContext;

            if(ctx.bool_expr().bool_expr_logical_operator() != null){
                return;
            }
            // no parentheses on either side of the expression
            else if (ctx.bool_expr().T_OPEN_P() == null) {
                boolBinaryContext = ctx.bool_expr().bool_expr_atom().bool_expr_binary();
            }
            // Parentheses around the expression
            else {
                boolBinaryContext = ctx.bool_expr().bool_expr(0).bool_expr_atom().bool_expr_binary();
            }

            HplsqlParser.ExprContext leftSymbol = boolBinaryContext.expr(0);
            // Handle cases where parentheses exist in subexpressions
            if(leftSymbol.getChild(0).getText().equals("(")){
                leftSymbol = leftSymbol.expr(0);
            }
            HplsqlParser.ExprContext rightSymbol = boolBinaryContext.expr(1);
            if(rightSymbol.getChild(0).getText().equals("(")){
                rightSymbol = rightSymbol.expr(0);
            }

            String[] table1 = leftSymbol.getChild(0).getText().split("\\.");
            String[] table2 = rightSymbol.getChild(0).getText().split("\\.");
            if(table1.length >= 2 || table2.length >= 2){
                String tableName1 = table1[0];
                String column1 = table1[1];
                String tableName2 = table2[0];
                String column2 = table2[1];
                tableName1 = aliasTableName.get(tableName1) == null?tableName1:aliasTableName.get(tableName1);
                tableName2 = aliasTableName.get(tableName2) == null?tableName2:aliasTableName.get(tableName2);
                List<String> joinParams = new ArrayList<>();
                joinParams.add(tableName1);
                joinParams.add(column1);
                joinParams.add(tableName2);
                joinParams.add(column2);
                returnMessageEntity.setJoinParams(joinParams);
            }

        }
    }

    @Override
    public void exitFrom_join_clause(HplsqlParser.From_join_clauseContext ctx) {
        hasJoin = false;
    }

    public String getAllBoolExpr(HplsqlParser.Bool_exprContext boolExpr){
        if(boolExpr.bool_expr_logical_operator() != null){
            return getAllBoolExpr(boolExpr.bool_expr(0)) +" "+boolExpr.bool_expr_logical_operator().getText()
                    +" "+getAllBoolExpr(boolExpr.bool_expr(1));
        }
        else if(boolExpr.bool_expr_atom() != null){
            return boolExpr.bool_expr_atom().bool_expr_binary().getText();
        }
        else{
            return getAllBoolExpr(boolExpr.bool_expr(0));
        }
    }

    //anti-pattern: Do not perform operations in where clause
    @Override
    public void enterWhere_clause(HplsqlParser.Where_clauseContext ctx) {
        hasWhere = true;
        selectStmt.setWhereCondition(getAllBoolExpr(ctx.bool_expr()));
    }

    @Override
    public void exitWhere_clause(HplsqlParser.Where_clauseContext ctx) {
        hasWhere = false;
    }

    // use select *
    @Override
    public void enterSelect_list_asterisk(HplsqlParser.Select_list_asteriskContext ctx){
        returnMessageEntity.addSuggestion(apDB.getSug("Select *"));
        selectStmt.addColumn("column1",null);
        selectStmt.addColumn("...",null);
        selectStmt.addColumn("columnN",null);
    }

    // Use having for filtering
    @Override
    public void enterNon_reserved_words(HplsqlParser.Non_reserved_wordsContext ctx){
        if(ctx.getText().equalsIgnoreCase("having")){
            returnMessageEntity.addSuggestion(apDB.getSug("Having"));
        }
    }

    public String getChildString(ParseTree tree) {
        if (tree.getChildCount() == 0) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < tree.getChildCount(); ++i) {
                builder.append(tree.getChild(i).getText());
                builder.append(" ");
            }

            return builder.toString();
        }
    }

    @Override
    public void enterHaving_clause(HplsqlParser.Having_clauseContext ctx){
        if(groupByFlag.size() == 0){
            selectStmt.setWhereCondition(ctx.bool_expr().getText());
            returnMessageEntity.addSuggestion(apDB.getSug("Having"));
        }else{
            selectStmt.setHavingCondition(getChildString(ctx.bool_expr().getChild(0).getChild(0)));
        }
    }

    // use order by
    @Override
    public void enterOrder_by_clause(HplsqlParser.Order_by_clauseContext ctx){
        returnMessageEntity.addSuggestion(apDB.getSug("Order by"));

        // Repair process construction Order by
        List<ParseTree> exprContext = ctx.children;
        // Remove order and by
        for(int i = 0;i<2;i++){
            exprContext.remove(0);
        }
        OrderByCondition condition = new OrderByCondition();
        for(ParseTree tree : exprContext){
            if(tree.getText().equalsIgnoreCase("asc") || tree.getText().equalsIgnoreCase("desc")){
                condition.setOrder(tree.getText());
                selectStmt.addOrderByCondition(condition);
            }
            else if(!tree.getText().equalsIgnoreCase(",")){
                if(condition.getOrder() == null){
                    selectStmt.addOrderByCondition(condition);
                }
                condition = new OrderByCondition();
                condition.setExpr(tree.getText());
            }
        }
        if(condition.getOrder() == null){
            selectStmt.addOrderByCondition(condition);
        }
    }

    // Use interval in date_sub()
    private boolean intervalInDatesub = false;

    @Override
    public void enterExpr_interval(HplsqlParser.Expr_intervalContext ctx){
        intervalInDatesub = true;
    }

    // group by is not used with aggregate functions
    // anti-pattern: The selected column is not in the group by
    // Use having to filter, or DONOT if matched with group by
    private boolean isGather = false;

    @Override
    public void enterGroup_by_clause(HplsqlParser.Group_by_clauseContext ctx){
        if(!isGather){
            returnMessageEntity.addSuggestion(apDB.getSug("Agg with group"));
        }
        groupByFlag.add(1);
        List<HplsqlParser.ExprContext> exprContext = ctx.expr();
        // Traverse the expr list after the group and delete its elements from selectItemList
        for(HplsqlParser.ExprContext expr:exprContext){

            // Repair statement construction group by condition
            selectStmt.addGroupByCondition(expr.getText());

            if(selectItemList.contains(expr.getText())){
                selectItemList.remove(expr.getText());
                currentSelectListNum.set(currentSelectListNum.size()-1,currentSelectListNum.get(currentSelectListNum.size()-1) - 1);
            }

        }
    }

    // Use count(distinct) in large tables frequently
    @Override
    public void enterExpr_agg_window_func(HplsqlParser.Expr_agg_window_funcContext ctx){
        if(ctx.getText().toLowerCase().contains("count(distinct")){
            returnMessageEntity.addSuggestion(apDB.getSug("Count distinct"));
        }
        isGather = true;
    }

    // The data types after then and else in the case statement are inconsistent
    // Judge integer (int)
    private boolean isInteger(String t) {
        if (null == t|| "".equals(t)) {
            return false;
        }
        Pattern chk = Pattern.compile("^[-\\+]?[\\d]*$");
        return  chk.matcher(t).matches();
    }

    @Override
    public void enterExpr_case_searched(HplsqlParser.Expr_case_searchedContext ctx){
        if(ctx.getChild(0).getText().equalsIgnoreCase("case")
        && ctx.getChild(1).getText().equalsIgnoreCase("when")){
            String s1 = ctx.getChild(4).getText();
            String s2 = ctx.getChild(6).getText();
            boolean isDouble1 = !isInteger(s1) || s1.contains("/");
            boolean isDouble2 = !isInteger(s2) || s2.contains("/");
            if(isDouble1 != isDouble2){
                returnMessageEntity.addSuggestion(apDB.getSug("Dif then else"));
            }
        }
    }

    // Create multiple identical tables
    HashSet<String> colName = new HashSet<>();
    String createTableName = null;
    
    @Override
    public void enterTable_name(HplsqlParser.Table_nameContext ctx) {
        createTableName = ctx.getText();
    }
    
    @Override
    public void enterCreate_table_columns_item(HplsqlParser.Create_table_columns_itemContext ctx){
        colName.add(ctx.getStart().getText());
    }

    @Override
    public void exitCreate_table_stmt(HplsqlParser.Create_table_stmtContext ctx){
        String table = MysqlUtil.hasSameTable(createTableName, colName);
        if(table != null){
            returnMessageEntity.addSuggestion(apDB.getSug("Similar table", createTableName, table));
        }
    }

    // No partition query is used on a partitioned table
    private boolean hasWhere=false;
    List<String> whereItemList = new ArrayList<>();

    private String tableName;
    @Override
    public void exitSubselect_stmt(HplsqlParser.Subselect_stmtContext ctx){
        HashSet<String> partCol = MysqlUtil.partitionCheck(tableName, whereItemList);
        if(partCol != null){
            returnMessageEntity.addSuggestion(apDB.getSug("Use partition"));
            String whereCd = selectStmt.getWhereCondition();
            StringBuilder whereCondition = new StringBuilder(whereCd==null ? "" : whereCd);
            boolean isFirst = true;
            for(String part : partCol){
                if(isFirst){
                    System.out.print(part);
                    isFirst = false;
                }else{
                    System.out.print(", "+part);
                }
                if(whereCondition.length() != 0){
                    whereCondition.append(" and ");
                }
                whereCondition.append(part).append("?");
            }
            selectStmt.setWhereCondition(whereCondition.toString());
        }
    }

    @Override
    public void enterBool_expr_atom(HplsqlParser.Bool_expr_atomContext ctx){
        if(hasWhere){
            whereItemList.add(ctx.getStart().getText());

            HplsqlParser.ExprContext leftSymbol = ctx.bool_expr_binary().expr(0);
            HplsqlParser.ExprContext rightSymbol = ctx.bool_expr_binary().expr(1);
            // Determine whether a function is used in the Boolean expression after where
            if(leftSymbol.expr_func() == null && leftSymbol.expr_agg_window_func() == null
                    && rightSymbol.expr_func() == null && rightSymbol.expr_agg_window_func() == null){
                // do nothing
            }
            else{
                returnMessageEntity.addSuggestion(apDB.getSug("Func in pred"));
            }

            // Determine whether four operations are performed in the Boolean expression after where
            if(leftSymbol.T_ADD() == null && leftSymbol.T_SUB() == null && leftSymbol.T_MUL() == null && leftSymbol.T_DIV() == null
                    && rightSymbol.T_ADD() == null && rightSymbol.T_SUB() == null && rightSymbol.T_MUL() == null && rightSymbol.T_DIV() == null){

            }
            else{
                returnMessageEntity.addSuggestion(apDB.getSug("Cal in where"));
            }
        }
        else if(hasJoin){
            HplsqlParser.ExprContext leftSymbol = ctx.bool_expr_binary().expr(0);
            // Handle cases where parentheses exist in subexpressions
            if(leftSymbol.getChild(0).getText().equals("(")){
                leftSymbol = leftSymbol.expr(0);
            }
            HplsqlParser.ExprContext rightSymbol = ctx.bool_expr_binary().expr(1);
            if(rightSymbol.getChild(0).getText().equals("(")){
                rightSymbol = rightSymbol.expr(0);
            }

            // Determine whether a function is used in the boolean expression after on
            if(leftSymbol.expr_func() == null && leftSymbol.expr_agg_window_func() == null
                    && rightSymbol.expr_func() == null && rightSymbol.expr_agg_window_func() == null){
                // do nothing
            }
            else{
                returnMessageEntity.addSuggestion(apDB.getSug("Func in pred"));
            }

            // Determine whether four operations are performed in the Boolean expression after on
            if (leftSymbol.T_ADD() == null && leftSymbol.T_SUB() == null && leftSymbol.T_MUL() == null && leftSymbol.T_DIV() == null
                    && rightSymbol.T_ADD() == null && rightSymbol.T_SUB() == null && rightSymbol.T_MUL() == null && rightSymbol.T_DIV() == null) {
                //什么都不干
            } else {
                returnMessageEntity.addSuggestion(apDB.getSug("Cal in join"));
            }
        }
    }

    @Override
    public void enterFrom_subselect_clause(HplsqlParser.From_subselect_clauseContext ctx){
    }

    @Override
    public void exitFrom_subselect_clause(HplsqlParser.From_subselect_clauseContext ctx){
        selectStmtList.remove(selectStmtList.size()-1);
        if(selectStmtList.size()>0){
            selectStmt = selectStmtList.get(selectStmtList.size()-1);
        }
    }
}
