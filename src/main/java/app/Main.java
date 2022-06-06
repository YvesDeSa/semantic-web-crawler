package app;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import implementation.SemanticCrawlerImplementation;

public class Main {
	
	public static final String URI = "http://dbpedia.org/resource/Churchill";

	public static void main(String[] args) {
		
		SemanticCrawlerImplementation data = new SemanticCrawlerImplementation();
		Model graph = ModelFactory.createDefaultModel();

		data.search(graph, URI);
		graph.write(System.out);

	}

}
