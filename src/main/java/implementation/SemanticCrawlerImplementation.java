package implementation;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;

import crawler.SemanticCrawler;

public class SemanticCrawlerImplementation implements SemanticCrawler {
	
	CharsetEncoder enc = Charset.forName("ISO-8859-1").newEncoder();
	
	private Set<String> arrived = new HashSet<String>();
	
	@Override
	public void search(Model graph, String resourceURI) {
		graph.add(getResourceModel(resourceURI));
	}
	
	private Model getResourceModel(String resourceURI){
		arrived.add(resourceURI);
		
		Model defaultModel = ModelFactory.createDefaultModel();
		Model readModel = readURIModel(resourceURI);
		Resource resource = ResourceFactory.createResource(resourceURI);
		
		defaultModel.add(getEquivalentResourceModel(readModel, resource));
		defaultModel.add(getResourceStatements(readModel, resource));
		
		return defaultModel;
	}

	private Model readURIModel(String resourceURI){
		Model defaultModel = ModelFactory.createDefaultModel();
		
		defaultModel.read(resourceURI);
		
		return defaultModel;
	}

	private Model getEquivalentResourceModel(Model readModel, Resource resource){
		Model defaultModel = ModelFactory.createDefaultModel();
		
		Iterator<Statement> subject = readModel.listStatements(resource, OWL.sameAs, (Resource) null);
		Iterator<Statement> object = readModel.listStatements(null, OWL.sameAs, resource);
		
		defaultModel.add(crawlForEquivalentModels(subject));
		defaultModel.add(crawlForEquivalentModels(object));
		
		return defaultModel;
	}
	
	private boolean isUriValid(String URI) {
		return !arrived.contains(URI) && enc.canEncode(URI);
	}

	private Model crawlForEquivalentModels(Iterator<Statement> statements){
		Model defaultModel = ModelFactory.createDefaultModel();
		Statement statement;
		Resource resource, subject;
		String resourceURI, subjectURI;
		
		while(statements.hasNext())
		{
			statement = statements.next();
			resource = statement.getResource();
			subject = statement.getSubject();
			
			resourceURI = (resource.isURIResource()) ? resource.getURI() : null;
			subjectURI = (subject.isURIResource()) ? subject.getURI() : null;
			
			if(isUriValid(resourceURI))
				defaultModel.add(getResourceModel(resourceURI));
				
			if(isUriValid(subjectURI))
				defaultModel.add(getResourceModel(resourceURI) );				
		}
		return defaultModel;
	}
	
	private Model getResourceStatements(Model readModel, Resource resource){
		Model defaultModel = ModelFactory.createDefaultModel();
		Statement statement;
		RDFNode node;
		
		Iterator<Statement> statements = readModel.listStatements(resource, null, (RDFNode)null);
		
		while(statements.hasNext())
		{
			statement = statements.next();
			node = statement.getObject();
			
			if (node.isAnon())
				defaultModel.add(getResourceStatements(readModel, (Resource) node));
			
			defaultModel.add(statement);
		}
		
		return defaultModel;
	}
	
}
