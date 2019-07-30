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
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaElementGeneratorTools;
import org.mybatis.generator.internal.util.XmlElementGeneratorTools;

/**
 * 新增or修改 存在即更新插件
 * 
 * @author fury
 * @date : 2017年11月22日 下午1:54:48
 */
public class UpsertPlugin extends PluginAdapter {

	protected CommentGenerator commentGenerator; // 注释工具

	protected List<String> warnings; // 提示

	public static final String METHOD_UPSERT = "upsert"; // 方法名
	public static final String METHOD_UPSERT_WITH_BLOBS = "upsertWithBLOBs"; // 方法名
	public static final String METHOD_UPSERT_SELECTIVE = "upsertSelective"; // 方法名

	public static final String METHOD_UPSERT_BY_EXAMPLE = "upsertByExample"; // 方法名
	public static final String METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS = "upsertByExampleWithBLOBs"; // 方法名
	public static final String METHOD_UPSERT_BY_EXAMPLE_SELECTIVE = "upsertByExampleSelective"; // 方法名

	public static final String PRO_ALLOW_MULTI_QUERIES = "allowMultiQueries"; // allowMultiQueries

	private static Boolean allowMultiQueries = Boolean.FALSE; // 是否允许多sql提交

	@Override
	public void setContext(Context context) {
		super.setContext(context);
		// 自定义
		commentGenerator = context.getCommentGenerator();
	}

	@Override
	public boolean validate(List<String> warnings) {
		// 插件是否开启了多sql提交
		String value = properties.getProperty(PRO_ALLOW_MULTI_QUERIES);
		allowMultiQueries = Boolean.valueOf(value);
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
		// 1.UPSERT
		Method mUpsert = JavaElementGeneratorTools.generateMethod(METHOD_UPSERT, JavaVisibility.DEFAULT,
				FullyQualifiedJavaType.getIntInstance(),
				new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record"));
		commentGenerator.addGeneralMethodComment(mUpsert, introspectedTable);
		// interface 增加方法
		interfaze.addMethod(mUpsert);

		// 2.upsertWithBLOBs
		// 不以有没有生成Model 的 WithBLOBs类为基准
		if (introspectedTable.hasBLOBColumns()) {
			Method mUpsertWithBLOBs = JavaElementGeneratorTools.generateMethod(METHOD_UPSERT_WITH_BLOBS,
					JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance(),
					new Parameter(JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable), "record"));
			commentGenerator.addGeneralMethodComment(mUpsertWithBLOBs, introspectedTable);
			// interface 增加方法
			interfaze.addMethod(mUpsertWithBLOBs);
		}

		// 3. upsertSelective
		Method mUpsertSelective = JavaElementGeneratorTools.generateMethod(METHOD_UPSERT_SELECTIVE,
				JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance(),
				new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record"));
		commentGenerator.addGeneralMethodComment(mUpsertSelective, introspectedTable);
		// 增加方法
		interfaze.addMethod(mUpsertSelective);

		if (allowMultiQueries) {
			// 4. upsertByExample
			Method mUpsertByExample = JavaElementGeneratorTools.generateMethod(METHOD_UPSERT_BY_EXAMPLE,
					JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance(),
					new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record",
							"@Param(\"record\")"),
					new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example",
							"@Param(\"example\")"));
			commentGenerator.addGeneralMethodComment(mUpsertByExample, introspectedTable);
			// interface 增加方法
			interfaze.addMethod(mUpsertByExample);

			// 5. upsertByExampleWithBLOBs
			// 不以有没有生成Model 的 WithBLOBs类为基准
			if (introspectedTable.hasBLOBColumns()) {
				Method mUpsertByExampleWithBLOBs = JavaElementGeneratorTools.generateMethod(
						METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS, JavaVisibility.DEFAULT,
						FullyQualifiedJavaType.getIntInstance(),
						new Parameter(JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable), "record",
								"@Param(\"record\")"),
						new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example",
								"@Param(\"example\")"));
				commentGenerator.addGeneralMethodComment(mUpsertByExampleWithBLOBs, introspectedTable);
				// interface 增加方法
				interfaze.addMethod(mUpsertByExampleWithBLOBs);
			}

			// 6. upsertByExampleSelective
			Method mUpsertByExampleSelective = JavaElementGeneratorTools.generateMethod(
					METHOD_UPSERT_BY_EXAMPLE_SELECTIVE, JavaVisibility.DEFAULT, FullyQualifiedJavaType.getIntInstance(),
					new Parameter(introspectedTable.getRules().calculateAllFieldsClass(), "record",
							"@Param(\"record\")"),
					new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example",
							"@Param(\"example\")"));
			commentGenerator.addGeneralMethodComment(mUpsertByExampleSelective, introspectedTable);
			// interface 增加方法
			interfaze.addMethod(mUpsertByExampleSelective);
		}

		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
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
		this.generateXmlElementWithoutBLOBs(document, introspectedTable);
		this.generateXmlElementWithSelective(document, introspectedTable);
		this.generateXmlElementWithBLOBs(document, introspectedTable);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	/**
	 * Selective
	 * 
	 * @param document
	 * @param introspectedTable
	 */
	private void generateXmlElementWithSelective(Document document, IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> columns = ListUtilities
				.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns());
		// 1.upsertSelective
		XmlElement eleUpsertSelective = new XmlElement("insert");
		eleUpsertSelective.addAttribute(new Attribute("id", METHOD_UPSERT_SELECTIVE));
		// 添加注释
		commentGenerator.addComment(eleUpsertSelective);

		// 参数类型
		eleUpsertSelective.addAttribute(new Attribute("parameterType",
				introspectedTable.getRules().calculateAllFieldsClass().getFullyQualifiedName()));

		// 返回新增主键
		XmlElementGeneratorTools.useGeneratedKeys(eleUpsertSelective, introspectedTable);

		// insert
		eleUpsertSelective
				.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		eleUpsertSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(columns));
		eleUpsertSelective.addElement(new TextElement("values"));
		eleUpsertSelective.addElement(XmlElementGeneratorTools.generateValuesSelective(columns));
		eleUpsertSelective.addElement(new TextElement("on duplicate key update "));
		// set 操作增加增量插件支持
		sqlSetGenerator(eleUpsertSelective, introspectedTable, false);
		// this.incrementsSelectiveSupport(eleUpsertSelective,
		// XmlElementGeneratorTools.generateSetsSelective(columns, null, false),
		// introspectedTable, false);

		document.getRootElement().addElement(eleUpsertSelective);
		if (allowMultiQueries) {
			// 2 upsertByExampleSelective
			XmlElement eleUpsertByExampleSelective = new XmlElement("insert");
			eleUpsertByExampleSelective.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE_SELECTIVE));
			// 参数类型
			eleUpsertByExampleSelective.addAttribute(new Attribute("parameterType", "map"));
			// 添加注释
			commentGenerator.addComment(eleUpsertByExampleSelective);

			// 返回新增主键
			XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExampleSelective, introspectedTable, "record.");

			// insert
			eleUpsertByExampleSelective.addElement(
					new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
			eleUpsertByExampleSelective.addElement(XmlElementGeneratorTools.generateKeysSelective(columns, "record."));

			this.generateExistsClause(introspectedTable, eleUpsertByExampleSelective, true, columns);

			// multiQueries
			eleUpsertByExampleSelective.addElement(new TextElement(";"));

			// update
			eleUpsertByExampleSelective.addElement(
					new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
			eleUpsertByExampleSelective.addElement(new TextElement("set"));
			// set 操作增加增量插件支持
			sqlSetGenerator(eleUpsertSelective, introspectedTable, true);

			// this.incrementsSelectiveSupport(eleUpsertByExampleSelective,
			// XmlElementGeneratorTools.generateSetsSelective(
			// ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columns),
			// "record."),
			// introspectedTable, true);

			// update where
			eleUpsertByExampleSelective
					.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

			document.getRootElement().addElement(eleUpsertByExampleSelective);
		}
	}

	/**
	 * 当Model有生成WithBLOBs类时的情况
	 * 
	 * @param document
	 * @param introspectedTable
	 */
	private void generateXmlElementWithBLOBs(Document document, IntrospectedTable introspectedTable) {
		if (introspectedTable.hasBLOBColumns()) {
			List<IntrospectedColumn> columns = ListUtilities
					.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns());
			// 1.upsertWithBLOBs
			XmlElement eleUpsertWithBLOBs = new XmlElement("insert");
			eleUpsertWithBLOBs.addAttribute(new Attribute("id", METHOD_UPSERT_WITH_BLOBS));
			// 添加注释
			commentGenerator.addComment(eleUpsertWithBLOBs);

			// 参数类型
			eleUpsertWithBLOBs.addAttribute(new Attribute("parameterType",
					JavaElementGeneratorTools.getModelTypeWithBLOBs(introspectedTable).getFullyQualifiedName()));

			// 返回主键
			XmlElementGeneratorTools.useGeneratedKeys(eleUpsertWithBLOBs, introspectedTable);

			// insert
			eleUpsertWithBLOBs.addElement(
					new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
			for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
				eleUpsertWithBLOBs.addElement(element);
			}
			eleUpsertWithBLOBs.addElement(new TextElement("values"));
			for (Element element : XmlElementGeneratorTools.generateValues(columns)) {
				eleUpsertWithBLOBs.addElement(element);
			}
			eleUpsertWithBLOBs.addElement(new TextElement("on duplicate key update "));
			// set 操作增加增量插件支持
			sqlSetGenerator(eleUpsertWithBLOBs, introspectedTable, false);

			document.getRootElement().addElement(eleUpsertWithBLOBs);

			if (allowMultiQueries) {

				// 2.upsertByExampleWithBLOBs
				XmlElement eleUpsertByExampleWithBLOBs = new XmlElement("insert");
				eleUpsertByExampleWithBLOBs.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE_WITH_BLOBS));
				// 参数类型
				eleUpsertByExampleWithBLOBs.addAttribute(new Attribute("parameterType", "map"));
				// 添加注释
				commentGenerator.addComment(eleUpsertByExampleWithBLOBs);
				// 返回主键
				XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExampleWithBLOBs, introspectedTable, "record.");

				// insert
				eleUpsertByExampleWithBLOBs.addElement(
						new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
				for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
					eleUpsertByExampleWithBLOBs.addElement(element);
				}
				this.generateExistsClause(introspectedTable, eleUpsertByExampleWithBLOBs, false, columns);

				// multiQueries
				eleUpsertByExampleWithBLOBs.addElement(new TextElement(";"));

				// update
				eleUpsertByExampleWithBLOBs.addElement(
						new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
				eleUpsertByExampleWithBLOBs.addElement(new TextElement("set"));
				// set 操作增加增量插件支持
				sqlSetGenerator(eleUpsertByExampleWithBLOBs, introspectedTable, true);
				// update where
				eleUpsertByExampleWithBLOBs
						.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

				document.getRootElement().addElement(eleUpsertByExampleWithBLOBs);
			}
		}
	}

	/**
	 * 当Model没有生成WithBLOBs类时的情况
	 * 
	 * @param document
	 * @param introspectedTable
	 */
	private void generateXmlElementWithoutBLOBs(Document document, IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> columns = ListUtilities
				.removeGeneratedAlwaysColumns(introspectedTable.getNonBLOBColumns());

		// 1.upsert
		XmlElement eleUpsert = new XmlElement("insert");
		eleUpsert.addAttribute(new Attribute("id", METHOD_UPSERT));
		// 添加注释
		commentGenerator.addComment(eleUpsert);

		// 参数类型
		eleUpsert.addAttribute(new Attribute("parameterType",
				JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable).getFullyQualifiedName()));

		XmlElementGeneratorTools.useGeneratedKeys(eleUpsert, introspectedTable);

		// insert
		eleUpsert.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
			eleUpsert.addElement(element);
		}
		eleUpsert.addElement(new TextElement("values"));
		for (Element element : XmlElementGeneratorTools.generateValues(columns)) {
			eleUpsert.addElement(element);
		}
		eleUpsert.addElement(new TextElement("on duplicate key update "));
		// set 操作增加增量插件支持
		// sqlSetGenerator(eleUpsert, introspectedTable, false);
		// set 操作增加增量插件支持
		sqlSetGeneratorUpsert(eleUpsert, introspectedTable, false);
		document.getRootElement().addElement(eleUpsert);

		if (allowMultiQueries) {
			// 2.upsertByExample
			XmlElement eleUpsertByExample = new XmlElement("insert");
			eleUpsertByExample.addAttribute(new Attribute("id", METHOD_UPSERT_BY_EXAMPLE));
			// 参数类型
			eleUpsertByExample.addAttribute(new Attribute("parameterType", "map"));
			// 添加注释
			commentGenerator.addComment(eleUpsertByExample);

			XmlElementGeneratorTools.useGeneratedKeys(eleUpsertByExample, introspectedTable, "record.");

			// insert
			eleUpsertByExample.addElement(
					new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
			for (Element element : XmlElementGeneratorTools.generateKeys(columns)) {
				eleUpsertByExample.addElement(element);
			}
			this.generateExistsClause(introspectedTable, eleUpsertByExample, false, columns);

			// multiQueries
			eleUpsertByExample.addElement(new TextElement(";"));

			// update
			eleUpsertByExample.addElement(
					new TextElement("update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
			eleUpsertByExample.addElement(new TextElement("set"));
			// set 操作增加增量插件支持
			sqlSetGenerator(eleUpsertByExample, introspectedTable, true);

			// update where
			eleUpsertByExample.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

			document.getRootElement().addElement(eleUpsertByExample);
		}
	}

	/**
	 * exists 语句
	 * 
	 * @param introspectedTable
	 * @param element
	 * @param selective
	 * @param columns
	 */
	private void generateExistsClause(IntrospectedTable introspectedTable, XmlElement element, boolean selective,
			List<IntrospectedColumn> columns) {
		element.addElement(new TextElement("select"));
		if (selective) {
			element.addElement(XmlElementGeneratorTools.generateValuesSelective(columns, "record.", false));
		} else {
			for (Element element1 : XmlElementGeneratorTools.generateValues(columns, "record.", false)) {
				element.addElement(element1);
			}
		}
		element.addElement(new TextElement("from dual where not exists"));
		element.addElement(new TextElement("("));
		element.addElement(new TextElement("select 1 from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

		// if example
		element.addElement(XmlElementGeneratorTools.getUpdateByExampleIncludeElement(introspectedTable));

		element.addElement(new TextElement(")"));
	}

	public static void sqlSetGenerator(XmlElement method, IntrospectedTable introspectedTable, boolean recordFalg) {

		List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();

		int index = columnList.size();
		for (IntrospectedColumn introspectedColumn : columnList) {
			// 属性类型
			String columnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();
			// 是否主键
			boolean isIdentity = introspectedColumn.isIdentity();
			
			// 实际数据库字段名 
			String databaseColumnName =	MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);

			// 属性名称
			String columnName = introspectedColumn.getJavaProperty();
			// jdbc类型
			String jdbcType = introspectedColumn.getJdbcTypeName();
			// BIGINT
			StringBuffer sb = new StringBuffer();

			XmlElement ifElement = new XmlElement("if");

			if (("java.lang.Integer".equals(columnType) || "java.math.BigDecimal".equals(columnType)) && !isIdentity) {

				if (recordFalg) {
					ifElement.addAttribute(new Attribute("test", "record." + columnName + " != null"));
					sb.append(databaseColumnName + " = (case when " + databaseColumnName + " is null then #{record." + columnName
							+ ",jdbcType=" + jdbcType + "} else " + databaseColumnName + " + #{record." + columnName
							+ ",jdbcType=" + jdbcType + "} end )");
				} else {
					ifElement.addAttribute(new Attribute("test", columnName + " != null"));
					sb.append(databaseColumnName + " = (case when " + databaseColumnName + " is null then #{" + columnName
							+ ",jdbcType=" + jdbcType + "} else " + databaseColumnName + " + #{" + columnName + ",jdbcType="
							+ jdbcType + "} end )");

				}
			} else {

				if (recordFalg) {
					ifElement.addAttribute(new Attribute("test", "record." + columnName + " != null"));
					sb.append(databaseColumnName + " =  #{record." + columnName + ",jdbcType=" + jdbcType + "}");
				} else {
					ifElement.addAttribute(new Attribute("test", columnName + " != null"));
					sb.append(databaseColumnName + " =  #{" + columnName + ",jdbcType=" + jdbcType + "}");
				}

			}

			if (index != 1) {
				sb.append(",");
			}
			ifElement.addElement(new TextElement(sb.toString()));
			method.addElement(ifElement);
			index--;
		}

	}

	public static void sqlSetGeneratorUpsert(XmlElement method, IntrospectedTable introspectedTable,
			boolean recordFalg) {

		List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
		int index = columnList.size();
		for (IntrospectedColumn introspectedColumn : columnList) {
			// 属性类型
			String columnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();
			// 是否主键
			boolean isIdentity = introspectedColumn.isIdentity();
			
			// 实际数据库字段名 
			String databaseColumnName =	MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);

			// 属性名称
			String columnName = introspectedColumn.getJavaProperty();
			// jdbc类型
			String jdbcType = introspectedColumn.getJdbcTypeName();
			// BIGINT
			StringBuffer sb = new StringBuffer();

			if (("java.lang.Integer".equals(columnType) || "java.math.BigDecimal".equals(columnType)) && !isIdentity) {

				if (recordFalg) {

					sb.append(databaseColumnName + " = (case when " + databaseColumnName + " is null then #{record." + columnName
							+ ",jdbcType=" + jdbcType + "} else " + databaseColumnName + " + #{record." + columnName
							+ ",jdbcType=" + jdbcType + "} end )");
				} else {

					sb.append(databaseColumnName + " = (case when " + databaseColumnName + " is null then #{" + columnName
							+ ",jdbcType=" + jdbcType + "} else " + databaseColumnName + " + #{" + columnName + ",jdbcType="
							+ jdbcType + "} end )");
				}
			} else {

				sb.append(databaseColumnName + " =  #{" + columnName + ",jdbcType=" + jdbcType + "}");

			}
			if (index != 1) {
				sb.append(",");
			}
			method.addElement(new TextElement(sb.toString()));
			index--;
		}

	}
}