package org.ietr.preesm.core.codegen.model;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.codegen.ImplementationPropertyNames;
import org.ietr.preesm.core.codegen.VertexType;
import org.sdf4j.model.sdf.SDFEdge;
import org.sdf4j.model.sdf.SDFVertex;

public class CodeGenSDFVertex extends SDFVertex{

	public static final String OPERATOR = ImplementationPropertyNames.Vertex_Operator; 
	public static final String NB_REPEAT ="nb_repeat";
	public static final String POS =ImplementationPropertyNames.Vertex_schedulingOrder;
	public static final String TYPE =ImplementationPropertyNames.Vertex_vertexType;
	
	public CodeGenSDFVertex(){
		this.getPropertyBean().setValue(TYPE,  VertexType.task);
	}
	
	
	public ArchitectureComponent getOperator(){
		return (ArchitectureComponent) this.getPropertyBean().getValue(OPERATOR, ArchitectureComponent.class);
	}
	
	public void setOperator(ArchitectureComponent op){
		this.getPropertyBean().setValue(OPERATOR, getOperator(), op);
	}
	
	public int getPos(){
		if(this.getPropertyBean().getValue(POS) != null){
			return (Integer) this.getPropertyBean().getValue(POS, Integer.class);
		}
		return 0 ;
	}
	
	public void setPos(int pos){
		this.getPropertyBean().setValue(POS, getPos(), pos);
	}
	
	public int getNbRepeat(){
		return (Integer) this.getPropertyBean().getValue(NB_REPEAT, Integer.class);
	}
	
	public void setNbRepeat(int nb){
		this.getPropertyBean().setValue(NB_REPEAT, nb);
	}
	
	
	public String toString(){
		String code = new String();
		if(getNbRepeat() > 1){
			code +="for(ind = 0 ; ind <"+getNbRepeat()+" ; ind ++){\n";
			if(this.getGraphDescription() == null){
				code += this.getName()+"(";
				for(SDFEdge edge : this.getBase().incomingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				for(SDFEdge edge : this.getBase().outgoingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				code = code.substring(0,code.length()-1);
				code +=");\n";
			}else{
				code +=this.getGraphDescription().toString();
				code +=";\n";
			}
			code +="}\n";
		}else{
			if(this.getGraphDescription() == null && this.getRefinement() instanceof FunctionCall){
				code += ((FunctionCall) this.getRefinement()).getFunctionName()+"(";
				for(SDFEdge edge : this.getBase().incomingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				for(SDFEdge edge : this.getBase().outgoingEdgesOf(this)){
					code+=edge.getSource().getName()+"_"+edge.getSourceInterface().getName()+",";
				}
				code = code.substring(0,code.length()-1);
				code +=");\n";
			}else{
				code +=this.getGraphDescription().toString();
			}
		}
		
		return code ;
	}
}