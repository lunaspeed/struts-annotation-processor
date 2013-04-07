
package com.lunary.struts.annotations.taglib.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Tag {
    private String name;
    private String tldBodyContent;
    private String tldTagClass;
    private String description;
    private boolean allowDynamicAttributes;
    private boolean include = true;
    private String declaredType;
    private Map<String, TagAttribute> attributes = new TreeMap<String, TagAttribute>();
    private List<String> skipAttributes = new ArrayList<String>();
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTldBodyContent() {
        return tldBodyContent;
    }

    public void setTldBodyContent(String tldBodyContent) {
        this.tldBodyContent = tldBodyContent;
    }

    public String getTldTagClass() {
        return tldTagClass;
    }

    public void setTldTagClass(String tldTagClass) {
        this.tldTagClass = tldTagClass;
    }

    public void addTagAttribute(TagAttribute attribute) {
        if (!attributes.containsKey(attribute.getName()))
            attributes.put(attribute.getName(), attribute);
    }

    public Collection<TagAttribute> getAttributes() {
        return attributes.values();
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public boolean isAllowDynamicAttributes() {
        return allowDynamicAttributes;
    }

    public void setAllowDynamicAttributes(boolean allowDynamicAttributes) {
        this.allowDynamicAttributes = allowDynamicAttributes;
    }
    
    public String getDeclaredType() {
        return declaredType;
    }

    public void setDeclaredType(String declaredType) {
        this.declaredType = declaredType;
    }

    public List<String> getSkipAttributes() {
        return skipAttributes;
    }
    
    public void addSkipAttribute(String name) {
        this.skipAttributes.add(name);
    }
}