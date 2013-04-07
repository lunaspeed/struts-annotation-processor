struts-annotation-processor
===========================

Annotation Processor for generating Struts2 taglib

This is used for generating taglib .tld from Struts 2 annotated class.
Instead of using APT, this uses Java 6 Annotation Processor.

Sample usage of using ANT
```xml
<target name="generate-taglib">
   <javac destdir="bin"
      debug="true"
      failonerror="true"
      compiler="javac1.6"
      srcdir="StrutsTags/com/bi/struts/tag/components" includeantruntime="false" encoding="utf-8" verbose="true">
        <classpath refid="tags.classpath"/>
        <compilerarg line="-proc:only"/>
        <compilerarg line="-processor com.lunary.struts.annotations.taglib.processor.Struts2AnnotationProcessor" />
        <compilerarg line="-s dist/apt" />
        <compilerarg line="-source 6"/>
        <compilerarg value="-AtlibVersion=1.0" />
        <compilerarg value="-AjspVersion=2.0" />
        <compilerarg value="-AshortName=mb" />
        <compilerarg value="-Auri=/my-struts-tags" />
        <compilerarg value="-Adescription='My Struts Tags'" />
        <compilerarg value="-AdisplayName='My Struts Tags'" />
        <compilerarg value="-AoutTemplatesDir=${basedir}/dist/taglib-doc" />
        <compilerarg value="-AoutFile=${basedir}/bin/META-INF/my-struts-tags.tld" />
    </javac>
</target>
```