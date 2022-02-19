<template>
  <el-container>
    <el-main>
      <el-tabs v-model="content.currentTabName" type="card" editable @edit="handleTabsEdit">
        <el-tab-pane
          :ref="'xterm'+item.name"
          v-for="item in content.tabs"
          :key="item.name"
          :label="item.title"
          :name="item.name"
        >

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

<script>
  import request from '@/utils/request'
  import "xterm/css/xterm.css";
  import { Terminal } from "xterm";
  import { FitAddon } from "xterm-addon-fit";

  export default {
    data() {
      return {
        webshells: [
          // {
          //   id: 1,
          //   url: "http://qq.com/1.php",
          // }
        ],

        content:{
          currentTabName: null,
          tabIndex:0,

          tabs:[
            // {
            //   title: 'Tab 1',
            //   name: '1',
            // }
          ],
        },

        selectWebshellForm:{
          visible: false,
          webshellId: null
        },



      }
    },
    methods: {
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
          var id = this.selectWebshellForm.webshellId;

          var index = this.content.tabs.push({
            title: "加载中...",
            name: newTabName,
            id:id
          })-1;
          this.content.currentTabName = newTabName;

          request({
            url: '/webshell/getTerminalInfoById',
            method: 'get',
            params: { id:id } 
          }).then(response => {

            //设置tab标题
            var url = response.data.url;
            var res = url.match(/https?:\/\/(.*)?\//);
            if(res){
              this.content.tabs[index].title = res[1];
            }else{
              this.content.tabs[index].title = url;
            }

            //初始化Terminal
            const term = new Terminal({
              //fontSize: 14,
              rendererType: "canvas", //渲染类型
              rows: 50, //行数
              //cols: parseInt(_this.cols), // 不指定行数，自动回车后光标从下一行开始
              convertEol: true, //启用时，光标将设置为下一行的开头
              //scrollback: 50, //终端中的回滚量
              disableStdin: false, //是否应禁用输入。
              cursorStyle: "underline", //光标样式
              cursorBlink: true, //光标闪烁
              theme: {
                foreground: "#BFCBD9", //字体
                background: "#304156", //背景色
                cursor: "help", //设置光标
                //lineHeight: 16,
              },
            });
            const fitAddon = new FitAddon();
            term.loadAddon(fitAddon);

            this.content.tabs[index].term = term;
            this.content.tabs[index].fitAddon = fitAddon;
            this.content.tabs[index].path = response.data.path;
            this.content.tabs[index].hostname = response.data.hostname;
            this.content.tabs[index].user = response.data.user;
            this.$forceUpdate();

            this.openTerm(this.$refs["xterm"+newTabName][0].$el,this.content.tabs[index]);
            
          }).catch(error => {
            this.content.tabs[index].title = "加载失败";
            console.log(error);
          })

        }else{
          this.selectWebshellForm.visible = true;
        }
      },

      openTerm(component,tab){
        tab.term.open(component);
        tab.fitAddon.fit();
        tab.term.focus();
        var prompt = tab.user+"@"+tab.hostname+": "+tab.path;
        tab.term.writeln(prompt);
        tab.term.write("$ ");
        
        tab.command = ""; //初始化命令
        tab.termOnData = function(data){  //在tab中定义termOnData函数

          switch (data) {
            case String.fromCodePoint(13): //回车
              tab.term.write(data + "\n");

              if(tab.command.trim() == "clear" || tab.command.trim() == "cls") {
                console.log(1);
                tab.term.clear();
                tab.term.write("\u001b[H\u001b[2J");
              }
              
              request({
                url: '/webshell/executeCommandById',
                method: 'post',
                data: { 
                  id: tab.id,
                  path: tab.path,
                  command: tab.command
                } 
              }).then(response => {
                tab.term.writeln(response.data.result);
                tab.path = response.data.path;

                var prompt = tab.user+"@"+tab.hostname+": "+tab.path;
                tab.term.writeln(prompt);
                tab.term.write("$ ");
              }).catch(error => {
                console.log(error);
              })

              tab.command = "";
              break;
            case String.fromCodePoint(127): //删除
              if (tab.command.length <= 0) { //输入的command删除完后，不再对删除键做出反应
                break;
              }
              if (tab.command.charCodeAt(tab.command.length - 1) > 127) { //判断要删除的字符是否是汉字
                tab.term.write("\b\b  \b\b");
              } else {
                tab.term.write("\b \b");
              }
              tab.command = tab.command.substring(0, tab.command.length - 1);
              break;
            case String.fromCodePoint(27) + "[A": //上
              break;
            case String.fromCodePoint(27) + "[B": //下
              break;
            case String.fromCodePoint(27) + "[D": //左
              break;
            case String.fromCodePoint(27) + "[C": //右
              break;
            case String.fromCodePoint(9): //tab
              break;
            default:
              tab.command = tab.command + data;
              tab.term.write(data);
          }
        };

        tab.term.onData(tab.termOnData); //在terminal输入数据，会调用tab中的termOnData函数并传入输入的数据
      },

      resize(){
        for(var index in this.content.tabs){
          if (this.content.tabs[index].fitAddon){
            this.content.tabs[index].fitAddon.fit();
          }
        }
      }

    },

    created(){
      this.fetchWebshellData();
      this.name = this.$route.name;
      if(this.$route.params.id){
        this.selectWebshellForm.webshellId = this.$route.params.id
        this.selectWebshell(true);
      }

      window.addEventListener('resize', this.resize);
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