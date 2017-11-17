package org.mybatis.generator.plugins;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

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
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.FormatTools;
import org.mybatis.generator.internal.util.JavaElementGeneratorTools;
import org.mybatis.generator.internal.util.XmlElementGeneratorTools;

/**
 * 根据条件查询返回自定义字段插件
 * 
 * @author fury
 * @date : 2017年11月17日 下午3:24:23
 */
public class SelectByExampleShowFieldPlugin extends PluginAdapter {
	protected CommentGenerator commentGenerator; // 注释工具
	protected List<String> warnings; // 提示
	// 根据主键查询自定义字段
	public static final String SELECT_BY_PRIMARYKEY_SHOWFIELD = "selectByPrimaryKeyShowField";
	// 查询自定义字段返回list
	public static final String SELECT_BY_EXAMPLE_SHOWFIELD = "selectByExampleShowField";
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
		// 定义第一个入参数 List<String> showField
		FullyQualifiedJavaType showField = FullyQualifiedJavaType.getNewListInstance();
		showField.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
		Parameter parameter1 = new Parameter(showField, "showField", "@Param(\"showField\")");

		// 1. 方法生成 selectByPrimaryKeyShowField
		Method selectByPrimaryKeyShowField = JavaElementGeneratorTools.generateMethod(SELECT_BY_PRIMARYKEY_SHOWFIELD,
				JavaVisibility.DEFAULT, JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable),
				parameter1);

		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
			selectByPrimaryKeyShowField.addParameter(new Parameter(type, "key", "@Param(\"record\")"));
		} else {
			// no primary key class - fields are in the base class
			// if more than one PK field, then we need to annotate the
			// parameters
			// for MyBatis3
			List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
			for (IntrospectedColumn introspectedColumn : introspectedColumns) {
				FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
				Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
				parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
				selectByPrimaryKeyShowField.addParameter(parameter);
			}
		}

		// 注释
		commentGenerator.addGeneralMethodComment(selectByPrimaryKeyShowField, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(selectByPrimaryKeyShowField);

		// 定义第二个参数为example
		FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		// 返回参数
		// 找出全字段对应的Model
		FullyQualifiedJavaType fullFieldModel = introspectedTable.getRules().calculateAllFieldsClass();
		FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
		listType.addTypeArgument(fullFieldModel);

		// 2. 方法生成 selectByExampleShowField
		Method selectByExampleShowField = JavaElementGeneratorTools.generateMethod(SELECT_BY_EXAMPLE_SHOWFIELD,
				JavaVisibility.DEFAULT, listType, parameter1, new Parameter(example, "example", "@Param(\"example\")"));
		// 注释
		commentGenerator.addGeneralMethodComment(selectByExampleShowField, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(selectByExampleShowField);

		// 3. 新增方法 selectOneByExampleShowField
		// 定义返回类型
		FullyQualifiedJavaType resultMap = introspectedTable.getRules().calculateAllFieldsClass();
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
		// 生成返回字段节点
		XmlElement columnsEle = new XmlElement("foreach");
		columnsEle.addAttribute(new Attribute("collection", "showField"));
		columnsEle.addAttribute(new Attribute("item", "column"));
		columnsEle.addAttribute(new Attribute("separator", ","));
		columnsEle.addElement(new TextElement("${column}"));

		// 1. selectByPrimaryKeyShowField
		XmlElement selectByPrimaryKeyShowField = new XmlElement("select");
		commentGenerator.addComment(selectByPrimaryKeyShowField);

		selectByPrimaryKeyShowField.addAttribute(new Attribute("id", SELECT_BY_PRIMARYKEY_SHOWFIELD));
		selectByPrimaryKeyShowField.addAttribute(new Attribute("resultType",
				introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));
		selectByPrimaryKeyShowField.addAttribute(new Attribute("parameterType", "map"));

		selectByPrimaryKeyShowField.addElement(new TextElement("select"));
		if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
			selectByPrimaryKeyShowField
					.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
		}
		selectByPrimaryKeyShowField.addElement(columnsEle);
		selectByPrimaryKeyShowField
				.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

		boolean and = false;
		StringBuffer sb = new StringBuffer();
		for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
			sb.setLength(0);
			if (and) {
				sb.append("  and ");
			} else {
				sb.append("where ");
				and = true;
			}

			sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
			sb.append(" = ");
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn,
					introspectedTable.getRules().generatePrimaryKeyClass() ? "record." : null));
			selectByPrimaryKeyShowField.addElement(new TextElement(sb.toString()));
		}
		selectByPrimaryKeyShowField.addElement(new TextElement("limit 1"));
		FormatTools.addElementWithBestPosition(document.getRootElement(), selectByPrimaryKeyShowField);

		// 2. selectByExampleShowField 方法
		XmlElement selectByExampleShowField = new XmlElement("select");
		commentGenerator.addComment(selectByExampleShowField);
		selectByExampleShowField.addAttribute(new Attribute("id", SELECT_BY_EXAMPLE_SHOWFIELD));
		selectByExampleShowField.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
		selectByExampleShowField.addAttribute(new Attribute("parameterType", "map"));

		selectByExampleShowField.addElement(new TextElement("select"));

		XmlElement elementDistinct = new XmlElement("if");
		elementDistinct.addAttribute(new Attribute("test", "example!=null and example.distinct"));
		elementDistinct.addElement(new TextElement("distinct"));
		selectByExampleShowField.addElement(elementDistinct);

		if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
			selectByExampleShowField
					.addElement(new TextElement("'" + introspectedTable.getSelectByExampleQueryId() + "' as QUERYID,"));
		}
		selectByExampleShowField.addElement(columnsEle);
		selectByExampleShowField
				.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

		selectByExampleShowField
				.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

		XmlElement elementShowField = new XmlElement("if");
		elementShowField.addAttribute(new Attribute("test", "example.orderByClause != null"));
		elementShowField.addElement(new TextElement("order by ${example.orderByClause}"));
		selectByExampleShowField.addElement(elementShowField);

		// 只查询一条
		FormatTools.addElementWithBestPosition(document.getRootElement(), selectByExampleShowField);

		// 3. selectOneByExampleShowField
		XmlElement selectOneByExampleShowField = new XmlElement("select");
		commentGenerator.addComment(selectOneByExampleShowField);

		selectOneByExampleShowField.addAttribute(new Attribute("id", SELECT_ONE_BY_EXAMPLE_SHOW_FIELD));
		selectOneByExampleShowField.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
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
