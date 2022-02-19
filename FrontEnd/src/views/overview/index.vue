<template>
  <el-container>
    <el-main>
      <el-tabs v-model="overview.currentTabName" type="card" editable @edit="handleTabsEdit">
        <el-tab-pane
          :key="item.name"
          v-for="item, index in overview.tabs"
          :label="item.title"
          :name="item.name"
        >
          <el-descriptions title="基础信息" :column="1" border>
            <template slot="extra">
              <el-tooltip class="item" effect="dark" content="刷新" placement="left">
                <el-button circle icon="el-icon-refresh" size="mini" @click="refreshInfo(index, item.id)"></el-button>
              </el-tooltip>
            </template>
            <el-descriptions-item label="URL">{{item.url}}</el-descriptions-item>
            <el-descriptions-item label="IP地址">{{item.ip_address}}</el-descriptions-item>
            <el-descriptions-item label="物理地址">{{item.address}}</el-descriptions-item>
            <el-descriptions-item label="当前用户">{{item.user}}</el-descriptions-item>
            <el-descriptions-item label="主机名">{{item.hostname}}</el-descriptions-item>
            <el-descriptions-item label="PHP版本">{{item.version}}</el-descriptions-item>
            <el-descriptions-item label="脚本路径">{{item.script_filename}}</el-descriptions-item>
            <el-descriptions-item label="系统信息">{{item.os}}</el-descriptions-item>
          </el-descriptions>

          <br>

          <el-descriptions title="进程列表" :colon="false">
            <template slot="extra">
              <el-tooltip class="item" effect="dark" content="刷新" placement="left">
                <el-button circle icon="el-icon-refresh" size="mini"  @click="refreshProcessList(index, item.id)"></el-button>
              </el-tooltip>
            </template>
            <el-descriptions-item>
              <el-input
                :readonly="true"
                type="textarea"
                :rows="10"
                v-model="item.process_list">
              </el-input>
            </el-descriptions-item>
          </el-descriptions>

        </el-tab-pane>
      </el-tabs>
      <el-empty v-if="overview.tabs.length == 0" description="未选择Webshell"></el-empty>
    </el-main>

    <el-dialog title="选择数据" :visible.sync="selectWebshellForm.visible" width="40%">
      <el-form :model="selectWebshellForm">
        <el-form-item label="URL" required>
          <el-select v-model="selectWebshellForm.webshellId" placeholder="请选择" style="width:85%">
            <el-option
              v-for="item in webshells"
              :key="item.id"
              :label="item.url"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="selectWebshellForm.visible = false">取 消</el-button>
        <el-button type="primary" @click="selectWebshell(true)">确 定</el-button>
      </div>
    </el-dialog>

  </el-container>
</template>

<style>
</style>


<script>
  import request from '@/utils/request'

  export default {
    data() {
      return {
        webshells: [
          // {
          //   id: 1,
          //   url: "http://qq.com/1.php",
          // }
        ],

        overview:{
          currentTabName: null,
          tabIndex:0,

          tabs:[
            // {title: 'Tab 1',
            // name: '1',
            
            // url:'',
            // ip_address:'',
            // address:'',
            // user:'',
            // hostname:'',
            // version:'',
            // script_filename:'',
            // os:'',
            // process_list:''}
          ],
        },

        selectWebshellForm:{
          visible: false,
          webshellId: null
        }

      }
    },
    methods: {
      handleTabsEdit(targetName, action) {
        if (action === 'add') {
          this.selectWebshell(false);
        }
        if (action === 'remove') {
          let tabs = this.overview.tabs;
          let activeName = this.overview.currentTabName;
          if (activeName === targetName) {
            tabs.forEach((tab, index) => {
              if (tab.name === targetName) {
                let nextTab = tabs[index + 1] || tabs[index - 1];
                if (nextTab) {
                  activeName = nextTab.name;
                }
              }
            });
          }
          
          this.overview.currentTabName = activeName;
          this.overview.tabs = tabs.filter(tab => tab.name !== targetName);
        }
      },

      fetchWebshellData(){
        request({
          url: '/webshell/findAll',
          method: 'get',
        }).then(response => {
          this.webshells = response.data;
        }).catch(error => {
          console.log(error);
        })
      },

      selectWebshell(isAddTab){
        if(isAddTab){
          this.selectWebshellForm.visible = false;
          let newTabName = ++this.overview.tabIndex + '';
          var id = this.selectWebshellForm.webshellId;

          var index = this.overview.tabs.push({
            title: "加载中...",
            name: newTabName,
            id:id
          })-1;
          this.overview.currentTabName = newTabName;

          request({
            url: '/webshell/getAndUpdateInfoById',
            method: 'get',
            params: { id:id } 
          }).then(response => {

            var url = response.data.url;
            var res = url.match(/https?:\/\/(.*)?\//);
            if(res){
              this.overview.tabs[index].title = res[1];
            }else{
              this.overview.tabs[index].title = url;
            }

            this.overview.tabs[index].url = response.data.url;
            this.overview.tabs[index].ip_address = response.data.ip_address;
            this.overview.tabs[index].address = response.data.address;
            this.overview.tabs[index].user = response.data.user;
            this.overview.tabs[index].hostname = response.data.hostname;
            this.overview.tabs[index].version = response.data.version;
            this.overview.tabs[index].script_filename = response.data.script_filename;
            this.overview.tabs[index].os = response.data.os;
            this.$forceUpdate();

            request({
              url: '/webshell/getProcessListById',
              method: 'get',
              params: { id:id } 
            }).then(response => {
              this.overview.tabs[index].process_list = response.data;
              this.$forceUpdate();
            }).catch(error => {
              console.log(error);
            })

          }).catch(error => {
            this.overview.tabs[index].title = "加载失败";
            console.log(error);
          })
   
        }else{
          this.selectWebshellForm.visible = true;
        }
      },

      refreshInfo(index, webshelId) {
        this.overview.tabs[index].url = '';
        this.overview.tabs[index].ip_address = '';
        this.overview.tabs[index].address = '';
        this.overview.tabs[index].user = '';
        this.overview.tabs[index].hostname = '';
        this.overview.tabs[index].version = '';
        this.overview.tabs[index].script_filename = '';
        this.overview.tabs[index].os = '';
        this.$forceUpdate();

        request({
          url: '/webshell/getAndUpdateInfoById',
          method: 'get',
          params: { id:webshelId } 
        }).then(response => {

          this.overview.tabs[index].url = response.data.url;
          this.overview.tabs[index].ip_address = response.data.ip_address;
          this.overview.tabs[index].address = response.data.address;
          this.overview.tabs[index].user = response.data.user;
          this.overview.tabs[index].hostname = response.data.hostname;
          this.overview.tabs[index].version = response.data.version;
          this.overview.tabs[index].script_filename = response.data.script_filename;
          this.overview.tabs[index].os = response.data.os;
          this.$forceUpdate();
  
        }).catch(error => {
          console.log(error);
        })
      },

      refreshProcessList(index, webshelId) {
        this.overview.tabs[index].process_list = '';
        this.$forceUpdate();

        request({
          url: '/webshell/getProcessListById',
          method: 'get',
          params: { id:webshelId } 
        }).then(response => {
          
          this.overview.tabs[index].process_list = response.data;
          this.$forceUpdate();

        }).catch(error => {
          console.log(error);
        })
      }

    },

    created(){
      this.fetchWebshellData();
      this.name = this.$route.name;
      if(this.$route.params.id){
        this.selectWebshellForm.webshellId = this.$route.params.id
        this.selectWebshell(true);
      }
    },

    watch: {
      $route(to, from) {
        if(to.name == this.name){
          this.fetchWebshellData();
          if(this.$route.params.id){
            this.selectWebshellForm.webshellId = this.$route.params.id
            this.selectWebshell(true);
          }
        }
      }
    }

  };
</script>