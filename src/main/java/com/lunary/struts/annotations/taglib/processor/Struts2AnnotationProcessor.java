
package com.lunary.struts.annotations.taglib.processor;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.apache.struts2.views.annotations.StrutsTag;
import org.apache.struts2.views.annotations.StrutsTagAttribute;
import org.apache.struts2.views.annotations.StrutsTagSkipInheritance;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

@SupportedAnnotationTypes({Struts2AnnotationProcessor.TAG, Struts2AnnotationProcessor.TAG_ATTRIBUTE, Struts2AnnotationProcessor.TAG_SKIP_HIERARCHY})
@SupportedOptions({"outFile", "tlibVersion", "jspVersion", "shortName", "uri", "description", "displayName", "outTemplatesDir"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Struts2AnnotationProcessor extends AbstractProcessor {

    public static final String TAG = "org.apache.struts2.views.annotations.StrutsTag";
    public static final String TAG_ATTRIBUTE = "org.apache.struts2.views.annotations.StrutsTagAttribute";
    public static final String TAG_SKIP_HIERARCHY = "org.apache.struts2.views.annotations.StrutsTagSkipInheritance";
    private Map<String, String> options;
    private final Map<String, Tag> tags = new TreeMap<String, Tag>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        options = processingEnv.getOptions();
        checkOptions();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element elem : roundEnv.getElementsAnnotatedWith(StrutsTag.class)) {

            String typeName = elem.toString();
            StrutsTag stag = elem.getAnnotation(StrutsTag.class);
            Tag tag = new Tag();
            tag.setDescription(stag.description());
            tag.setName(stag.name());
            tag.setTldBodyContent(stag.tldBodyContent());
            tag.setTldTagClass(stag.tldTagClass());
            tag.setDeclaredType(typeName);
            tag.setAllowDynamicAttributes(stag.allowDynamicAttributes());
            // add to map
            tags.put(typeName, tag);
        }

        for (Element elem : roundEnv.getElementsAnnotatedWith(StrutsTagSkipInheritance.class)) {

            if (elem.getKind() == ElementKind.METHOD) {
                // StrutsTagSkipInheritance stag =
                // elem.getAnnotation(StrutsTagSkipInheritance.class);
                String typeName = elem.getEnclosingElement().toString();
                Tag tag = tags.get(typeName);
                if (tag != null) {
                    // if it is on an abstract class, there is not tag for it at
                    // this point
                    String methodName = elem.getSimpleName().toString();
                    String name = String.valueOf(Character.toLowerCase(methodName.charAt(3))) + methodName.substring(4);

                    tags.get(typeName).addSkipAttribute(name);
                }
            }
        }

        for (Element elem : roundEnv.getElementsAnnotatedWith(StrutsTagAttribute.class)) {
            StrutsTagAttribute stagAttribute = elem.getAnnotation(StrutsTagAttribute.class);

            // create Attribute and apply values found
            TagAttribute attribute = new TagAttribute();
            populateTagAttributes(attribute, stagAttribute);
            if (stagAttribute.name() == null || stagAttribute.name().isEmpty()) {
                String methodName = elem.getSimpleName().toString();
                String name = String.valueOf(Character.toLowerCase(methodName.charAt(3))) + methodName.substring(4);
                attribute.setName(name);
            }

            // add to map
            String typeName = elem.getEnclosingElement().toString();
            Tag parentTag = tags.get(typeName);
            if (parentTag != null)
                tags.get(typeName).addTagAttribute(attribute);
            else {
                // an abstract or base class
                parentTag = new Tag();
                parentTag.setDeclaredType(typeName);
                parentTag.setInclude(false);
                parentTag.addTagAttribute(attribute);
                tags.put(typeName, parentTag);
            }
        }

        // only annotations on the scanned packages are scanned so need to
        // manually scan their super classes
        for (Map.Entry<String, Tag> entry : tags.entrySet()) {
            processHierarchy(entry.getValue());
        }

        if (roundEnv.processingOver()) {
            // save
            // freemarker configuration
            Configuration config = new Configuration();
            config.setClassForTemplateLoading(getClass(), "");
            config.setObjectWrapper(new DefaultObjectWrapper());
            saveAsXml(config);
            saveTemplates(config);
        }
        return true;
    }

    private void populateTagAttributes(TagAttribute attribute, StrutsTagAttribute sTagattribute) {

        attribute.setRequired(sTagattribute.required());
        attribute.setRtexprvalue(sTagattribute.rtexprvalue());
        attribute.setDefaultValue(sTagattribute.defaultValue());
        attribute.setType(sTagattribute.type());
        attribute.setDescription(sTagattribute.description());
        attribute.setName(sTagattribute.name());
    }

    private void processHierarchy(final Tag tag) {

        try {
            Class<?> clazz = Class.forName(tag.getDeclaredType());
            List<String> skipAttributes = tag.getSkipAttributes();
            // skip hierarchy processing if the class is marked with the skip annotation
            // while(getAnnotation(TAG_SKIP_HIERARCHY, clazz.getAnnotations()) == null
            while (!clazz.isAnnotationPresent(StrutsTagSkipInheritance.class) && ((clazz = clazz.getSuperclass()) != null)) {
                Tag parentTag = tags.get(clazz.getName());
                // copy parent annotations to this tag
                if (parentTag != null) {
                    for (TagAttribute attribute : parentTag.getAttributes()) {
                        if (!skipAttributes.contains(attribute.getName()))
                            tag.addTagAttribute(attribute);
                    }
                }
                else {
                    // Maybe the parent class is already compiled
                    addTagAttributesFromParent(tag, clazz);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addTagAttributesFromParent(Tag tag, Class<?> clazz) throws ClassNotFoundException {
        
        try {
            BeanInfo info = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            List<String> skipAttributes = tag.getSkipAttributes();

            // iterate over class fields
            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                Method writeMethod = prop.getWriteMethod();

                // make sure it is public
                if (writeMethod != null && Modifier.isPublic(writeMethod.getModifiers())) {
                    // //can't use the genertic getAnnotation 'cause the class
                    // it not on this jar
                    // but seems to work for now TODO need to varify simple
                    // getAnnotation(Class) works
                    // Annotation annotation = getAnnotation(TAG_ATTRIBUTE,
                    // writeMethod.getAnnotations());
                    StrutsTagAttribute stagAttribute = writeMethod.getAnnotation(StrutsTagAttribute.class);
                    if (stagAttribute != null && !skipAttributes.contains(prop.getName())) {
                        // create tag
                        TagAttribute attribute = new TagAttribute();
                        // values.put("name", prop.getName());
                        populateTagAttributes(attribute, stagAttribute);
                        if (stagAttribute.name() == null || stagAttribute.name().isEmpty()) {
                            attribute.setName(prop.getName());
                        }
                        tag.addTagAttribute(attribute);
                    }
                }

            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    private Annotation getAnnotation(String typeName, Annotation[] annotations) {
//        for (int i = 0; i < annotations.length; i++) {
//            if (annotations[i].annotationType().getName().equals(typeName))
//                return annotations[i];
//        }
//        return null;
//    }

    private void checkOptions() {
        if (getOption("tlibVersion") == null)
            throw new IllegalArgumentException("'tlibVersion' is missing");
        if (getOption("jspVersion") == null)
            throw new IllegalArgumentException("'jspVersion' is missing");
        if (getOption("shortName") == null)
            throw new IllegalArgumentException("'shortName' is missing");
        if (getOption("description") == null)
            throw new IllegalArgumentException("'description' is missing");
        if (getOption("displayName") == null)
            throw new IllegalArgumentException("'displayName' is missing");
        if (getOption("uri") == null)
            throw new IllegalArgumentException("'uri' is missing");
        if (getOption("outTemplatesDir") == null)
            throw new IllegalArgumentException("'outTemplatesDir' is missing");
        if (getOption("outFile") == null)
            throw new IllegalArgumentException("'outFile' is missing");
    }

    private void saveTemplates(final Configuration config) {

        try {
            // load template
            Template template = config.getTemplate("tag.ftl");
            String rootDir = (new File(getOption("outTemplatesDir"))).getAbsolutePath();
            for (Tag tag : tags.values()) {
                if (tag.isInclude()) {
                    // model
                    HashMap<String, Tag> root = new HashMap<String, Tag>();
                    root.put("tag", tag);

                    // save file
                    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(rootDir, tag.getName() + ".html")));
                    try {
                        template.process(root, writer);
                    }
                    finally {
                        writer.close();
                    }
                }
            }
        }
        catch (Exception e) {
            // oops we cannot throw checked exceptions
            throw new RuntimeException(e);
        }
    }

    private void saveAsXml(final Configuration config) {
        try {
            Template template = config.getTemplate("tld.ftl");
            // create output directory if it does not exists
            File outputFile = new File(getOption("outFile"));
            File parentDir = outputFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "could not create parent folder for file " + outputFile.getCanonicalPath());
                }
            }
            Map<String, Object> data = new HashMap<String, Object>(7);
            data.put("jspVersion", getOption("jspVersion"));
            data.put("description", getOption("description"));
            data.put("displayName", getOption("displayName"));
            data.put("tlibVersion", getOption("tlibVersion"));
            data.put("shortName", getOption("shortName"));
            data.put("uri", getOption("uri"));
            data.put("tags", tags.values());

            // save file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            try {
                template.process(data, writer);
            }
            finally {
                writer.close();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String getOption(String name) {
        return options.get(name);
    }

}