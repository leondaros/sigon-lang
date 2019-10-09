package br.ufsc.ine.agent.context.ontologic;

import java.util.ArrayList;
import java.util.List;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;
import br.ufsc.ine.agent.Agent;
import br.ufsc.ine.agent.bridgerules.Body;
import br.ufsc.ine.agent.bridgerules.BridgeRule;
import br.ufsc.ine.agent.bridgerules.Head;
import br.ufsc.ine.agent.context.ContextService;
import br.ufsc.ine.agent.context.LangContext;
import br.ufsc.ine.agent.context.beliefs.BeliefsContextService;
import br.ufsc.ine.agent.context.communication.CommunicationContextService;
import br.ufsc.ine.agent.context.desires.DesiresContextService;
import br.ufsc.ine.agent.context.intentions.IntentionsContextService;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlObject;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlResult;
import br.ufsc.ine.agent.context.ontologic.semanticWeb.SparqlSearch;
import br.ufsc.ine.agent.context.plans.PlansContextService;
import br.ufsc.ine.utils.PrologEnvironment;
import java.text.Normalizer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

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
			System.out.println(fact+" malformed");
			return false;
		}
	}
	
	public void ontologic(List<LangContext> langContexts) {
		langContexts.forEach(ctx -> {
			ctx.getClauses().forEach(clause -> {
				try {
					this.addInitialFact(clause);
				} catch (InvalidTheoryException e) {
					e.printStackTrace();
				}
			});
		});
	}

	@Override
	public void appendFact(String fact) {
		String subject = "";
		String content = getContent(fact);
		removeNewResourceStatus(content);
		fact = stringToInputFormat(getContent(fact));
//		subject = getURI(fact);
		subject = "http://dbpedia.org/resource/"+fact;
		if(subject!=""){
			for (String predicate : mappedPredicates) {
				String newFact = iterateUriInPredicates(fact,subject,predicate);
				if(newFact!=""){
					apprendToProlog(newFact);
				}
			}
		}
	}
	
	public String getContent(String fact){
		return fact.substring(fact.indexOf("(") + 1, fact.lastIndexOf(")"));
	}
	
	public String encodeString(String value){
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
	}
	
	public String decodeString(String value){
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
	}
	
	public String iterateUriInPredicates(String fact, String subject, String predicate){
		String object = "";
		String newFact = "";
		List<SparqlResult> result = executeQuery("<"+subject+">", predicate, "?value", null);
		if (!result.isEmpty()){
            for (SparqlResult sr : result){
            	if(sr.getResourceResult()!=null){
            		//ADICIONAR ESTE RESOURCE AS INTENTIONS
            		object = getResourceLabel(sr.getResourceResult().getURI());
//            		String objectFormated = encodeString(firstCharLowerCase(object));
            		String objectFormated = stringToOutputFormat(object);
            		appendNewResource(objectFormated);
            	}
            	if(sr.getLiteralResult()!=null){
            		object = sr.getLiteralResult().getString();
            	}
            }
			newFact = formatPredicate(predicate)+"("+stringToOutputFormat(fact)+","+stringToOutputFormat(object)+").";
        }
		return newFact;
	}
	public void appendNewResource(String resource){
		apprendToProlog("newResource("+resource+").");
	}
	
	public void apprendToProlog(String newFact){
		boolean update = false;
		try {
			update = this.verify(newFact);
			if (update) {
				prologEnvironment.updateFact(newFact, newFact);
			} else {
				prologEnvironment.appendFact(newFact);
			}
		} catch (InvalidTheoryException e) {
			e.printStackTrace();
		}
	}
	
	public void removeNewResourceStatus(String c){
		String removedFact = "newResource("+c+")";
		if (prologEnvironment.getEngine().getTheory().toString().contains(removedFact)) {
//			c = c.replace("-", "").trim();
			Agent.removeBelief = true;
		}
		if (Agent.removeBelief) {
			Agent.removeBelief = false;
			try {
				prologEnvironment.removeFact(removedFact);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String stringToOutputFormat(String s) {
		s = firstCharLowerCase(s);
	    s = Normalizer.normalize(s, Normalizer.Form.NFD);
	    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
	    s = s.replaceAll("\\s","_");
	    return s;
	}
	
	public String stringToInputFormat(String s) {
		s = firstCharUpperCase(s);
//	    s = s.replaceAll("_"," ");
	    return s;
	}
	
	public List<SparqlResult> getWrongAnswer(){
		return null;
	}
	
	public List<SparqlResult> searchNewURI(List<SparqlObject> objects){
		List<SparqlResult> result = sparqlSearch.searchDbpedia(objects);
		clearObjectsList();
		return result;
	}
	
	public List<SparqlResult> executeQuery(String subject,String predicate, String object, String filter){
		addObjectToList(subject, predicate, object, filter);
		return searchNewURI(getSparqlObjects());
	}
	
	
	public String getURI(String label){
		List<SparqlResult> result = executeQuery("?uri", "rdfs:label", "'"+label+"'@en",null);
		if (!result.isEmpty()) {
			for (SparqlResult sparqlResult : result) {
				if(!sparqlResult.getResourceResult().getURI().contains("wikidata")){
					return sparqlResult.getResourceResult().getURI();
				}
			}
		}
		return "";
	}

	public String firstCharUpperCase(String label){
		return label.substring(0,1).toUpperCase() + label.substring(1).toLowerCase();
	}
	
	public String firstCharLowerCase(String label){
		return label.substring(0,1).toLowerCase() + label.substring(1);
	}
	
	public String formatPredicate(String predicate){
		return predicate.substring(predicate.lastIndexOf(":") + 1);
	}
	
	public String getResourceLabel(String subject){
		this.addObjectToList("<"+subject+">", "rdfs:label", "?label","FILTER (lang(?label) = 'en')");
		return this.searchNewURI(sparqlObjects).get(0).getLiteralResult().getString();
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
//		
//		//How Many
		mappedPredicates.add("dbo:populationalTotal");
	}
	
	public void clearObjectsList(){
		sparqlObjects.clear();
	}
	
	public void addObjectToList(String subject,String predicate, String object, String filter){
		SparqlObject triple = new SparqlObject();
		triple.setUri(subject);
		triple.setPredicate(predicate);
		triple.setObject(object);
		triple.setFilter(filter);
		sparqlObjects.add(triple);
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
