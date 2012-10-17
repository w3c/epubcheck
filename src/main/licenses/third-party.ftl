<#-- To render the third-party file.
 Available context :

 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<#function licenseFormat licenses>
    <#assign result = "    "/>
    <#list licenses as license>
        <#assign result = result + license/>
    </#list>
    <#return result>
</#function>
<#function artifactFormat p>
    <#return "  " + p.name + ", v" + p.version + "\n"+ "    by "+ p.organization.name +" (" + (p.url!"no url defined") + ")">
</#function>
Licenses of third-party dependencies
------------------------------------

<#list dependencyMap as e>
    <#assign project = e.getKey()/>
    <#assign licenses = e.getValue()/>
  ${project.name}, ${project.version}
<#--     by ${project.organization.name} (${project.url}) -->
    <#list licenses as license>
    ${license}
    </#list>

</#list>

Copies of the licenses are provided in the 'licenses' directory.