/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.FormatTools;
import org.mybatis.generator.internal.util.JavaElementGeneratorTools;
import org.mybatis.generator.internal.util.XmlElementGeneratorTools;

/**
 * 增量插件,需要进行累计加,减计算用的插件
 * 
 * @author fury
 * @date : 2017年11月19日 下午6:19:21
 */
public class UpdateIncrementsPlugin extends PluginAdapter {
	protected CommentGenerator commentGenerator; // 注释工具

	protected List<String> warnings; // 提示

	// 根据设置条件累计数据
	public static final String UPDATE_BY_EXAMPLE_SELECTIVE_SYNC = "updateByExampleSelectiveSync";
	// 根据主键累计数据
	public static final String UPDATE_BY_PRIMARYKEY_SELECTIVE_SYNC = "updateByPrimaryKeySelectiveSync";

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
	 * Java Client Methods
	 * 
	 * @param interfaze
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {

		// 1. 方法生成 updateByExampleSelectiveSync
		Method updateByExampleSelectiveSync = JavaElementGeneratorTools.generateMethod(UPDATE_BY_EXAMPLE_SELECTIVE_SYNC,
				JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance());

		// 定义第一个入参数 record
		FullyQualifiedJavaType record = introspectedTable.getRules().calculateAllFieldsClass();
		Parameter parameter1 = new Parameter(record, "record", "@Param(\"record\")");
		updateByExampleSelectiveSync.addParameter(parameter1);

		// 定义第二个参数为example
		FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		updateByExampleSelectiveSync.addParameter(new Parameter(example, "example", "@Param(\"example\")"));

		// 注释
		commentGenerator.addGeneralMethodComment(updateByExampleSelectiveSync, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(updateByExampleSelectiveSync);

		// 2. 方法生成 updateByPrimaryKeySelectiveSync
		Method updateByPrimaryKeySelectiveSync = JavaElementGeneratorTools.generateMethod(
				UPDATE_BY_PRIMARYKEY_SELECTIVE_SYNC, JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance(),
				new Parameter(record, "record"));
		// 注释
		commentGenerator.addGeneralMethodComment(updateByPrimaryKeySelectiveSync, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(updateByPrimaryKeySelectiveSync);

		return true;
	}

	/**
	 * SQL Map Methods
	 */
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		// 1.updateByExampleSelectiveSync
		XmlElement updateByExampleSelectiveSync = new XmlElement("update");
		updateByExampleSelectiveSync.addAttribute(new Attribute("id", UPDATE_BY_EXAMPLE_SELECTIVE_SYNC));
		updateByExampleSelectiveSync.addAttribute(new Attribute("parameterType", "map"));
		// 添加注释 必须添加注释
		commentGenerator.addComment(updateByExampleSelectiveSync);
		sqlSetGenerator(updateByExampleSelectiveSync, introspectedTable, true);
		// where条件
		updateByExampleSelectiveSync
				.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));
		// 最佳位置生成sql
		FormatTools.addElementWithBestPosition(document.getRootElement(), updateByExampleSelectiveSync);

		// 2.生成 updateByPrimaryKeySelectiveSync

		XmlElement updateByPrimaryKeySelectiveSync = new XmlElement("update");
		updateByPrimaryKeySelectiveSync.addAttribute(new Attribute("id", UPDATE_BY_PRIMARYKEY_SELECTIVE_SYNC));
		updateByPrimaryKeySelectiveSync.addAttribute(new Attribute("parameterType", "map"));
		// 添加注释 必须添加注释
		commentGenerator.addComment(updateByPrimaryKeySelectiveSync);
		sqlSetGenerator(updateByPrimaryKeySelectiveSync, introspectedTable, false);
		// where条件 key
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
			updateByPrimaryKeySelectiveSync.addElement(new TextElement(sb.toString()));
		}
		// 最佳位置生成sql
		FormatTools.addElementWithBestPosition(document.getRootElement(), updateByPrimaryKeySelectiveSync);
		return true;
	}

	/**
	 * 生成set条件语句
	 * 
	 * @param method
	 *            当前方法
	 * @param introspectedTable
	 *            所有元素信息
	 * @param recordFalg
	 *            根据传参条件 是否生成带有实体属性
	 */
	public static void sqlSetGenerator(XmlElement method, IntrospectedTable introspectedTable, boolean recordFalg) {
		method.addElement(new TextElement("update  " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

		method.addElement(new TextElement("<set>"));

		List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : columnList) {
			// 属性类型
			String columnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();
			// 是否主键
			boolean isIdentity = introspectedColumn.isIdentity();
			if (("java.lang.Integer".equals(columnType) || "java.math.BigDecimal".equals(columnType)) && !isIdentity) {
				
				// 实际数据库字段名 
				String databaseColumnName =	MyBatis3FormattingUtilities
                .getEscapedColumnName(introspectedColumn);
				
				// 指定生成实体类时是否使用实际的列名作为实体类的属性名  true 则是数据库字段名，否则是骆驼命名规则 false  #{userMoney,jdbcType=DECIMAL}
				//属性名称  jdbc类型
				//String columnNameAndJdbcType = MyBatis3FormattingUtilities
	            //        .getParameterClause(introspectedColumn);
				// 属性名称
				String columnName = introspectedColumn.getJavaProperty();
				// jdbc类型
				String jdbcType = introspectedColumn.getJdbcTypeName();
				// BIGINT
				StringBuffer sb = new StringBuffer();
				XmlElement ifElement = new XmlElement("if");

				if (recordFalg) {
					ifElement.addAttribute(new Attribute("test", "record." + columnName + " != null"));
					sb.append(" " + databaseColumnName + " = (case when " + databaseColumnName + " is null then #{record." + columnName
							+ ",jdbcType=" + jdbcType + "} else " + databaseColumnName + " + #{record." + columnName
							+ ",jdbcType=" + jdbcType + "} end ),");
					ifElement.addElement(new TextElement(sb.toString()));
				} else {
					ifElement.addAttribute(new Attribute("test", columnName + " != null"));
					sb.append(" " + databaseColumnName + " = (case when " + databaseColumnName + " is null then #{" + columnName
							+ ",jdbcType=" + jdbcType + "} else " + databaseColumnName + " + #{" + columnName + ",jdbcType="
							+ jdbcType + "} end ),");
					ifElement.addElement(new TextElement(sb.toString()));
				}

				method.addElement(ifElement);
			}

		}
		// set结束标签
		method.addElement(new TextElement("</set>"));
	}

}
