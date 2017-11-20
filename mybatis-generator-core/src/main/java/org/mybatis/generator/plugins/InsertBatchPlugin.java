package org.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaElementGeneratorTools;
import org.mybatis.generator.internal.util.XmlElementGeneratorTools;

/**
 * 新增批量插入方法插件
 * 
 * @author fury
 * @date : 2017年11月17日 下午2:31:07
 */
public class InsertBatchPlugin extends PluginAdapter {
	protected CommentGenerator commentGenerator; // 注释工具
	protected List<String> warnings; // 提示
	// 方法名 insertBatch
	public static final String INSERT_BATCH = "insertBatch";
	// 方法名 insertBatchSelective
	public static final String INSERT_BATCH_SELECTIVE = "insertBatchSelective";
	// 读取配置文件中设置的是否插入返回主键key
	public static Boolean useGeneratedKeys = Boolean.FALSE;

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		// 自定义
		commentGenerator = context.getCommentGenerator();
	}

	@Override
	public boolean validate(List<String> warnings) {
		String value = properties.getProperty("useGeneratedKeys"); //$NON-NLS-1$
		useGeneratedKeys = Boolean.valueOf(value);
		return true;
	}

	/**
	 * Java Client Methods 生成
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		// 1. insertBatch
		FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
		listType.addTypeArgument(introspectedTable.getRules().calculateAllFieldsClass());
		Method insertBatch = JavaElementGeneratorTools.generateMethod(INSERT_BATCH, JavaVisibility.DEFAULT,
				FullyQualifiedJavaType.getIntInstance(), new Parameter(listType, "list")

		);
		commentGenerator.addGeneralMethodComment(insertBatch, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(insertBatch);

		// 2. insertBatchSelective
		FullyQualifiedJavaType selectiveType = listType = FullyQualifiedJavaType.getNewListInstance();
		selectiveType.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		Method mBatchInsertSelective = JavaElementGeneratorTools.generateMethod(INSERT_BATCH_SELECTIVE,
				JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance(),
				new Parameter(selectiveType, "showField", "@Param(\"showField\")"),
				new Parameter(listType, "list", "@Param(\"list\")"));
		commentGenerator.addGeneralMethodComment(mBatchInsertSelective, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(mBatchInsertSelective);

		return true;
	}

	/**
	 * SQL Map Methods 生成
	 */
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		// 1. insertBatch
		XmlElement batchInsertEle = new XmlElement("insert");
		batchInsertEle.addAttribute(new Attribute("id", INSERT_BATCH));
		// 参数类型
		batchInsertEle.addAttribute(new Attribute("parameterType", "java.util.List"));
		// 添加注释 必须添加注释
		commentGenerator.addComment(batchInsertEle);

		// 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中
		if (useGeneratedKeys) {
			XmlElementGeneratorTools.useGeneratedKeys(batchInsertEle, introspectedTable);
		}

		batchInsertEle
				.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		for (Element element : XmlElementGeneratorTools.generateKeys(
				ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()))) {
			batchInsertEle.addElement(element);
		}

		// 添加foreach节点
		XmlElement foreachElement = new XmlElement("foreach");
		foreachElement.addAttribute(new Attribute("collection", "list"));
		foreachElement.addAttribute(new Attribute("item", "item"));
		foreachElement.addAttribute(new Attribute("separator", ","));

		for (Element element : XmlElementGeneratorTools.generateValues(
				ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item.")) {
			foreachElement.addElement(element);
		}

		// values 构建
		batchInsertEle.addElement(new TextElement("values"));
		batchInsertEle.addElement(foreachElement);
		if (context.getPlugins().sqlMapInsertElementGenerated(batchInsertEle, introspectedTable)) {
			document.getRootElement().addElement(batchInsertEle);
		}

		// 2. insertBatchSelective
		XmlElement element = new XmlElement("insert");
		element.addAttribute(new Attribute("id", INSERT_BATCH_SELECTIVE));
		// 参数类型
		element.addAttribute(new Attribute("parameterType", "java.util.List"));
		// 添加注释 必须添加注释
		commentGenerator.addComment(element);

		// 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中
		if (useGeneratedKeys) {
			XmlElementGeneratorTools.useGeneratedKeys(element, introspectedTable);
		}

		element.addElement(
				new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));

		XmlElement foreachInsertColumns = new XmlElement("foreach");
		foreachInsertColumns.addAttribute(new Attribute("collection", "showField"));
		foreachInsertColumns.addAttribute(new Attribute("item", "one"));
		foreachInsertColumns.addAttribute(new Attribute("separator", ","));
		foreachInsertColumns.addElement(new TextElement("${one}"));

		element.addElement(foreachInsertColumns);

		element.addElement(new TextElement(")"));

		// values
		element.addElement(new TextElement("values"));

		// foreach values
		XmlElement foreachValues = new XmlElement("foreach");
		foreachValues.addAttribute(new Attribute("collection", "list"));
		foreachValues.addAttribute(new Attribute("item", "item"));
		foreachValues.addAttribute(new Attribute("separator", ","));

		foreachValues.addElement(new TextElement("("));

		// foreach 所有插入的列，比较是否存在
		XmlElement foreachInsertColumnsCheck = new XmlElement("foreach");
		foreachInsertColumnsCheck.addAttribute(new Attribute("collection", "showField"));
		foreachInsertColumnsCheck.addAttribute(new Attribute("item", "column"));
		foreachInsertColumnsCheck.addAttribute(new Attribute("separator", ","));

		// 所有表字段
		List<IntrospectedColumn> columns = ListUtilities
				.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
		List<IntrospectedColumn> columns1 = ListUtilities
				.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
		for (int i = 0; i < columns1.size(); i++) {
			IntrospectedColumn introspectedColumn = columns.get(i);
			XmlElement check = new XmlElement("if");
			check.addAttribute(new Attribute("test", "'" + introspectedColumn.getActualColumnName() + "' == column"));
			check.addElement(
					new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "item.")));

			foreachInsertColumnsCheck.addElement(check);
		}
		foreachValues.addElement(foreachInsertColumnsCheck);

		foreachValues.addElement(new TextElement(")"));

		element.addElement(foreachValues);

		if (context.getPlugins().sqlMapInsertElementGenerated(element, introspectedTable)) {
			document.getRootElement().addElement(element);
		}

		return true;
	}

}