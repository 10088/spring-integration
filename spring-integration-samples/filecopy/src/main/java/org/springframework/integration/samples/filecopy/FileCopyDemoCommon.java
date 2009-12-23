/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.samples.filecopy;

import java.io.File;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;

/**
 * Displays the names of the input and output directories.
 * 
 * @author Marius Bogoevici
 * @author Mark Fisher
 */
public class FileCopyDemoCommon {

	public static void displayDirectories(ApplicationContext context) {
		Object source = context.getBeansOfType(FileReadingMessageSource.class).values().iterator().next();
		Object handler = context.getBeansOfType(FileWritingMessageHandler.class).values().iterator().next();
		File inDir = (File) new DirectFieldAccessor(source).getPropertyValue("inputDirectory");
		File outDir = (File) new DirectFieldAccessor(handler).getPropertyValue("destinationDirectory");
		System.out.println("Input directory is: " + inDir.getAbsolutePath());
		System.out.println("Output directory is: " + outDir.getAbsolutePath());
		System.out.println("===================================================");
	}

}
