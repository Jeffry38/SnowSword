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

                <el-aside style="border:1px solid #e5e7ec;padding-top: 0px;max-height: 800px;" width="30%">
                    <el-tree
                    lazy
                    :props="el_tree_props"
                    :load="(node, resolve) => loadNode(node, resolve, item)"
                    @node-click="(object,node,component) => nodeClick(node,item,index)">
                    </el-tree>
                </el-aside>

                <el-main style="padding-top: 0px;margin-left:10px;">

                  <el-descriptions title="属性" :column="1" border>
                    <el-descriptions-item label="真实路径">{{item.fileData.realpath}}</el-descriptions-item>
                    <el-descriptions-item label="文件大小">{{item.fileData.size}}</el-descriptions-item>
                    <el-descriptions-item label="权限信息">{{item.fileData.perms}}</el-descriptions-item>
                    <el-descriptions-item label="inode修改时间">{{item.fileData.inode_change_time}}</el-descriptions-item>
                    <el-descriptions-item label="文件修改时间">{{item.fileData.modification_time}}</el-descriptions-item>
                  </el-descriptions>

                  <br>

                  <el-descriptions title="预览" :colon="false">
                    <template slot="extra">
                      <el-form :inline="true">
                        <el-form-item label="文本限制(KB)">
                          <el-select 
                            size="mini" 
                            v-model="item.txtLimit" 
                            placeholder="大小" 
                            style="width:100px"
                            filterable
                            allow-create>
                            <el-option value="100"></el-option>
                            <el-option value="512"></el-option>
                          </el-select>
                        </el-form-item>

                        <el-form-item label="图片限制(KB)">
                          <el-select 
                            size="mini" 
                            v-model="item.imgLimit" 
                            placeholder="大小" 
                            style="width:100px"
                            filterable
                            allow-create>
                            <el-option value="1024"></el-option>
                            <el-option value="5120"></el-option>
                          </el-select>
                        </el-form-item>
                      </el-form>
                    </template>

                    <el-descriptions-item>
                      <el-empty v-if="!(item.fileData.is_txt || item.fileData.is_dir || item.fileData.is_img)" description="无法预览"></el-empty>

                      <el-input
                        v-if="item.fileData.is_txt" 
                        type="textarea"
                        :rows="20"
                        :readonly="true"
                        v-model="item.fileData.preview">
                      </el-input>

                      <el-image
                        v-if="item.fileData.is_img" 
                        :src="item.fileData.preview">
                      </el-image>

                      <el-table
                        v-if="item.fileData.is_dir" 
                        :data="item.fileData.preview"
                        style="width: 100%">
                        <el-table-column
                          label="名称">
                          <template slot-scope="scope">
                            <i v-if="scope.row.is_dir" class="el-icon-folder"></i>
                            <i v-if="!scope.row.is_dir" class="el-icon-document"></i>
                            <span style="margin-left: 10px">{{ scope.row.name }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          prop="size"
                          label="大小">
                        </el-table-column>
                        <el-table-column
                          prop="perms"
                          label="权限">
                        </el-table-column>
                        <el-table-column
                          prop="inode_change_time"
                          label="inode改变时间">
                        </el-table-column>
                        <el-table-column
                          prop="modification_time"
                          label="最后修改时间">
                        </el-table-column>
                      </el-table>

                    </el-descriptions-item>
                  </el-descriptions>

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
        el_tree_props:{
          label: 'name',
          isLeaf: 'is_leaf'
        },

        webshells: [
        ],

        content:{
          currentTabName: "1",
          tabIndex: 1,

          tabs:[
          ],
        },

        selectWebshellForm:{
          visible: false,
          webshellId: null,
        }

      }
    },
    methods: {
      loadNode(node, resolve, item){
        let path;
        if (node.level === 0) {
          path = "";
        }else{
          path = node.data.realpath;
        }

        request({
          url: '/webshell/loadFilesById',
          method: 'post',
          data: { 
            id:item.webshellId,
            path:path
          } 
        }).then(response => {
          resolve(response.data);
          node.loaded = false;    //展开文件夹后，依然设置loaded为false，这样重新展开时才会再次请求后端，获取最新数据
          node.isLeaf = false;    //展开空文件夹后，会变成叶子节点，这里手动设置成非叶子节点
        }).catch(error => {
          resolve([]);  //resolve空数组后，会变成叶子节点，下面设置回非叶子节点，并且设置为非展开未加载，让用户可以重新点击加载
          node.expanded = false;
          node.loaded = false;
          node.isLeaf = false;
          console.log(error);
        })
      },

      nodeClick(node,item,index){
        request({
          url: '/webshell/previewFileById',
          method: 'post',
          data: { 
            id:item.webshellId,
            path:node.data.realpath,
            txtLimit:Number(item.txtLimit),
            imgLimit:Number(item.imgLimit)
          } 
        }).then(response => {
          item.fileData = response.data;
        }).catch(error => {
          console.log(error);
          console.log(error);
        })
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
            fileData:"",
            txtLimit:100,
            imgLimit:1024
          })-1;
          this.content.currentTabName = newTabName;

          request({
            url: '/webshell/getAndUpdateInfoById',
            method: 'get',
            params: { id:webshellId } 
          }).then(response => {
            var url = response.data.url;
            var res = url.match(/https?:\/\/(.*)?\//);
            if(res){
              this.content.tabs[index].title = res[1];
            }else{
              this.content.tabs[index].title = url;
            }
            this.$forceUpdate();
          }).catch(error => {
            this.content.tabs[index].title = "加载失败";
            console.log(error);
          })



        }else{
          this.selectWebshellForm.visible = true;
        }
      },


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