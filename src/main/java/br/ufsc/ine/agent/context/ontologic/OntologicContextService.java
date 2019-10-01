package br.ufsc.ine.agent.context.ontologic;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.ine.agent.context.custom.CustomContext;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlObject;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlResult;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlSearch;

public class OntologicContextService extends CustomContext{
	
	SparqlSearch sparqlSearch = new SparqlSearch();
	List<SparqlObject> sparqlObjects = new ArrayList<SparqlObject>();
		
	public OntologicContextService(String name) {
		super(name);
	}
	
	public void clearObjectsList(){
		sparqlObjects.clear();
	}
	
	public void addObjectToList(String subject,String predicate, String object){
		SparqlObject triple = new SparqlObject();
		triple.setUri(subject);
		triple.setPredicate(predicate);
		triple.setObject(object);
		sparqlObjects.add(triple);
	}
	
	public List<SparqlResult> searchNewURI(List<SparqlObject> objects){
		clearObjectsList();
		return sparqlSearch.searchDbpedia(objects);
	}
	
	public List<SparqlResult> searchKnownURI(List<SparqlObject> objects){
		return null;
	}
	
}
