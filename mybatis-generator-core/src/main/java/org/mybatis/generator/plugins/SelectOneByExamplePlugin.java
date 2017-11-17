package org.mybatis.generator.plugins;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
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
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.FormatTools;
import org.mybatis.generator.internal.util.JavaElementGeneratorTools;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.XmlElementGeneratorTools;

/**
 * 查询一条数据的方法插件
 * 
 * @author fury
 * @date : 2017年11月17日 下午3:24:23
 */
public class SelectOneByExamplePlugin extends PluginAdapter {
	protected CommentGenerator commentGenerator; // 注释工具
	protected List<String> warnings; // 提示
	// 查询一条数据方法
	public static final String SELECT_ONE_BY_EXAMPLE = "selectOneByExample";
	// 查询一条数据大文本方法
	public static final String SELECT_ONE_BY_EXAMPLE_WITH_BLOBS = "selectOneByExampleWithBLOBs";
	// 根据需求查询需要返回的列
	public static final String SELECT_ONE_BY_EXAMPLE_SHOW_FIELD = "selectOneByExampleShowField";

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		// 用户自定义
		commentGenerator = context.getCommentGenerator();
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * Java Client Methods 生成 具体执行顺序
	 * 
	 * @param interfaze
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		// 1. 方法生成 selectOneByExample
		Method selectOneByExample = JavaElementGeneratorTools.generateMethod(SELECT_ONE_BY_EXAMPLE,
				JavaVisibility.DEFAULT, JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable),
				new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example"));
		// 注释
		commentGenerator.addGeneralMethodComment(selectOneByExample, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(selectOneByExample);

		// 2. 方法生成 selectOneByExampleWithBLOBs
		// WithBLOBs类为基准
		if (introspectedTable.hasBLOBColumns()) {
			// 方法生成 selectOneByExample
			Method selectOneByExampleWithBLOBs = JavaElementGeneratorTools.generateMethod(
					SELECT_ONE_BY_EXAMPLE_WITH_BLOBS, JavaVisibility.DEFAULT,
					JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable),
					new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example"));
			commentGenerator.addGeneralMethodComment(selectOneByExampleWithBLOBs, introspectedTable);

			// interface 增加方法
			interfaze.addMethod(selectOneByExampleWithBLOBs);
		}

		// 3. 新增方法 selectOneByExampleShowField
		// 定义返回类型
		FullyQualifiedJavaType resultMap = introspectedTable.getRules().calculateAllFieldsClass();
		// 定义第一个参数为List<String>
		FullyQualifiedJavaType showField = FullyQualifiedJavaType.getNewListInstance();
		showField.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		// 定义第二个参数为example
		FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		Method selectOneByExampleShowField = JavaElementGeneratorTools.generateMethod(SELECT_ONE_BY_EXAMPLE_SHOW_FIELD,
				JavaVisibility.DEFAULT, resultMap, new Parameter(showField, "showField", "@Param(\"showField\")"),
				new Parameter(example, "example", "@Param(\"example\")"));

		commentGenerator.addGeneralMethodComment(selectOneByExampleShowField, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(selectOneByExampleShowField);
		return true;
	}

	/**
	 * SQL Map Methods
	 * 
	 * @param document
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		// 1. selectOneByExample
		// 生成查询语句
		XmlElement selectOneElement = new XmlElement("select");
		// 添加注释 必须添加注释
		commentGenerator.addComment(selectOneElement);
		// 添加ID
		selectOneElement.addAttribute(new Attribute("id", SELECT_ONE_BY_EXAMPLE));
		// 添加返回类型
		selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
		// 添加参数类型
		selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
		// 添加查询节点
		selectOneElement.addElement(new TextElement("select"));

		StringBuilder sb = new StringBuilder();
		if (StringUtility.stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
			sb.append('\'');
			sb.append(introspectedTable.getSelectByExampleQueryId());
			sb.append("' as QUERYID,");
			selectOneElement.addElement(new TextElement(sb.toString()));
		}
		selectOneElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));

		sb.setLength(0);
		sb.append("from ");
		sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
		selectOneElement.addElement(new TextElement(sb.toString()));
		selectOneElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

		XmlElement ifElement = new XmlElement("if");
		ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-2$
		ifElement.addElement(new TextElement("order by ${orderByClause}"));// $NON-NLS-2$
		selectOneElement.addElement(ifElement);

		// 只查询一条
		selectOneElement.addElement(new TextElement("limit 1"));
		FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneElement);

		// 2. selectOneByExampleWithBLOBs
		// 以 WithBLOBs类为基准
		if (introspectedTable.hasBLOBColumns()) {
			// 生成查询语句
			XmlElement selectOneWithBLOBsElement = new XmlElement("select");
			// 添加注释 必须添加注释
			commentGenerator.addComment(selectOneWithBLOBsElement);

			// 添加ID
			selectOneWithBLOBsElement.addAttribute(new Attribute("id", SELECT_ONE_BY_EXAMPLE_WITH_BLOBS));
			// 添加返回类型
			selectOneWithBLOBsElement
					.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
			// 添加参数类型
			selectOneWithBLOBsElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
			// 添加查询SQL
			selectOneWithBLOBsElement.addElement(new TextElement("select"));

			sb.setLength(0);
			if (StringUtility.stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
				sb.append('\'');
				sb.append(introspectedTable.getSelectByExampleQueryId());
				sb.append("' as QUERYID,");
				selectOneWithBLOBsElement.addElement(new TextElement(sb.toString()));
			}

			selectOneWithBLOBsElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
			selectOneWithBLOBsElement.addElement(new TextElement(","));
			selectOneWithBLOBsElement.addElement(XmlElementGeneratorTools.getBlobColumnListElement(introspectedTable));

			sb.setLength(0);
			sb.append("from ");
			sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
			selectOneWithBLOBsElement.addElement(new TextElement(sb.toString()));
			selectOneWithBLOBsElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

			XmlElement ifElement1 = new XmlElement("if");
			ifElement1.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-2$
			ifElement1.addElement(new TextElement("order by ${orderByClause}"));
			selectOneWithBLOBsElement.addElement(ifElement1);

			// 只查询一条
			selectOneWithBLOBsElement.addElement(new TextElement("limit 1"));
			FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneWithBLOBsElement);

		}

		// 3. selectOneByExampleShowField
		// 生成返回字段节点
		XmlElement columnsEle = new XmlElement("foreach");
		columnsEle.addAttribute(new Attribute("collection", "showField"));
		columnsEle.addAttribute(new Attribute("item", "column"));
		columnsEle.addAttribute(new Attribute("separator", ","));
		columnsEle.addElement(new TextElement("${column.value}"));

		// 1. selectByExampleSelective 方法
		XmlElement selectOneByExampleShowField = new XmlElement("select");
		commentGenerator.addComment(selectOneByExampleShowField);

		selectOneByExampleShowField.addAttribute(new Attribute("id", SELECT_ONE_BY_EXAMPLE_SHOW_FIELD));
		selectOneByExampleShowField.addAttribute(new Attribute("resultType",
				introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));
		selectOneByExampleShowField.addAttribute(new Attribute("parameterType", "map"));

		selectOneByExampleShowField.addElement(new TextElement("select"));
		if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
			selectOneByExampleShowField
					.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
		}
		selectOneByExampleShowField.addElement(columnsEle);
		selectOneByExampleShowField
				.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

		selectOneByExampleShowField
				.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

		XmlElement ifElementShowField = new XmlElement("if");
		ifElementShowField.addAttribute(new Attribute("test", "example.orderByClause != null"));
		ifElementShowField.addElement(new TextElement("order by ${example.orderByClause}"));
		selectOneByExampleShowField.addElement(ifElementShowField);

		// 只查询一条
		selectOneByExampleShowField.addElement(new TextElement("limit 1"));
		FormatTools.addElementWithBestPosition(document.getRootElement(), selectOneByExampleShowField);

		return true;
	}
}
