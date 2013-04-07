
package com.lunary.struts.annotations.taglib.processor;

/**
 * Used to hold tag attribute information for TLD generation
 * 
 */
public class TagAttribute {
    private String name;
    private boolean required;
    private boolean rtexprvalue;
    private String description;
    private String defaultValue;
    private String type;

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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRtexprvalue() {
        return rtexprvalue;
    }

    public void setRtexprvalue(boolean rtexprvalue) {
        this.rtexprvalue = rtexprvalue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}