<?xml version="1.0" encoding="UTF-8"?>
<taglib xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="${jspVersion}" xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd">
  <description><![CDATA[${description}]]></description>
  <display-name>${displayName}</display-name>
  <tlib-version>${tlibVersion}</tlib-version>
  <short-name>${shortName}</short-name>
  <uri>${uri}</uri>
  <#list tags as t>
  <#if t.include>
  <tag>
    <description><#if t.description??><![CDATA[${t.description}]]></#if></description>
    <name>${t.name}</name>
    <tag-class>${t.tldTagClass}</tag-class>
    <body-content>${t.tldBodyContent}</body-content>
    <#list t.attributes as a>
    <attribute>
      <description><#if a.description??><![CDATA[${a.description}]]></#if></description>
      <name>${a.name}</name>
      <required>${a.required?string}</required>
      <rtexprvalue>${a.rtexprvalue?string}</rtexprvalue>
    </attribute>
    </#list>
    <dynamic-attributes>${t.allowDynamicAttributes?string}</dynamic-attributes>
  </tag>
  </#if>
  </#list>
</taglib>