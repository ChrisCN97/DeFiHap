import Vue from 'vue';
import Vuex from 'vuex';
Vue.use(Vuex);
const state={
  hiveQL: "select t1.name,avg(t1.score),t1.age from t1 group by t1.name; -- example HiveQL statement",
  fixedHiveql: "",
  dataImbalancedSuggest: "",
  recommendReduceNum: "",
  fixSuggestions:null,
  configFixSuggestions: null,
};
const getters = {
  getHiveQL(state) {
    return state.hiveQL;
  },
  getFixedHiveql(state) {
    return state.fixedHiveql;
  },
  getDataImbalancedSuggest(state) {
    return state.dataImbalancedSuggest;
  },
  getRecommendReduceNum(state) {
    return state.recommendReduceNum;
  },
  getFixSuggestions(state) {
    return state.fixSuggestions;
  },
  getConfigFixSuggestions(state) {
    return state.configFixSuggestions;
  }
};
const mutations = {
  setHiveQL(state, hive){
    state.hiveQL = hive;
    state.fixedHiveql = "";
    state.dataImbalancedSuggest = "";
    state.recommendReduceNum = "";
    state.fixSuggestions = null;
  },
  setFixedHiveql(state, fixedHiveql) {
    state.fixedHiveql = fixedHiveql;
  },
  setDataImbalancedSuggest(state, dataImbalancedSuggest) {
    state.dataImbalancedSuggest = dataImbalancedSuggest;
  },
  setRecommendReduceNum(state, recommendReduceNum) {
    state.recommendReduceNum = recommendReduceNum;
  },
  setFixSuggestions(state, fixSuggestions) {
    state.fixSuggestions = fixSuggestions;
  },
  setConfigFixSuggestions(state, configFixSuggestions) {
    state.configFixSuggestions = configFixSuggestions;
  }
};
const store = new Vuex.Store({
  state,
  getters,
  mutations
});

export default store;
