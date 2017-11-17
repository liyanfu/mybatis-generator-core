
import org.mybatis.generator.api.ShellRunner;

/**
 * @author fury
 * @date : 2017年11月17日 下午2:37:33
 */
public class Startup {

	public static void main(String[] args) throws Exception {

		// 调试初始化参数
		Startup startup = new Startup();
		// 取得根目录路径
		String rootPath = startup.getClass().getResource("/").getFile().toString();
		// 获取配置文件
		String[] arg = new String[] { "-configfile", rootPath + "conf/generatorConfig.xml", "-overwrite" };
		ShellRunner.main(arg);

	}
}
