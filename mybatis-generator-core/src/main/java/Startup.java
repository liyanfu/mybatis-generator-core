


import org.mybatis.generator.api.ShellRunner;

/**
 * @author fury
 *      
 */
public class Startup {


    public static void main(String[] args) throws Exception {

        // 调试初始化参数
        Startup startup = new Startup();
        //取得根目录路径
        String rootPath = startup.getClass().getResource("/").getFile().toString();
        //当前目录路径
     // String rootPath=startup.getClass().getResource(".").getPath();
     // String rootPath=startup.getClass().getResource("").getFile().toString();
        //当前目录的上级目录路径
    		  // String rootPath=startup.getClass().getResource("../").getFile().toString();
        String[] arg = new String[]{"-configfile", rootPath + "conf/generatorConfig.xml", "-overwrite"};
        //String[] arg = new String[]{"-configfile", rootPath + "test/generatorConfigForMySql.xml", "-overwrite"};

        ShellRunner.main(arg);

    }
}
