/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of California
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      https://source.sakaiproject.org/svn/sakai/trunk/sakai_license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.test;

import java.net.URL;
import java.net.URLClassLoader;


public class SakaiTestClassLoader extends URLClassLoader {
	private static final TestLogger log = new TestLogger();
	
	public SakaiTestClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
//	        for (int i = 0; i < urls.length; i++) {
//	        	log.debug("added url " + urls[i]);
//	        }
	}

	public String toString() {
        StringBuffer sb = new StringBuffer("TestClassLoader\r\n");
        sb.append("  repositories:\r\n");
    	URL[] urls = super.getURLs();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                sb.append("    ");
                sb.append(urls[i]);
                sb.append("\r\n");
            }
        }
        if(super.getParent() != null) {
            sb.append("----------> Parent Classloader:\r\n");
            sb.append(super.getParent().toString());
            sb.append("\r\n");
        }
        return (sb.toString());
    }
}
