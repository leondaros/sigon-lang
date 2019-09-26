package br.ufsc.ine.agent.context.ontologic.semanticWeb;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;


/**
 * Classe respons�vel por armazenar os resultados obtidos com base nas queries SPARQL
 * 
 * @author felipe.demarchi
 *
 */
public class SparqlResult {

    //Armazena o resultado caso for um Resource.
    private Resource resourceResult;
    //Armazena o resultado caso for um Literal.
    private Literal literalResult;
    //Armazena o nome da vari�vel respons�vel por obter este resultado.
    private String variable;

    /**
     * Construtor Default.
     */
    public SparqlResult() {}
    
    /**
     * Construtor passando a vari�vel e o Resource.
     * 
     * @param variable Vari�vel respons�vel por obter o resultado.
     * @param resourceResult Valor obtido em formato de Resource.
     */
    public SparqlResult(String variable, Resource resourceResult) {
        this.resourceResult = resourceResult;
        this.variable = variable;
    }

    /**
     * Construtor passando a vari�vel e o Literal.
     * 
     * @param variable Vari�vel respons�vel por obter o resultado.
     * @param literalResult Valor obtido em formato de Literal.
     */
    public SparqlResult(String variable, Literal literalResult) {
        this.literalResult = literalResult;
        this.variable = variable;
    }

    //Getters and Setters
    
    public Resource getResourceResult() {
        return resourceResult;
    }

    public void setResourceResult(Resource resourceResult) {
        this.resourceResult = resourceResult;
    }

    public Literal getLiteralResult() {
        return literalResult;
    }

    public void setLiteralResult(Literal literalResult) {
        this.literalResult = literalResult;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

}
