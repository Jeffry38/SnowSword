<template>
  <el-container>
    <el-main>
      <el-tabs v-model="content.currentTabName" type="card" editable @edit="handleTabsEdit">
        <el-tab-pane
          :key="item.name"
          v-for="item, index in content.tabs"
          :label="item.title"
          :name="item.name"
        >

          <el-container>
            <el-aside width="250px" style="border:1px solid #e5e7ec;padding-top: 0px;">
              <el-button 
              v-if="item.empty" 
              style="display:block;margin:10px auto 0px auto;" 
              size="mini" 
              icon="el-icon-plus" 
              circle 
              @click="addDatabaseInfo(false,item,index)"></el-button>
              <el-tree
                :props="el_tree_props"
                :load="(node, resolve) => loadNode(node, resolve, item)"
                ref="tree"
                node-key="nodeKey"
                lazy
                @node-click="(object,node,component) => nodeClick(node,item,index)"
                @node-contextmenu="(event,object,node,component) => nodeRightClick(event,node,item,index)">
              </el-tree>
            </el-aside>
            <el-main style="padding-top: 0px;">

              <el-form :inline="true">
                <el-form-item label="数据库配置">
                  <el-input size="mini" readonly v-model="item.databaseConf" placeholder="数据库配置"></el-input>
                </el-form-item>
                <el-form-item label="数据库名">
                  <el-input size="mini" readonly v-model="item.dbname" placeholder="数据库名"></el-input>
                </el-form-item>
                <el-form-item label="数据量限制">
                  <el-select 
                    size="mini" 
                    v-model="item.limit" 
                    placeholder="数量" 
                    style="width:100px"
                    filterable
                    allow-create>
                    <el-option value="100"></el-option>
                    <el-option label="无限制" value="-1"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button size="mini" type="primary" @click="executeSQL(item)">执行</el-button>
                </el-form-item>
                <el-form-item>
                  <el-button size="mini" type="primary" @click="item.SQL=''">清空</el-button>
                </el-form-item>
              </el-form>

              <el-input
                type="textarea"
                :rows="10"
                placeholder="请输入SQL语句"
                v-model="item.SQL">
              </el-input>

              <div
                v-for="(result,index) in item.results"
                :key="index">

                <el-table
                  :data="result"
                  max-height="300"
                  border
                  resizable
                  style="width: 100%;margin-top:20px;">
                  <el-table-column
                    v-for="key in Object.keys( (JSON.stringify(result)==='[]')?{}:result[0] )"
                    :key="key"

                    :label="key">
                    <template slot-scope="scope">
                      {{ scope.row[key]}}
                    </template>
                  </el-table-column>
                </el-table>

              </div>

            </el-main>
          </el-container>


        </el-tab-pane>
      </el-tabs>
      <el-empty v-if="content.tabs.length == 0" description="未选择Webshell"></el-empty>
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

    <el-dialog :title="databaseInfoForm.title" :visible.sync="databaseInfoForm.visible">
      <el-form :model="databaseInfoForm">
        <el-form-item label="地址" label-width="80px" required>
          <el-col :span="17">
            <el-input placeholder="地址" v-model="databaseInfoForm.host" autocomplete="off" style="width:100%"></el-input>
          </el-col>
          <el-col class="line" :span="1"><p style="text-align:center; display:block;margin:0px auto">:</p></el-col>
          <el-col :span="4">
            <el-input placeholder="端口" v-model.number="databaseInfoForm.port" autocomplete="off" style="width:100%"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="用户名" label-width="80px" required>
          <el-col :span="22">
            <el-input placeholder="用户名" v-model="databaseInfoForm.username" autocomplete="off" style="width:100%"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="密码" label-width="80px" required>
          <el-col :span="22">
            <el-input placeholder="密码" v-model="databaseInfoForm.password" autocomplete="off" style="width:100%"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="类型" label-width="80px" required>
          <el-col :span="22">
            <el-select 
              v-model="databaseInfoForm.type" 
              placeholder="类型" 
              style="width:100%">
              <el-option value="Mysql"></el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item label="编码" label-width="80px" required>
          <el-col :span="22">
            <el-select 
              v-model="databaseInfoForm.code" 
              placeholder="编码" 
              style="width:100%"
              filterable
              allow-create>
              <el-option value="utf8"></el-option>
              <el-option value="gbk"></el-option>
            </el-select>
          </el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button icon="el-icon-connection" @click="testConnection()">测试连接</el-button>
        <el-button @click="databaseInfoForm.visible = false">取 消</el-button>
        <el-button type="primary" @click="addOrUpdateDatabaseInfo()">确 定</el-button>
      </div>
    </el-dialog>

    <div v-show="contextMenu.visible">
      <ul id="contextMenu" :style="{left:contextMenu.left+'px',top:contextMenu.top+'px'}" class="contextmenu">
        <li class="el-icon-edit" @click="updateDatabaseInfo(false)"> 编辑配置 </li>
        <br>
        <li class="el-icon-document-add" @click="contextMenu_addDatabaseInfo"> 添加配置 </li>
        <br>
        <li class="el-icon-delete" @click="deleteDatabaseInfo"> 删除配置 </li>
      </ul>
    </div>

  </el-container>
</template>

<style>
  .contextmenu {
    border: 1px solid #ccc;
    background: #fff;
    z-index: 3000;
    position: absolute;
    list-style-type: none;
    padding: 5px 0;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
    box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, 0.2);
  }
  .contextmenu li {
    margin: 0;
    padding: 5px 10px;
  }
  .contextmenu li:hover {
    background: #f2f2f2;
    cursor: default;
  }
  .el-tree-node.is-expanded > .el-tree-node__children{
    display: inline-block;
  }
</style>

<script>
  import request from '@/utils/request'

  export default {
    data() {
      return {
        contextMenu:{
          visible: false,
          top: 0,
          left: 0,
          height:0
        },

        databaseInfoForm:{
          title: "",
          isUpdate: false,
          host: "",
          port: 3306,
          username: "",
          password: "",
          type: "",
          code: "",
          visible: false,
          webshellId:null,
          tab_index:null,
        },

        el_tree_props:{
          label: 'name',
          isLeaf: 'isleaf'
        },

        webshells: [
          // {
          //   id: 1,
          //   url: "http://qq.com/1.php",
          // }
        ],

        content:{
          currentTabName: "",
          tabIndex:0,

          tabs:[
          ],
        },

        selectWebshellForm:{
          visible: false,
          webshellId: null
        }

      }
    },
    methods: {
      loadNode(node, resolve, item) {
        if (node.level === 0) {   //返回数据库配置列表

          request({
            url: '/databaseConf/getByWebShellId',
            method: 'get',
            params: { id:item.webshellId } 
          }).then(response => {

            let url = response.data.url;
            let res = url.match(/https?:\/\/(.*)?\//);
            if(res){
              item.title = res[1];
            }else{
              item.title = url;
            }

            const databaseInfos = response.data.databases;

            let data = [];
            databaseInfos.forEach(function(value,index) {
              let tmp = {};
              tmp["name"] = value.username+"@"+value.host+":"+value.port;
              tmp["id"] = value.id;   //记录数据库配置id
              tmp["nodeKey"] = value.id;

              tmp["host"] = value.host;   //记录其他配置信息，供右键点击编辑一条配置时取用
              tmp["port"] = value.port;
              tmp["username"] = value.username;
              tmp["password"] = value.password;
              tmp["code"] = value.code;
              tmp["webshellId"] = item.webshellId;
              data.push(tmp);
            });

            if(JSON.stringify(data)==='[]'){       //判断是否是空数组
              item.empty = true;
            }else{
              item.empty = false;
            }
            this.$forceUpdate();

            resolve(data);

          }).catch(error => {
            item.title = "加载失败";
            console.log(error);
          })

        }

        if (node.level === 1) {   //点击了某条数据库配置，应该返回数据库名列表
          const databaseInfoId = node.data.id;  //数据库配置id

          request({
            url: '/databaseConf/getDatabases',
            method: 'get',
            params: { id:databaseInfoId } 
          }).then(response => {

            const databases = response.data[0];

            let data = [];
            databases.forEach(function(value,index) {
              let tmp ={};
              tmp["name"] = Object.values(value)[0];  //显示的名称、数据库名
              tmp["id"] = databaseInfoId;   //记录数据库配置id
              tmp["nodeKey"] = databaseInfoId+"."+Object.values(value)[0];
              data.push(tmp);
            });

            resolve(data);
            node.loaded = false;  //设置loaded为false，这样每次折叠后再展开时会重新请求后端获取最新数据
            node.isLeaf = false;

          }).catch(error => {
            resolve([]);  //resolve空数组后，会变成叶子节点，下面设置回非叶子节点，并且设置为非展开未加载，让用户可以重新点击加载
            node.expanded = false;
            node.loaded = false;
            node.isLeaf = false;
            console.log(error);
          })

        }

        if (node.level === 2) {   //点击了某数据库，应该返回表名列表
          const databaseInfoId = node.data.id;  //数据库配置id
          const dbname = node.data.name;  //数据库名

          request({
            url: '/databaseConf/getTables',
            method: 'get',
            params: { 
              id:databaseInfoId,
              dbname:dbname
            } 
          }).then(response => {

            const tables = response.data[0];

            let data = [];
            tables.forEach(function(value,index) {
              let tmp ={};
              tmp["name"] = Object.values(value)[0];  //显示的名称、表名
              tmp["id"] = databaseInfoId;   //记录数据库配置id
              tmp["dbname"] = dbname;   //记录数据库名
              tmp["nodeKey"] = "3."+databaseInfoId+"."+dbname+"."+Object.values(value)[0];
              data.push(tmp);
            });

            resolve(data);
            node.loaded = false;  //设置loaded为false，这样每次折叠后再展开时会重新请求后端获取最新数据
            node.isLeaf = false;

          }).catch(error => {
            resolve([]);  //resolve空数组后，会变成叶子节点，下面设置回非叶子节点，并且设置为非展开未加载，让用户可以重新点击加载
            node.expanded = false;
            node.loaded = false;
            node.isLeaf = false;
            console.log(error);
          })

        }

        if (node.level === 3) {   //点击了某表，应该返回列名列表
          const databaseInfoId = node.data.id;  //数据库配置id
          const dbname = node.data.dbname;  //数据库名
          const table = node.data.name;  //表名

          request({
            url: '/databaseConf/getColumns',
            method: 'get',
            params: { 
              id:databaseInfoId,
              dbname:dbname,
              table:table
            } 
          }).then(response => {

            const columns = response.data[0];

            let data = [];
            columns.forEach(function(value,index) {
              let tmp ={};
              tmp["name"] = value.Field+" ( "+value.Type+" ) ";  //显示的名称
              tmp["id"] = databaseInfoId;   //记录数据库配置id
              tmp["dbname"] = dbname;   //记录数据库名
              tmp["table"] = table;   //记录表名
              tmp["column"] = value.Field;  //记录列名
              tmp["isleaf"] = true; //表示是叶子节点
              data.push(tmp);
            });

            resolve(data);
            node.loaded = false;  //设置loaded为false，这样每次折叠后再展开时会重新请求后端获取最新数据
            node.isLeaf = false;

          }).catch(error => {
            resolve([]);  //resolve空数组后，会变成叶子节点，下面设置回非叶子节点，并且设置为非展开未加载，让用户可以重新点击加载
            node.expanded = false;
            node.loaded = false;
            node.isLeaf = false;
            console.log(error);
          })

        }
      },

      test(){
        console.log(1);
      },

      handleTabsEdit(targetName, action) {
        if (action === 'add') {
          this.selectWebshell(false);
        }
        if (action === 'remove') {
          let tabs = this.content.tabs;
          let activeName = this.content.currentTabName;
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
          
          this.content.currentTabName = activeName;
          this.content.tabs = tabs.filter(tab => tab.name !== targetName);
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
          let newTabName = ++this.content.tabIndex + '';
          let webshellId = this.selectWebshellForm.webshellId;

          let index = this.content.tabs.push({
            title: "加载中...",
            name: newTabName,
            webshellId:webshellId,
            limit:100,
            databaseConf:"",
            databaseConfId:0,
            dbname:"",
          })-1;
          this.content.currentTabName = newTabName;

        }else{
          this.selectWebshellForm.visible = true;
        }
      },

      addDatabaseInfo(isSubmit,item,index){
        if(isSubmit){
          request({
            url: '/databaseConf/add',
            method: 'post',
            data: { 
              webshellId:this.databaseInfoForm.webshellId,
              host:this.databaseInfoForm.host,
              port:this.databaseInfoForm.port,
              username:this.databaseInfoForm.username,
              password:this.databaseInfoForm.password,
              code:this.databaseInfoForm.code,
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '添加成功'
            });

            this.refreshData(this.databaseInfoForm.tab_index);
            this.databaseInfoForm.visible = false;

          }).catch(error => {
            console.log(error);
          })
        }else{
          this.databaseInfoForm.title = "添加配置";
          this.databaseInfoForm.isUpdate = false;
          this.databaseInfoForm.host = "";
          this.databaseInfoForm.port = 3306;
          this.databaseInfoForm.username = "";
          this.databaseInfoForm.password = "";
          this.databaseInfoForm.type = "Mysql";
          this.databaseInfoForm.code = "utf8";
          this.databaseInfoForm.visible = true;

          this.databaseInfoForm.webshellId = item.webshellId;
          this.databaseInfoForm.tab_index = index;
        }
      },

      addOrUpdateDatabaseInfo(){
        if(this.databaseInfoForm.isUpdate){
          this.updateDatabaseInfo(true);
        }else{
          this.addDatabaseInfo(true,null,null);
        }
      },

      refreshData(index){
        this.$refs.tree[index].root.loaded = false;
        this.$refs.tree[index].root.expanded = false;
        this.$refs.tree[index].root.expand();
      },

      nodeRightClick(event,node,item,index){
        this.closeMenu();
        if (node.level == 1){
          event.preventDefault();

          const offsetLeft = this.$el.getBoundingClientRect().left
          const left = event.clientX - offsetLeft;
          this.contextMenu.left = left;
          if ( (document.body.scrollHeight - event.pageY) < this.contextMenu.height ){
            this.contextMenu.top = document.body.scrollHeight - this.contextMenu.height-70;
          }else{
            this.contextMenu.top = event.pageY - 60;
          }
          this.contextMenu.visible = true;

          this.contextMenu.item = item;     //记录item和index还有node,在点击菜单中的选项时需要用到这两个值
          this.contextMenu.index = index;
          this.contextMenu.node = node;

          document.body.addEventListener('click', this.closeMenu);
        }
      },

      closeMenu() {
        this.contextMenu.visible = false;
        document.body.removeEventListener('click', this.closeMenu);
      },

      updateDatabaseInfo(isSubmit){
        if(isSubmit){

          request({
            url: '/databaseConf/update',
            method: 'post',
            data: { 
              id: this.databaseInfoForm.databaseInfoId,
              host: this.databaseInfoForm.host,
              port: this.databaseInfoForm.port,
              username: this.databaseInfoForm.username,
              password: this.databaseInfoForm.password,
              code: this.databaseInfoForm.code
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '更新成功'
            });

            this.databaseInfoForm.visible = false;
            this.refreshData(this.databaseInfoForm.index);

          }).catch(error => {
            console.log(error);
          })

        }else{
          this.databaseInfoForm.title = "修改配置";
          this.databaseInfoForm.isUpdate = true;
          this.databaseInfoForm.host = this.contextMenu.node.data.host;
          this.databaseInfoForm.port = this.contextMenu.node.data.port;
          this.databaseInfoForm.username = this.contextMenu.node.data.username;
          this.databaseInfoForm.password = this.contextMenu.node.data.password;
          this.databaseInfoForm.type = "Mysql";
          this.databaseInfoForm.code = this.contextMenu.node.data.code;
          this.databaseInfoForm.visible = true;
          this.databaseInfoForm.index = this.contextMenu.index;
          this.databaseInfoForm.databaseInfoId = this.contextMenu.node.data.id;
          this.databaseInfoForm.webshellId = this.contextMenu.node.data.webshellId;
        }
      },

      contextMenu_addDatabaseInfo(){
        this.addDatabaseInfo(false,this.contextMenu.item,this.contextMenu.index);
      },

      deleteDatabaseInfo(){
        this.$confirm('此操作将永久删除该配置, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {

          request({
            url: '/databaseConf/deleteById',
            method: 'get',
            params: { 
              id:this.contextMenu.node.data.id
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '删除成功'
            });

            this.refreshData(this.contextMenu.index);

          }).catch(error => {
            console.log(error);
          })

        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });          
        });
      },

      testConnection(){
        request({
          url: '/databaseConf/testConnection',
          method: 'post',
          data: { 
            webshellId: this.databaseInfoForm.webshellId,
            host: this.databaseInfoForm.host,
            port: this.databaseInfoForm.port,
            username: this.databaseInfoForm.username,
            password: this.databaseInfoForm.password,
            code: this.databaseInfoForm.code
          } 
        }).then(response => {
          
          if(response.data){
            this.$message({
              type: 'success',
              message: '连接成功'
            });
          }else{
            this.$message({
              type: 'error',
              message: response.message
            });  
          }

        }).catch(error => {
          console.log(error);
        })
      },

      nodeClick(node,item,index){
        if(node.level == 1) {
          item.databaseConfId = node.data.id;
          item.databaseConf = node.data.name;
        }else if(node.level == 2){
          item.databaseConfId = node.parent.data.id;
          item.databaseConf = node.parent.data.name;
          item.dbname = node.data.name;
        }else if(node.level == 3){
          item.databaseConfId = node.parent.parent.data.id;
          item.databaseConf = node.parent.parent.data.name;
          item.dbname = node.parent.data.name;
        }else if(node.level == 4){
          item.databaseConfId = node.parent.parent.parent.data.id;
          item.databaseConf = node.parent.parent.parent.data.name;
          item.dbname = node.parent.parent.data.name;
        }
      },

      executeSQL(item){
        item.results = [[]];
        this.$forceUpdate();
        request({
          url: '/databaseConf/executeSQL',
          method: 'post',
          data: { 
            id:item.databaseConfId,
            dbname:item.dbname,
            SQL:item.SQL,
            limit:Number(item.limit),
          } 
        }).then(response => {
          
          item.results = response.data;
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
    },

    mounted(){
      window.vue = this;
    },

  };
</script>