/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU 
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You 
 * may not use this file except in compliance with the License.  You can 
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL 
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.steeplesoft.pluginsystem.kernel;

import com.steeplesoft.pluginsystem.data.PluginFinder;
import com.sun.faces.config.AnnotationScanner;
import com.sun.faces.spi.AnnotationProvider;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.servlet.ServletContext;

/**
 *
 * @author jdlee
 */
public class PluginAnnotationScanner extends AnnotationScanner {
    private AnnotationProvider parent;
    private final HashSet<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>() {{
        add(FacesBehavior.class);
        add(FacesBehaviorRenderer.class);
        add(FacesComponent.class);
        add(FacesConverter.class);
        add(FacesValidator.class);
        add(FacesRenderer.class);
        add(ManagedBean.class);
        add(NamedEvent.class);
    }};
    
    
    public PluginAnnotationScanner(ServletContext sc, AnnotationProvider parent) {
        super(sc);
        this.parent = parent;
    }

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(Set<URI> set) {
        final Map<Class<? extends Annotation>, Set<Class<?>>> classes = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        final Map<Class<? extends Annotation>, Set<Class<?>>> pluginClasses = getClasses();
        
        classes.putAll(parent.getAnnotatedClasses(set));
        
        for (Map.Entry<Class<? extends Annotation>, Set<Class<?>>> entry : pluginClasses.entrySet()) {
            Set<Class<?>> annotatedClassSet = classes.get(entry.getKey());
            if (annotatedClassSet == null) {
                annotatedClassSet = new HashSet<Class<?>>();
                classes.put(entry.getKey(), annotatedClassSet);
            }
            
            annotatedClassSet.addAll(entry.getValue());
        }
        

        return classes;
    }

    protected void processEnumeration(Enumeration<URL> e) {
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            System.out.println("**** resource = " + url.toString());
        }
    }

    public Map<Class<? extends Annotation>, Set<Class<?>>> getClasses() {
        Map<Class<? extends Annotation>, Set<Class<?>>> classes = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
        
        for (PluginFinder pluginFinder : PluginLoader.getPluginFinders()) {
            for (Class<?> clazz : pluginFinder.getClasses()) {
                for (Class<? extends Annotation> a : annotations) {
                    if (clazz.getAnnotation(a) != null) {
                        Set<Class<?>> set = classes.get(a);
                        if (set == null) {
                            set = new HashSet<Class<?>>();
                            classes.put(a, set);
                        }
                        set.add(clazz);
                    }

                }
            }
        }
        /*
        PluginService ps = PluginUtil.getPluginService();
        
        for (String className : ps.getClassNames()) {
            try {
                Class clazz = Class.forName(className);
                for (Class<? extends Annotation> a : annotations) {
                    if (clazz.getAnnotation(a) != null) {
                        Set<Class<?>> set = classes.get(a);
                        if (set == null) {
                            set = new HashSet<Class<?>>();
                            classes.put(a, set);
                        }
                        set.add(clazz);
                    }

                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(PluginService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        * 
        */
        return Collections.unmodifiableMap(classes);
    }
}
