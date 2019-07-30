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
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;

/**
 * model 生成对应属性的静态属性插件
 * 
 * @author fury
 * @date : 2017年11月20日 下午1:22:57
 */
public class AddFieldPlugin extends PluginAdapter {

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
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : columnList) {
			Field columnField = this.generateColumnField(topLevelClass, introspectedColumn);
			commentGenerator.addFieldComment(columnField, introspectedTable, introspectedColumn);
			topLevelClass.addField(columnField);
		}
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 生成Column静态常量属性
	 *
	 * @param topLevelClass
	 * @param introspectedColumn
	 * @return
	 */
	public Field generateColumnField(TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
		// 属性名称
		String columnName = introspectedColumn.getJavaProperty();
		// 实际数据库字段名 
		String databaseColumnName =	MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);

		FullyQualifiedJavaType columnType = FullyQualifiedJavaType.getStringInstance();
		// 静态常量属性名称
		Field columnField = new Field("FD_" + columnName.toUpperCase(), columnType);
		columnField.setVisibility(JavaVisibility.PUBLIC);
		columnField.setFinal(true);
		columnField.setStatic(true);
		columnField.setType(columnType); // $NON-NLS-1$
		columnField.setInitializationString("\"" + databaseColumnName + "\""); // $NON-NLS-1$
		return columnField;
	}
}
