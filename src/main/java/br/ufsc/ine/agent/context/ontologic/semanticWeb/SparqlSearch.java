package br.ufsc.ine.agent.context.ontologic.semanticWeb;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetImpl;

public class SparqlSearch {
    
    private static SparqlSearch singleton = null;
    
    public static SparqlSearch create() {
        if (singleton == null)
            singleton = new SparqlSearch();
        return singleton;
    }

    private final String OWL = "PREFIX owl: <http://www.w3.org/2002/07/owl#>";
    private final String XSD = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
    private final String RDFS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
    private final String RDF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    private final String FOAF = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    private final String DC = "PREFIX dc: <http://purl.org/dc/elements/1.1/>";
    private final String DB = "PREFIX : <http://dbpedia.org/resource/>";
    private final String DBPEDIA2 = "PREFIX dbpedia2: <http://dbpedia.org/property/>";
    private final String DBPEDIA = "PREFIX dbpedia: <http://dbpedia.org/>";
    private final String SKOS = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
    private final String DBO = "PREFIX dbo: <http://dbpedia.org/ontology/>";
    
    private final String PREFIXES = OWL + XSD + RDFS + RDF + FOAF + DC + DB + DBPEDIA2 + DBPEDIA + SKOS + DBO;
    
    private final String SERVICE = "SERVICE <http://DBpedia.org/sparql>";
    
    //M�todo respons�vel por realizar a consulta SPARQL e retornar o resultado
    public List<SparqlResult> searchDbpedia(List<SparqlObject> sparqlObjects) {

        String querySearch = "";
        String results = "";

        for (SparqlObject so : sparqlObjects) {
            querySearch += so.toString();
            if (so.isUriVariable() && !results.contains(so.getUri())) {
                results += " " + so.getUri();
            }
            if (so.isPredicateVariable() && !results.contains(so.getPredicate())) {
                results += " " + so.getPredicate();
            }
            if (so.isObjectVariable() && !results.contains((String) so.getObject())) {
                String s = (String) so.getObject();
                results += " " + s;
            }
        }

        String query = PREFIXES + 
                "SELECT " + results + " WHERE {"
                + SERVICE + " {"
                + querySearch
                + "}"
                + "}";
        
        //Cria a query sparql.
        Query queryExec = QueryFactory.create(query);
        //Prepara a execu��o da query sparql.
//        QueryExecution qe = QueryExecutionFactory.create(queryExec, new DatasetImpl(ModelFactory.createDefaultModel()));
        QueryExecution qe = QueryExecutionFactory.create(queryExec, new DatasetImpl(ModelFactory.createOntologyModel()));
        //Executa a query e obt�m o ResultSet como resultado
        ResultSet rs = qe.execSelect();
        
        //Cria a lista de objetos do tipo SparqlResult para receber os resultados.
        List<SparqlResult> itemList = new ArrayList<SparqlResult>();
        //Separa as vari�veis utilizadas como resultado para obter seus valores.
        String[] splitResult = results.trim().split(" ");
        
        //Itera sobre o resultSet para obter todos os resultados.
        while(rs.hasNext()) {
            
            //Obt�m o QuerySolution com base no resultSet.
            QuerySolution sol = rs.nextSolution();
            
            //Para todas as vari�veis de retorno
            for (String variable : splitResult) {
                //Cria o objeto respons�vel por armazenar o resultado da query
                SparqlResult sr = new SparqlResult();
                
                //Verifica se o retorno foi um Resource
                try {
                    sr.setResourceResult(sol.getResource(variable));
                } catch (ClassCastException e) {
                	System.out.println(e);
                }
                
                //Verifica se o retorno foi um Literal
                try {
                    sr.setLiteralResult(sol.getLiteral(variable));
                } catch (ClassCastException e) { 
                	System.out.println(e);
                }
                
                //Define a vari�vel respons�vel por este retorno
                sr.setVariable(variable);
                
                //Adiciona este SparqlResult a lista
                itemList.add(sr);
            }
            
        }
        
        //Retorna a lista de resultados
        return itemList;
        
    }
    
}
