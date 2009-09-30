<?xml version="1.0" encoding="UTF-8"?>
<preesm:workflow xmlns:preesm="http://ietr-image.insa-rennes.fr/projects/Preesm">
    <preesm:algorithm/>
    <preesm:architecture/>
    <preesm:scenario/>
    <preesm:task pluginId="org.ietr.preesm.plugin.codegen" taskId="codegen">
        <data key="variables">
            <variable name="sourcePath" value="/tutorial1/Code"/>
            <variable name="xslLibraryPath" value="/tutorial1/Code/XSL"/>
        </data>
    </preesm:task>
    <preesm:task pluginId="org.ietr.preesm.plugin.mapper.plot" taskId="DAG Plotter">
        <data key="variables"/>
    </preesm:task>
    <preesm:task
        pluginId="org.ietr.preesm.plugin.mapper.exporter.ImplExportTransform" taskId="ImplementationExporter">
        <data key="variables">
            <variable name="path" value="/tutorial1/DAG/outDAG.xml"/>
        </data>
    </preesm:task>
    <preesm:task pluginId="org.ietr.preesm.plugin.exportXml.sdf4jgml" taskId="Exporter">
        <data key="variables">
            <variable name="path" value="/tutorial1/DAG/flatten.graphml"/>
        </data>
    </preesm:task>
    <preesm:task pluginId="org.ietr.preesm.plugin.transforms.sdf2hsdf" taskId="HSDF">
        <data key="variables"/>
    </preesm:task>
    <preesm:task
        pluginId="org.ietr.preesm.plugin.transforms.flathierarchy" taskId="HierarchyFlattening">
        <data key="variables">
            <variable name="depth" value="2"/>
        </data>
    </preesm:task>
    <preesm:task pluginId="org.ietr.preesm.plugin.mapper.fast" taskId="FAST mapper">
        <data key="variables">
            <variable name="balanceLoads" value="false"/>
            <variable name="displaySolutions" value="true"/>
            <variable name="edgeSchedType" value="Simple"/>
            <variable name="fastLocalSearchTime" value="5"/>
            <variable name="fastTime" value="200"/>
            <variable name="simulatorType" value="AccuratelyTimed"/>
        </data>
    </preesm:task>
    <preesm:dataTransfer from="__scenario" sourceport=""
        targetport="scenario" to="DAG Plotter"/>
    <preesm:dataTransfer from="__architecture" sourceport=""
        targetport="architecture" to="codegen"/>
    <preesm:dataTransfer from="__scenario" sourceport=""
        targetport="scenario" to="__algorithm"/>
    <preesm:dataTransfer from="__scenario" sourceport=""
        targetport="scenario" to="__architecture"/>
    <preesm:dataTransfer from="__algorithm" sourceport=""
        targetport="SDF" to="ImplementationExporter"/>
    <preesm:dataTransfer from="__architecture" sourceport=""
        targetport="architecture" to="ImplementationExporter"/>
    <preesm:dataTransfer from="__scenario" sourceport=""
        targetport="scenario" to="ImplementationExporter"/>
    <preesm:dataTransfer from="__algorithm" sourceport=""
        targetport="SDF" to="HierarchyFlattening"/>
    <preesm:dataTransfer from="HierarchyFlattening" sourceport="SDF"
        targetport="SDF" to="HSDF"/>
    <preesm:dataTransfer from="HSDF" sourceport="SDF" targetport="SDF" to="Exporter"/>
    <preesm:dataTransfer from="HSDF" sourceport="SDF" targetport="SDF" to="FAST mapper"/>
    <preesm:dataTransfer from="__architecture" sourceport=""
        targetport="architecture" to="FAST mapper"/>
    <preesm:dataTransfer from="__scenario" sourceport=""
        targetport="scenario" to="FAST mapper"/>
    <preesm:dataTransfer from="FAST mapper" sourceport="ABC"
        targetport="ABC" to="DAG Plotter"/>
    <preesm:dataTransfer from="FAST mapper" sourceport="DAG"
        targetport="DAG" to="codegen"/>
    <preesm:dataTransfer from="FAST mapper" sourceport="DAG"
        targetport="DAG" to="ImplementationExporter"/>
</preesm:workflow>