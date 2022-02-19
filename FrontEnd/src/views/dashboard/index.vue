<template>
  <el-container>
    <el-main>
      <el-table
        @row-contextmenu="webshellRightClick"
        border
        resizable
        :data="webshells"
        highlight-current-row
        style="width: 100%">
        <el-table-column
          property="url"
          label="URL地址">
        </el-table-column>
        <el-table-column
          property="info.ip_address"
          label="IP地址"
          width="130px">
        </el-table-column>
        <el-table-column
          property="info.address"
          label="物理位置">
        </el-table-column>
        <el-table-column
          property="note"
          label="网站备注">
        </el-table-column>
        <el-table-column
          property="create_time"
          label="创建时间"
          width="160px">
        </el-table-column>
        <el-table-column
          property="update_time"
          width="160px"
          label="更新时间">
        </el-table-column>
        <template slot="empty">
          <el-button size="mini" icon="el-icon-plus" circle @click="addWebshell(false)"></el-button>
        </template>
      </el-table>
    </el-main>

    <el-aside width="200px">
      <el-table
        @row-click="changeCategory"
        @row-contextmenu="categoryRightClick"
        border
        :data="categories"
        highlight-current-row
        style="width: 100%">
        <el-table-column
          label="分类">
          <template slot-scope="scope">
            <i class="el-icon-folder"></i>
            <span style="margin-left: 10px">{{ scope.row.name }}</span> <el-tag size="mini" style="float:right">{{ scope.row.webshellCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-aside>

    <div v-show="contextMenu.webshell.visible">
      <ul id="webshellContextMenu" :style="{left:contextMenu.webshell.left+'px',top:contextMenu.webshell.top+'px'}" class="contextmenu">
        <li class="el-icon-tickets" @click="openOverview()"> 信息概览 </li>
        <br>
        <li class="el-icon-monitor" @click="openTerminal()"> 虚拟终端 </li>
        <br>
        <li class="el-icon-files" @click="openFilemanager()"> 文件管理 </li>
        <br>
        <li class="el-icon-coin" @click="openDatabase()"> 数据操作 </li>
        <br>
        <li class="el-icon-view" @click="openWebsite"> 浏览网站 </li>
        <br><br>
        <li class="el-icon-document-add" @click="addWebshell(false)"> 添加数据 </li>
        <br>
        <li class="el-icon-edit" @click="updateWebshell(false)"> 编辑数据 </li>
        <br>
        <li class="el-icon-delete" @click="deleteWebshell"> 删除数据 </li>
        <br><br>
        <li class="el-icon-folder-checked" @click="addToCategory(false)"> 设置分类 </li>
        <br>
        <li class="el-icon-folder-delete" @click="removeFromCategory"> 移出分类 </li>
      </ul>
    </div>

    <div v-show="contextMenu.category.visible">
      <ul id="categoryContextMenu" :style="{left:contextMenu.category.left+'px',top:contextMenu.category.top+'px'}" class="contextmenu">
        <li class="el-icon-edit" @click="renameCategory"> 重命名 </li>
        <br>
        <li class="el-icon-folder-add" @click="addCategory"> 添加分类 </li>
        <br>
        <li class="el-icon-delete" @click="deleteCategory"> 删除分类 </li>
      </ul>
    </div>


    <el-dialog :title="webshellForm.title" :visible.sync="webshellForm.visible">
      <el-form :model="webshellForm">
        <el-form-item label="URL地址" label-width="120px" required>
          <el-input v-model="webshellForm.url" autocomplete="off" style="width:90%"></el-input>
        </el-form-item>
        <el-form-item label="连接密码" label-width="120px" required>
          <el-input v-model="webshellForm.password" autocomplete="off" style="width:90%" show-password></el-input>
        </el-form-item>
        <el-form-item label="网站备注" label-width="120px">
          <el-input
            type="textarea"
            style="width:90%"
            :autosize="{ minRows: 2, maxRows: 4}"
            v-model="webshellForm.note">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button icon="el-icon-connection" @click="testConnection">测试连接</el-button>
        <el-button @click="webshellForm.visible = false">取 消</el-button>
        <el-button type="primary" @click="addOrUpdateWebshell()">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="设置分类" :visible.sync="categoryForm.visible" width="40%">
      <el-form :model="categoryForm">
        <el-form-item label="分类名" required>
          <el-select v-model="categoryForm.categoryId" placeholder="请选择" style="width:85%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="categoryForm.visible = false">取 消</el-button>
        <el-button type="primary" @click="addToCategory(true)">确 定</el-button>
      </div>
    </el-dialog>

  </el-container>
</template>


<style>
  .el-aside {
    padding-top: 20px;
    padding-right: 20px;
  }

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
</style>

<script>
  import request from '@/utils/request'

  export default {
    data() {
      return {
        contextMenu:{
          webshell: {
            visible: false,
            top: 0,
            left: 0,
            currentId: 0,
            current:null,
            height:0
          },
          category: {
            visible: false,
            top: 0,
            left: 0,
            currentId: 0,
            current:null,
            height:0
          },
        },


        webshells: [
          // {
          // id: 1,
          // url: 'http://qq.com/1.php',
          // info: {
          //   ip_address: '203.205.254.157',
          //   address: '香港特别行政区  腾讯云 数据中心',
          // },
          // note: '备注信息',
          // create_time: '2022-01-09 15:04:13',
          // update_time: '2022-01-09 15:04:13',
          // }, 
        ],

        categories: [
          // {
          // id: 1,
          // name: "分类一"
          // }
        ],

        currentCategoryId:0,

        webshellForm:{
          visible:false,
          title:'',
          isUpdate:false,

          url:'',
          password:'',
          note:''
        },

        categoryForm:{
          visible:false,
          categoryId:0
        }

      }
    },

    methods: {
      test(){
        console.log(this.contextMenu.webshell.currentId);
        console.log(this.contextMenu.category.currentId);
      },

      categoryRightClick(row, column, event) {
        event.preventDefault();
        this.closeMenu();

        const offsetLeft = this.$el.getBoundingClientRect().left
        const left = event.clientX - offsetLeft;
        this.contextMenu.category.left = left;
        if ( (document.body.scrollHeight - event.pageY) < this.contextMenu.category.height ){
          this.contextMenu.category.top = document.body.scrollHeight - this.contextMenu.category.height-70;
        }else{
          this.contextMenu.category.top = event.pageY - 60;
        }
        this.contextMenu.category.visible = true;

        this.contextMenu.category.currentId = row.id;
        this.contextMenu.category.current = row;

        document.body.addEventListener('click', this.closeMenu);
      },

      webshellRightClick(row, column, event) {
        event.preventDefault();
        this.closeMenu();
        
        const offsetLeft = this.$el.getBoundingClientRect().left
        const left = event.clientX - offsetLeft;
        this.contextMenu.webshell.left = left;
        if ( (document.body.scrollHeight - event.pageY) < this.contextMenu.webshell.height ){
          this.contextMenu.webshell.top = document.body.scrollHeight - this.contextMenu.webshell.height-70;
        }else{
          this.contextMenu.webshell.top = event.pageY - 60;
        }
        this.contextMenu.webshell.visible = true;
        
        this.contextMenu.webshell.currentId = row.id;
        this.contextMenu.webshell.current = row;

        document.body.addEventListener('click', this.closeMenu);
      },

      closeMenu() {
        this.contextMenu.webshell.visible = false;
        this.contextMenu.category.visible = false;
        document.body.removeEventListener('click', this.closeMenu);
      },

      fetchWebshellData(categoryId){
        if(categoryId){
          request({
            url: '/webshell/getByCategoryId',
            method: 'get',
            params: { categoryId:categoryId } 
          }).then(response => {
            this.webshells = response.data;
          }).catch(error => {
            console.log(error);
          })
        }else{
          request({
            url: '/webshell/findAll',
            method: 'get',
          }).then(response => {
            this.webshells = response.data;
          }).catch(error => {
            console.log(error);
          })
        }
      },

      fetchCategoryData(){
        request({
          url: '/category/findAll',
          method: 'get',
        }).then(response => {
          this.categories = response.data;
        }).catch(error => {
          console.log(error);
        })
      },

      changeCategory(row){
        this.fetchWebshellData(row.id);
        this.currentCategoryId = row.id;
      },

      renameCategory(){

        this.$prompt('请输入分类名', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(({ value }) => {

          request({
            url: '/category/renameById',
            method: 'get',
            params: { 
              categoryId:this.contextMenu.category.currentId,
              categoryName:value
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '修改成功'
            });

            this.refreshData();

          }).catch(error => {
            console.log(error);
          })

        }).catch(() => {
          this.$message({
            type: 'info',
            message: '取消输入'
          });       
        });

      },

      refreshData(){
        this.fetchCategoryData();
        this.fetchWebshellData(this.currentCategoryId);
      },

      addCategory(){

        this.$prompt('请输入分类名', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(({ value }) => {

          request({
            url: '/category/add',
            method: 'post',
            data: { 
              name:value
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '添加成功'
            });

            this.refreshData();

          }).catch(error => {
            console.log(error);
          })

        }).catch(() => {
          this.$message({
            type: 'info',
            message: '取消输入'
          });       
        });

      },

      deleteCategory(){

        this.$confirm('此操作将永久删除该分类, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {

          request({
            url: '/category/deleteById',
            method: 'get',
            params: { 
              id:this.contextMenu.category.currentId
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '删除成功'
            });

            if(this.currentCategoryId == this.contextMenu.category.currentId){
              this.currentCategoryId = 0;
            }
            this.refreshData();

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

      openWebsite(){
        var url = this.contextMenu.webshell.current.url;
        var patt = /https?:\/\/.*?\//
        var host = url.match(patt)[0];
        window.open(host,"_blank");
      },

      addOrUpdateWebshell(){
        if(this.webshellForm.isUpdate){
          this.updateWebshell(true);
        }else{
          this.addWebshell(true);
        }
      },

      addWebshell(isSubmit){
        if(isSubmit){

          request({
            url: '/webshell/add',
            method: 'post',
            data: { 
              url:this.webshellForm.url,
              password:this.webshellForm.password,
              note:this.webshellForm.note
            } 
          }).then(response => {
            
            var webshellId = response.data.id;
            var categoryId = this.currentCategoryId;
            if(categoryId){

              request({
                url: '/category/addWebshellById',
                method: 'get',
                params: { 
                  webshellId:webshellId,
                  categoryId:categoryId
                } 
              }).then(response => {
                
                this.$message({
                  type: 'success',
                  message: '添加成功'
                });

                this.webshellForm.visible = false;
                this.refreshData();

              }).catch(error => {
                console.log(error);
              })

            }else{
              this.$message({
                type: 'success',
                message: '添加成功'
              });

              this.webshellForm.visible = false;
              this.refreshData();
            }

          }).catch(error => {
            console.log(error);
          })

        }else{
          this.webshellForm.title = "添加数据";
          this.webshellForm.isUpdate = false;
          this.webshellForm.url = "";
          this.webshellForm.password = "";
          this.webshellForm.note = "";
          this.webshellForm.visible = true;
        }
      },

      updateWebshell(isSubmit){
        if(isSubmit){

          request({
            url: '/webshell/update',
            method: 'post',
            data: { 
              id:this.contextMenu.webshell.current.id,
              url:this.webshellForm.url,
              password:this.webshellForm.password,
              note:this.webshellForm.note
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '更新成功'
            });

            this.webshellForm.visible = false;
            this.refreshData();

          }).catch(error => {
            console.log(error);
          })

        }else{
          this.webshellForm.title = "编辑数据";
          this.webshellForm.isUpdate = true;
          this.webshellForm.url = this.contextMenu.webshell.current.url;
          this.webshellForm.password = this.contextMenu.webshell.current.password;
          this.webshellForm.note = this.contextMenu.webshell.current.note;
          this.webshellForm.visible = true;
        }
      },

      deleteWebshell(){

        this.$confirm('此操作将永久删除该数据, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {

          request({
            url: '/webshell/deleteById',
            method: 'get',
            params: { 
              id:this.contextMenu.webshell.current.id
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '删除成功'
            });

            this.refreshData();

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

      addToCategory(isSubmit){
        if(isSubmit){
          var categoryId = this.categoryForm.categoryId;
          var webshellId = this.contextMenu.webshell.current.id;
          if(!categoryId){
            this.categoryForm.visible = false;
            return;
          }

          request({
            url: '/category/addWebshellById',
            method: 'get',
            params: { 
              webshellId:webshellId,
              categoryId:categoryId
            } 
          }).then(response => {
            
            this.$message({
              type: 'success',
              message: '设置成功'
            });

            this.categoryForm.visible = false;
            this.refreshData();

          }).catch(error => {
            console.log(error);
          })

        }else{
          this.categoryForm.visible = true;
        }
      },

      removeFromCategory(){
        var categoryId = this.currentCategoryId;
        var webshellId = this.contextMenu.webshell.current.id;

        if(!categoryId){
          this.$message({
            type: 'warning',
            message: '不能从"全部数据"分类中移出'
          });
          return;
        }

        request({
          url: '/category/removeWebshellById',
          method: 'get',
          params: { 
            webshellId:webshellId,
            categoryId:categoryId
          } 
        }).then(response => {
          
          this.$message({
            type: 'success',
            message: '移出成功'
          });

          this.refreshData();

        }).catch(error => {
          console.log(error);
        })

      },

      testConnection(){

        request({
          url: '/webshell/testConnection',
          method: 'post',
          data: { 
            url:this.webshellForm.url,
            password:this.webshellForm.password,
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
              message: '连接失败'
            });
          }

        }).catch(error => {
          console.log(error);
        })

      },

      openOverview() {
        this.$router.push({
          name : 'Overview',
          params : {
            id : this.contextMenu.webshell.current.id
          }
        });
      },

      openTerminal() {
        this.$router.push({
          name : 'Terminal',
          params : {
            id : this.contextMenu.webshell.current.id
          }
        });
      },

      openDatabase() {
        this.$router.push({
          name : 'Database',
          params : {
            id : this.contextMenu.webshell.current.id
          }
        });
      },

      openFilemanager(){
        this.$router.push({
          name : 'FileManager',
          params : {
            id : this.contextMenu.webshell.current.id
          }
        });
      }

    },

    created(){
      this.fetchWebshellData();
      this.fetchCategoryData();

      this.contextMenu.webshell.visible = true;
      this.contextMenu.category.visible = true;
    },

    mounted(){
      this.contextMenu.webshell.height = document.getElementById("webshellContextMenu").clientHeight;
      this.contextMenu.category.height = document.getElementById("categoryContextMenu").clientHeight;
      this.contextMenu.webshell.visible = false;
      this.contextMenu.category.visible = false;
    }

  }
</script>