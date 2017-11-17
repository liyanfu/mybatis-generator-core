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

package org.mybatis.generator.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.Context;

/**
 * IntrospectedTable 工具类
 * 
 * @author fury
 * @date : 2017年11月17日 下午2:35:35
 */
public class IntrospectedTableTools {

	/**
	 * 设置DomainObjectName和MapperName
	 *
	 * @param introspectedTable
	 * @param context
	 * @param domainObjectName
	 */
	public static void setDomainObjectName(IntrospectedTable introspectedTable, Context context,
			String domainObjectName)
			throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		// 配置信息
		introspectedTable.getTableConfiguration().setDomainObjectName(domainObjectName);

		// FullyQualifiedTable
		Field domainObjectNameField = FullyQualifiedTable.class.getDeclaredField("domainObjectName");
		domainObjectNameField.setAccessible(true);
		domainObjectNameField.set(introspectedTable.getFullyQualifiedTable(), domainObjectName);

		// introspectedTable属性信息
		Method calculateJavaClientAttributes = IntrospectedTable.class
				.getDeclaredMethod("calculateJavaClientAttributes");
		calculateJavaClientAttributes.setAccessible(true);
		calculateJavaClientAttributes.invoke(introspectedTable);

		Method calculateModelAttributes = IntrospectedTable.class.getDeclaredMethod("calculateModelAttributes");
		calculateModelAttributes.setAccessible(true);
		calculateModelAttributes.invoke(introspectedTable);

		Method calculateXmlAttributes = IntrospectedTable.class.getDeclaredMethod("calculateXmlAttributes");
		calculateXmlAttributes.setAccessible(true);
		calculateXmlAttributes.invoke(introspectedTable);
	}

	/**
	 * 安全获取column 通过正则获取的name可能包含beginningDelimiter&&endingDelimiter
	 *
	 * @param introspectedTable
	 * @param columnName
	 * @return
	 */
	public static IntrospectedColumn safeGetColumn(IntrospectedTable introspectedTable, String columnName) {
		// columnName
		columnName = columnName.trim();
		// 过滤
		String beginningDelimiter = introspectedTable.getContext().getBeginningDelimiter();
		if (StringUtility.stringHasValue(beginningDelimiter)) {
			columnName = columnName.replaceFirst("^" + beginningDelimiter, "");
		}
		String endingDelimiter = introspectedTable.getContext().getEndingDelimiter();
		if (StringUtility.stringHasValue(endingDelimiter)) {
			columnName = columnName.replaceFirst(endingDelimiter + "$", "");
		}

		return introspectedTable.getColumn(columnName);
	}
}
