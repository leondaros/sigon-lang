package br.ufsc.ine.agent.context.ontologic;

import java.util.ArrayList;
import java.util.List;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;
import br.ufsc.ine.agent.context.ContextService;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlObject;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlResult;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlSearch;
import br.ufsc.ine.utils.PrologEnvironment;

public class OntologicContextService implements ContextService{
	
	private static OntologicContextService instance = new OntologicContextService();
	private static PrologEnvironment prologEnvironment;
	
	SparqlSearch sparqlSearch;
	List<SparqlObject> sparqlObjects;
	List<String> mappedPredicates; 
	
	public OntologicContextService() {
		prologEnvironment = new PrologEnvironment();
		sparqlSearch = new SparqlSearch();
		sparqlObjects = new ArrayList<SparqlObject>();
		initMappedPredicates();
	}

	@Override
	public Theory getTheory() {
		return prologEnvironment.getEngine().getTheory();
	}

	@Override
	public boolean verify(String fact) {
		SolveInfo solveGoal;
		try {
			solveGoal = prologEnvironment.solveGoal(fact);
			return solveGoal.isSuccess();
		} catch (MalformedGoalException e) {
			return false;
		}
	}

	@Override
	public void appendFact(String fact) {
		String subject,object = "";
		subject = getURI(fact);
		for (String predicate : mappedPredicates) {
			List<SparqlResult> result = executeQuery("<"+subject+">", predicate, "?value");
			if (!result.isEmpty()) {
	            for (SparqlResult sr : result) {
	            	if(sr.getResourceResult()!=null){
	            		
	            		//ADICIONAR ESTE RESOURCE AS INTENTIONS
	            		
	            		object = getResourceLabel(sr.getResourceResult().getURI()).toString();
	            	}
	            	if(sr.getLiteralResult()!=null){
	            		object = sr.getLiteralResult().getString();
	            	}
	            }
				String newFact = formatPredicate(predicate)+"("+fact+","+object+")";
				try {
					prologEnvironment.appendFact(newFact);
				} catch (InvalidTheoryException e) {
					e.printStackTrace();
				}
	        }
		}
	}
	
	public List<SparqlResult> getWrongAnswer(){
		return null;
	}
	
	public String getURI(String label){
		this.addObjectToList("?uri", "rdfs:label", ""+label+"");
		List<SparqlResult> result = this.searchNewURI(sparqlObjects);
		return result.get(0).getResourceResult().getURI();
	}

	public String formatPredicate(String predicate){
		return predicate.substring(predicate.lastIndexOf(":") + 1);
	}
	
	public SparqlResult getResourceLabel(String subject){
		this.addObjectToList("<"+subject+">", "rdfs:label", "?label");
		return this.searchNewURI(sparqlObjects).get(0);
	}
	
	@Override
	public void addInitialFact(String fact) throws InvalidTheoryException {
		prologEnvironment.appendFact(fact);
	}

	@Override
	public String getName() {
		return "ontologic";
	}
	
	public void initMappedPredicates(){
		mappedPredicates = new ArrayList<String>();
		
		//Where
		mappedPredicates.add("dbo:country");
		mappedPredicates.add("dbo:isPartOf");
		mappedPredicates.add("geo:lat");
		mappedPredicates.add("geo:long");
		
		//When
		mappedPredicates.add("dbo:foundingDate");
		
		//Who
		mappedPredicates.add("dbo:leaderName");
		
		//What
		mappedPredicates.add("foaf:nick");
		mappedPredicates.add("dbo:capital");
		mappedPredicates.add("dbo:currency");
		mappedPredicates.add("dbo:officialLanguage");
		mappedPredicates.add("dbo:largestCity");
		
		//How Many
		mappedPredicates.add("dbo:populationalTotal");
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
	
	public List<SparqlResult> executeQuery(String subject,String predicate, String object){
		addObjectToList(subject, predicate, object);
		return searchNewURI(getSparqlObjects());
	}

	public static OntologicContextService getInstance() {
		return instance;
	}

	public static void setInstance(OntologicContextService instance) {
		OntologicContextService.instance = instance;
	}

	public SparqlSearch getSparqlSearch() {
		return sparqlSearch;
	}

	public void setSparqlSearch(SparqlSearch sparqlSearch) {
		this.sparqlSearch = sparqlSearch;
	}

	public List<SparqlObject> getSparqlObjects() {
		return sparqlObjects;
	}

	public void setSparqlObjects(List<SparqlObject> sparqlObjects) {
		this.sparqlObjects = sparqlObjects;
	}

	public List<String> getMappedPredicates() {
		return mappedPredicates;
	}

	public void setMappedPredicates(List<String> mappedPredicates) {
		this.mappedPredicates = mappedPredicates;
	}
	
}
