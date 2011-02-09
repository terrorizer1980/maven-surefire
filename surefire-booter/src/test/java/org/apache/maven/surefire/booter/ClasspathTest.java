package org.apache.maven.surefire.booter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * @author Kristian Rosenvold
 */
public class ClasspathTest
    extends TestCase
{
    private static final String DUMMY_PROPERTY_NAME = "dummyProperty";

    private static final String DUMMY_URL_1 = "foo.jar";

    private static final String DUMMY_URL_2 = "bar.jar";

    public void testShouldWriteEmptyPropertyForEmptyClasspath()
        throws Exception
    {
        Classpath classpath = new Classpath();
        classpath.writeToSystemProperty( DUMMY_PROPERTY_NAME );
        assertEquals( "", System.getProperty( DUMMY_PROPERTY_NAME ) );
    }

    public void testShouldWriteSeparatedElementsAsSystemProperty()
        throws Exception
    {
        Classpath classpath = new Classpath();
        classpath.addClassPathElementUrl( DUMMY_URL_1 );
        classpath.addClassPathElementUrl( DUMMY_URL_2 );
        classpath.writeToSystemProperty( DUMMY_PROPERTY_NAME );
        assertEquals( DUMMY_URL_1 + File.pathSeparatorChar + DUMMY_URL_2 + File.pathSeparatorChar,
                      System.getProperty( DUMMY_PROPERTY_NAME ) );
    }

    public void testShouldAddNoDuplicateElements()
    {
        Classpath classpath = new Classpath();
        classpath.addClassPathElementUrl( DUMMY_URL_1 );
        classpath.addClassPathElementUrl( DUMMY_URL_1 );
        assertClasspathConsistsOfElements( classpath, new String[] { DUMMY_URL_1 } );
    }

    public void testGetAsUrlList()
        throws Exception
    {
        final List asUrlList = createClasspathWithTwoElements().getAsUrlList();
        assertEquals( 2, asUrlList.size() );
        assertTrue( asUrlList.get( 0 ).toString().endsWith( DUMMY_URL_1 ) );
        assertTrue( asUrlList.get( 1 ).toString().endsWith( DUMMY_URL_2 ) );
    }

    public void testSetForkProperties()
        throws Exception
    {
        Properties properties = new Properties();
        createClasspathWithTwoElements().writeToForkProperties( properties, "test" );
        assertEquals( DUMMY_URL_1, properties.get( "test0" ) );
        assertEquals( DUMMY_URL_2, properties.get( "test1" ) );
    }

    public void testShouldThrowIllegalArgumentExceptionWhenNullIsAddedAsClassPathElementUrl()
        throws Exception
    {
        Classpath classpath = new Classpath();
        try
        {
            classpath.addClassPathElementUrl( null );
            fail( "IllegalArgumentException not thrown." );
        }
        catch ( IllegalArgumentException expected )
        {
        }
    }

    public void testShouldNotAddNullAsClassPathElementUrl()
        throws Exception
    {
        Classpath classpath = new Classpath();
        try
        {
            classpath.addClassPathElementUrl( null );
        }
        catch ( IllegalArgumentException ignored )
        {
        }
        assertEmptyClasspath( classpath );
    }

    public void testShouldJoinTwoNullClasspaths()
    {
        Classpath joinedClasspath = Classpath.join( null, null );
        assertEmptyClasspath( joinedClasspath );
    }

    public void testShouldHaveAllElementsAfterJoiningTwoDifferentClasspaths()
        throws Exception
    {
        Classpath firstClasspath = new Classpath();
        firstClasspath.addClassPathElementUrl( DUMMY_URL_1 );
        Classpath secondClasspath = new Classpath();
        secondClasspath.addClassPathElementUrl( DUMMY_URL_2 );
        Classpath joinedClasspath = Classpath.join( firstClasspath, secondClasspath );
        assertClasspathConsistsOfElements( joinedClasspath, new String[] { DUMMY_URL_1, DUMMY_URL_2 } );
    }

    public void testShouldNotHaveDuplicatesAfterJoiningTowClasspathsWithEqualElements()
        throws Exception
    {
        Classpath firstClasspath = new Classpath();
        firstClasspath.addClassPathElementUrl( DUMMY_URL_1 );
        Classpath secondClasspath = new Classpath();
        secondClasspath.addClassPathElementUrl( DUMMY_URL_1 );
        Classpath joinedClasspath = Classpath.join( firstClasspath, secondClasspath );
        assertClasspathConsistsOfElements( joinedClasspath, new String[] { DUMMY_URL_1 } );
    }
    
    public void testShouldReadEmptyClasspathFromForkProperties() {
        PropertiesWrapper properties = new PropertiesWrapper( new Properties() );
        Classpath classpath = Classpath.readFromForkProperties( properties, "test" );
        assertEmptyClasspath(classpath);
    }
    
    public void testShouldReadClasspathWithToElementsFromForkProperties() {
        PropertiesWrapper properties = new PropertiesWrapper( new Properties() );
        properties.setProperty( "test0", DUMMY_URL_1 );
        properties.setProperty( "test1", DUMMY_URL_2 );
        Classpath classpath = Classpath.readFromForkProperties( properties, "test" );
        assertClasspathConsistsOfElements( classpath, new String[] { DUMMY_URL_1, DUMMY_URL_2 } );
    }
    
    public void testShouldNotBeAbleToRemoveElement()
        throws Exception
    {
        Classpath classpath = createClasspathWithTwoElements();
        classpath.getClassPath().remove( 0 );
        assertEquals(2, classpath.getClassPath().size());
    }

    private void assertClasspathConsistsOfElements( Classpath classpath, String[] elements )
    {
        List classpathElements = classpath.getClassPath();
        for ( int i = 0; i < elements.length; ++i )
        {
            assertTrue( "The element '" + elements[i] + " is missing.", classpathElements.contains( elements[i] ) );
        }
        assertEquals( "Wrong number of classpath elements.", elements.length, classpathElements.size() );
    }

    private void assertEmptyClasspath( Classpath classpath )
    {
        List classpathElements = classpath.getClassPath();
        assertEquals( "Wrong number of classpath elements.", 0, classpathElements.size() );
    }

    private Classpath createClasspathWithTwoElements()
    {
        Classpath classpath = new Classpath();
        classpath.addClassPathElementUrl( DUMMY_URL_1 );
        classpath.addClassPathElementUrl( DUMMY_URL_2 );
        return classpath;
    }
}
