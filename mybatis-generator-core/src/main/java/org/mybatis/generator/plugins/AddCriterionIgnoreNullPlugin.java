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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaElementGeneratorTools;

/**
 * 查询条件参数忽略Null
 * 
 * @author fury
 * @date : 2017年11月20日 下午2:13:45
 */
public class AddCriterionIgnoreNullPlugin extends PluginAdapter {
	protected CommentGenerator commentGenerator; // 注释工具

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

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
		List<IntrospectedColumn> columnsList = introspectedTable.getAllColumns();
		for (InnerClass innerClass : innerClasses) {
			if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
				// addCriterionIgnoreNull方法
				addCriterionIgnoreNull(topLevelClass, innerClass, introspectedTable);

				List<Method> methods = innerClass.getMethods();
				List<Method> newMethods = new ArrayList<Method>();
				for (Method method : methods) {
					if (method.getName().startsWith("and") && !method.getName().endsWith("IsNull")
							&& !method.getName().endsWith("IsNotNull") && !method.getName().endsWith("Between")) {
						// 所有属性生成的方法都生成IgnoreNull方法
						newMethods.add(addColumnIgnoreNull(topLevelClass, innerClass, method));

					}
				}

				for (Method newMethod : newMethods) {
					innerClass.addMethod(newMethod);
				}

			}

			// 所有属性值转大写比较
			if ("Criteria".equals(innerClass.getType().getShortName())) {
				// addCriterionIgnoreNull方法
				for (IntrospectedColumn columns : columnsList) {
					andColumnLikeInsensitive(topLevelClass, innerClass, columns);
				}

			}
		}

		// setOrderByClauseIgnoreNull方法
		setOrderByClauseIgnoreNull(topLevelClass, introspectedTable);

		return true;
	}

	/**
	 * 所有属性生成的方法都生成IgnoreNull方法
	 * 
	 * @param topLevelClass
	 * @param innerClass
	 * @param columns
	 */
	private Method addColumnIgnoreNull(TopLevelClass topLevelClass, InnerClass innerClass, Method method) {

		// 添加 andColumnLikeInsensitive
		Method addColumnIgnoreNull = JavaElementGeneratorTools.generateMethod(method.getName() + "IgnoreNull",
				JavaVisibility.PUBLIC, FullyQualifiedJavaType.getCriteriaInstance(),
				new Parameter(method.getParameters().get(0).getType(), "value"));

		String addCriterionIgnoreNull = method.getBodyLines().get(0);
		String sb = "addCriterionIgnoreNull" + addCriterionIgnoreNull.substring(addCriterionIgnoreNull.indexOf("("),
				addCriterionIgnoreNull.length());

		// 生成方法实现体
		addColumnIgnoreNull = JavaElementGeneratorTools.generateMethodBody(addColumnIgnoreNull,
				sb.replace("values", "value"), "return (Criteria) this;");
		// 添加方法
		return addColumnIgnoreNull;

	}

	/**
	 * 添加like方法
	 * 
	 * @param topLevelClass
	 * @param innerClass
	 * @param columns
	 */
	private void andColumnLikeInsensitive(TopLevelClass topLevelClass, InnerClass innerClass,
			IntrospectedColumn columns) {
		// bean属性名称
		String columnName = columns.getJavaProperty();
		// 属性名首字母转大写
		StringBuffer sb = new StringBuffer("");
		Matcher m = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(columnName);
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase() + m.group(2));
		}
		// 添加 andColumnLikeInsensitive
		Method andColumnLikeInsensitive = JavaElementGeneratorTools.generateMethod(
				"and" + sb.toString() + "LikeInsensitive", JavaVisibility.PUBLIC,
				FullyQualifiedJavaType.getCriteriaInstance(),
				new Parameter(FullyQualifiedJavaType.getStringInstance(), "value"));

		// 生成方法实现体
		andColumnLikeInsensitive = JavaElementGeneratorTools.generateMethodBody(andColumnLikeInsensitive,
				"addCriterion(\"upper(" + columnName + ") like\", value.toUpperCase(), \"" + columnName + "\");", //
				"return this;" //
		);
		// 添加方法
		innerClass.addMethod(andColumnLikeInsensitive);

	}

	/**
	 * 添加工厂方法
	 * 
	 * @param topLevelClass
	 * @param innerClass
	 * @param introspectedTable
	 */
	private void addFactoryMethodToCriteria(TopLevelClass topLevelClass, InnerClass innerClass,
			IntrospectedTable introspectedTable) {
		// example field
		Field exampleField = JavaElementGeneratorTools.generateField("example", JavaVisibility.PRIVATE,
				topLevelClass.getType(), null);
		commentGenerator.addFieldComment(exampleField, introspectedTable);
		innerClass.addField(exampleField);

		// overwrite constructor
		List<Method> methods = innerClass.getMethods();
		for (Method method : methods) {
			if (method.isConstructor()) {
				method.addParameter(new Parameter(topLevelClass.getType(), "example"));
				method.addBodyLine("this.example = example;");
				commentGenerator.addGeneralMethodComment(method, introspectedTable);
			}
		}

		// 添加example工厂方法
		Method exampleMethod = JavaElementGeneratorTools.generateMethod("example", JavaVisibility.PUBLIC,
				topLevelClass.getType());
		commentGenerator.addGeneralMethodComment(exampleMethod, introspectedTable);
		exampleMethod = JavaElementGeneratorTools.generateMethodBody(exampleMethod, "return this.example;");
		innerClass.addMethod(exampleMethod);
	}

	/**
	 * 增强Criteria的链式调用，添加andIf(boolean addIf, CriteriaAdd
	 * add)方法，实现链式调用中按条件增加查询语句
	 * 
	 * @param topLevelClass
	 * @param innerClass
	 * @param introspectedTable
	 */
	// private void addAndIfMethodToCriteria(TopLevelClass topLevelClass,
	// InnerClass innerClass,
	// IntrospectedTable introspectedTable) {
	// // 添加接口CriteriaAdd
	// InnerInterface criteriaAddInterface = new InnerInterface("ICriteriaAdd");
	// criteriaAddInterface.setVisibility(JavaVisibility.PUBLIC);
	//
	// // ICriteriaAdd增加接口add
	// Method addMethod = JavaElementGeneratorTools.generateMethod("add",
	// JavaVisibility.DEFAULT, innerClass.getType(),
	// new Parameter(innerClass.getType(), "add"));
	// commentGenerator.addGeneralMethodComment(addMethod, introspectedTable);
	// criteriaAddInterface.addMethod(addMethod);
	//
	// InnerClass innerClassWrapper = new
	// InnerInterfaceWrapperToInnerClass(criteriaAddInterface);
	// // 添加注释
	// commentGenerator.addClassComment(innerClassWrapper, introspectedTable);
	// innerClass.addInnerClass(innerClassWrapper);
	//
	// // 添加andIf方法
	// Method andIfMethod = JavaElementGeneratorTools.generateMethod("andIf",
	// JavaVisibility.PUBLIC,
	// innerClass.getType(), new
	// Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ifAdd"),
	// new Parameter(criteriaAddInterface.getType(), "add"));
	// commentGenerator.addGeneralMethodComment(andIfMethod, introspectedTable);
	// andIfMethod = JavaElementGeneratorTools.generateMethodBody(andIfMethod,
	// "if (ifAdd) {", "add.add(this);", "}",
	// "return this;");
	// innerClass.addMethod(andIfMethod);
	//
	// }

	/**
	 * 
	 * 在外层类中新增方法 setOrderByClauseIgnoreNull
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 */
	private void setOrderByClauseIgnoreNull(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// 添加 setOrderByClauseIgnoreNull
		Method setOrderByClauseIgnoreNull = JavaElementGeneratorTools.generateMethod("setOrderByClauseIgnoreNull",
				JavaVisibility.PUBLIC, null,
				new Parameter(FullyQualifiedJavaType.getStringInstance(), "orderByClause"));
		// 注释
		commentGenerator.addGeneralMethodComment(setOrderByClauseIgnoreNull, introspectedTable);

		// 生成方法实现体
		setOrderByClauseIgnoreNull = JavaElementGeneratorTools.generateMethodBody(setOrderByClauseIgnoreNull,
				"String tmp = orderByClause.toLowerCase().replace(\"null\", \"\").replace(\"asc\", \"\").replace(\"desc\", \"\").trim();", //
				"if (orderByClause != null && tmp.length() > 0) {", //
				"this.orderByClause = orderByClause;}"//
		);
		// 添加方法
		topLevelClass.addMethod(setOrderByClauseIgnoreNull);

	}

	/**
	 * 
	 * 在内部类中新增方法 addCriterionIgnoreNull
	 * 
	 * @param topLevelClass
	 * @param introspectedTable
	 */
	private void addCriterionIgnoreNull(TopLevelClass topLevelClass, InnerClass innerClass,
			IntrospectedTable introspectedTable) {
		// 添加 addCriterionIgnoreNull
		Method addCriterionIgnoreNull = JavaElementGeneratorTools.generateMethod("addCriterionIgnoreNull",
				JavaVisibility.PROTECTED, null, new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"),
				new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"),
				new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));

		// 生成方法实现体
		addCriterionIgnoreNull = JavaElementGeneratorTools.generateMethodBody(addCriterionIgnoreNull,
				"if (value != null) {", //
				"if (value instanceof String) {", //
				"if (condition.toLowerCase().endsWith(\"like\")) {", //
				"if (((String) value).replaceAll(\"%\", \"\").length() > 0) {", //
				"criteria.add(new Criterion(condition, value));", //
				"}", //
				"} else if (((String) value).length() > 0) {", //
				"criteria.add(new Criterion(condition, value));", //
				"}", //
				"} else if (value instanceof List<?>) {", //
				"if (((List<?>) value).size() > 0) {", //
				"criteria.add(new Criterion(condition, value));", //
				"}", //
				"} else {", //
				"criteria.add(new Criterion(condition, value));", //
				"}", //
				"}"//
		);
		// 添加方法
		innerClass.addMethod(addCriterionIgnoreNull);

	}

}
