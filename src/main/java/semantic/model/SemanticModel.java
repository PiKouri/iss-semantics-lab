package semantic.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.update.UpdateAction;

import fr.irit.melodi.sparql.query.ask.SparqlAsk;
import fr.irit.melodi.sparql.query.dataquery.insert.SparqlInsertData;
import fr.irit.melodi.sparql.query.dataquery.insert.SparqlInsertWhere;
import fr.irit.melodi.sparql.query.select.SparqlSelect;

public class SemanticModel implements IConvenienceInterface
{
	
	public final static String SSN = "http://purl.oclc.org/NET/ssnx/ssn#";
	public final static String SAREF = "http://ontology.tno.nl/saref#";
	public final static String SAN = "http://www.irit.fr/recherches/MELODI/ontologies/SAN.owl#";
	public final static String IOT_O = "http://www.irit.fr/recherches/MELODI/ontologies/IoT-O.owl#";
	public final static String ADREAM = "http://pelican/adreamdata#";
	public final static String BASE = "http://tp.5iss.fr#";
	
	// Separating prefixes into several lists lightens requests
	public static Set<Entry<String, String>> IOT_PREFIXES;
	public static Set<Entry<String, String>> SERVICES_PREFIXES;
	public static Set<Entry<String, String>> ALL_PREFIXES;
	public static Map<String, String> FEATURES;
	
	static
	{
		Set<Entry<String, String>> tmp1 = new HashSet<Map.Entry<String,String>>();
		tmp1.add(new AbstractMap.SimpleEntry<String, String>( "iot-o","<"+IOT_O+">"));
		tmp1.add(new AbstractMap.SimpleEntry<String, String>("ssn","<"+SSN+">"));
		tmp1.add(new AbstractMap.SimpleEntry<String, String>("DUL",
				"<http://www.loa-cnr.it/ontologies/DUL.owl#>"));
		tmp1.add(new AbstractMap.SimpleEntry<String, String>("san",
				"<http://www.irit.fr/recherches/MELODI/ontologies/SAN.owl#>"));
		tmp1.add(new AbstractMap.SimpleEntry<String, String>("lifecycle",
				"<http://purl.org/vocab/lifecycle/schema#>"));
		tmp1.add(new AbstractMap.SimpleEntry<String, String>("adream",	"<"+ADREAM+">"));
		IOT_PREFIXES = Collections.unmodifiableSet(tmp1);
		
		Set<Entry<String, String>> tmp2 = new HashSet<Map.Entry<String,String>>();
		tmp2.add(new AbstractMap.SimpleEntry<String, String>("wsmo-lite",
				"<http://www.wsmo.org/ns/wsmo-lite#>"));
		tmp2.add(new AbstractMap.SimpleEntry<String, String>("msm",
				"<http://iserve.kmi.open.ac.uk/ns/msm#>"));
		tmp2.add(new AbstractMap.SimpleEntry<String, String>("hrests",
				"<http://www.wsmo.org/ns/hrests#>"));
		SERVICES_PREFIXES = Collections.unmodifiableSet(tmp2);
		
		Set<Entry<String, String>> tmp3 = new HashSet<Map.Entry<String,String>>();
		tmp3.addAll(tmp1);
		tmp3.addAll(tmp2);
		ALL_PREFIXES = Collections.unmodifiableSet(tmp3);
		Map<String, String> tmp4 = new HashMap<String, String>();
		tmp4.put("Temperature","http://qudt.org/vocab/quantity#ThermodynamicTemperature");
		tmp4.put("Light","http://qudt.org/vocab/quantity#LuminousIntensity");
		FEATURES = Collections.unmodifiableMap(tmp4);
	}
	
	private Model model;
	private Dataset dataset;
	private DatasetGraph dsg;
	private String temperatureSensor1URI;
	private String temperatureSensor2URI;
	private String temperatureSensor3URI;
	private String humiditySensor1URI;
	private String temperatureURI;
	private String humidityURI;

	/**
	 * Creates an empty model (for test purpose mainly)
	 */
	public SemanticModel()
	{
		this.dsg = DatasetGraphFactory.create();
		this.dataset = DatasetFactory.wrap(dsg);
		this.model = this.dataset.getDefaultModel();
	}
	
	/**
	 * Creates a model already loaded with triples issued from
	 * the provided ontology, as well as another knowledge base
	 * describing sensors and their ranges.
	 * @param ontologyPath
	 */
	public SemanticModel(String ontologyPath)
	{
		this.dsg = DatasetGraphFactory.create();
		this.dataset = DatasetFactory.wrap(dsg);
		this.model = this.dataset.getDefaultModel();
		this.loadFromFile("./tp2_sensors.rdf", "", "RDF/XML", true);
		this.loadFromFile(ontologyPath, "", "TURTLE", true);
		// Computation of URI that will be needed often, to limit frequent requests
		this.temperatureSensor1URI = this.getEntityURI("TemperatureSensor_1").get(0);
		this.temperatureSensor2URI = this.getEntityURI("TemperatureSensor_2").get(0);
		this.temperatureSensor3URI = this.getEntityURI("TemperatureSensor_3").get(0);
		this.temperatureURI = this.getEntityURI("Temperature").get(0);
		this.humidityURI = this.getEntityURI("Humidity").get(0);
	}
	
	public void beginWriteTransaction()
	{
		this.dataset.begin(ReadWrite.WRITE);
	}
	
	public void endTransaction()
	{
		this.dataset.end();
		this.dataset.begin(ReadWrite.READ);
		this.model = this.dataset.getDefaultModel();
		this.dataset.end();
	}
	
	void updateDataset(String queryString)
	{	
		UpdateAction.parseExecute(queryString, this.dsg);
	}
	
	public void commitDataset()
	{
		this.dataset.commit();
	}
	
	public void updateModel()
	{
		this.dataset.begin(ReadWrite.READ);
		this.model = this.dataset.getDefaultModel();
		this.dataset.end();
	}
	
	public Model getModel()
	{
		return this.model;
	}
	
	public static String formatURIForSparql(String uri)
	{
		if(uri.startsWith("http"))
		{
			return "<"+uri+">";
		}
		return uri;
	}
	
	public static List<Map<String, String>> queryModel(String queryString, Model m)
	{
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		Query query = QueryFactory.create(queryString) ;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m))
		{
			ResultSet results = qexec.execSelect();
			while (results.hasNext())
			{
				QuerySolution soln = results.next();
				resultList.add(new HashMap<String, String>());
				List<String> varNameTestList = new ArrayList<String>();
				Iterator<String> querySolutionVariables = soln.varNames();
				while(querySolutionVariables.hasNext())
				{
					varNameTestList.add(querySolutionVariables.next());
				}
				// On va ajouter pour chaque résultat une map contenant les noms de variables et leurs valeurs
				for(String varName : varNameTestList)
				{
					RDFNode x = soln.get(varName); // Get a result variable by name.
					resultList.get(resultList.size()-1).put(varName, x.toString());
				}
			}
			return resultList;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Map<String, String>> processReadQuery(String queryString)
	{
		return queryModel(queryString, this.model);
	}
	
	public boolean processWriteQuery(String query)
	{
		try
		{
//			this.beginWriteTransaction();
			this.updateDataset(query);
//			this.commitDataset();
//			this.endTransaction();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
//			this.endTransaction();
			return false;
		}
	}
	
	public boolean askDataset(String queryString)
	{
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		boolean result = qexec.execAsk() ;
		qexec.close() ;
		return result;
	}
	
	private String createSparqlDataFromStatement(Statement s, boolean acceptObjectProperties)
	{
		String subject = null;
		String predicate = null;
		String object = null;
		if(s.getSubject().toString().startsWith("http"))
		{
			subject = "<"+s.getSubject().toString()+">";
		}
		if(s.getPredicate().toString().startsWith("http"))
		{
			predicate = "<"+s.getPredicate().toString()+">";
		}
		if((s.getObject().toString().startsWith("http") && !s.getObject().toString().contains("skos")))
		{
			object = "<"+s.getObject().toString()+">";
		}
		// FIXME find why object properties mess it up -> temporary fix, can be activated for safe files
		else if (acceptObjectProperties)
		{
			// In this case, object is a data and not a resource
			object = "\""+s.getObject().toString()+"\"";
		}
		if(subject != null && predicate != null && object != null)
		{
			return subject+" "+predicate+" "+object+".\n";
		}
		else
		{
			return null;
		}
	}
	
	void writeFromModel(Model m, boolean acceptObjectProperties)
	{
		StmtIterator i = m.listStatements();
		String data = "";
		while(i.hasNext())
		{
			Statement s = i.next();
			String dataInstance = this.createSparqlDataFromStatement(s, acceptObjectProperties);
			if(dataInstance != null)
			{
				data+=dataInstance;
			}
		}
		// No need for prefixes, they are resolved in the model
		SparqlInsertData s = new SparqlInsertData(null, data);
		if(!this.processWriteQuery(s.toString()))
		{
			System.out.println("writeFomModel failed for model "+m);
		}
	}
	
	void loadFromFile(String path, String base, String lang, boolean supportObjectProperties)
	{
		Model tmpModel = ModelFactory.createDefaultModel();
		try (FileInputStream fis = new FileInputStream(path);
		        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
		        BufferedReader reader = new BufferedReader(isr)
		   ) {

		   tmpModel.read(reader, base, lang);
		   this.writeFromModel(tmpModel, supportObjectProperties);

		   } catch (IOException e) {
		       e.printStackTrace();
		   }
	}
	
	@Override
	public String whichSensorDidIt(String timestamp, String paramURI)
	{
		TimestampEntity t = new TimestampEntity(timestamp);
		if(paramURI.equals(this.temperatureURI))
		{
			// Pour une raison qui leur est propre, les capteurs de température de
			// la station font les 3*8. Le taylorisme est passé par là.
			if(t.getHour()>=5 && t.getHour()<13)
			{
				return this.temperatureSensor1URI;
			}
			else if(t.getHour()>=13 && t.getHour()<21)
			{
				return this.temperatureSensor2URI;
			}
			else
			{
				return this.temperatureSensor3URI;
			}
		}
		else if(paramURI.equals(this.humidityURI))
		{
			return this.humiditySensor1URI;
		}
		else
		{
			return null;
		}
	}
	
	public boolean URIExists(String URI)
	{
		URI = SemanticModel.formatURIForSparql(URI);
		SparqlAsk q = new SparqlAsk(SemanticModel.IOT_PREFIXES, URI+" ?p ?o");
		return this.askDataset(q.toString());
	}

	@Override
	public void addObservationToSensor(String obsURI, String sensorURI)
	{
		obsURI = SemanticModel.formatURIForSparql(obsURI);
		sensorURI = SemanticModel.formatURIForSparql(sensorURI);
		String insert = "?output ssn:hasValue "+obsURI;
		String where = "?output ssn:isProducedBy "+sensorURI;
		SparqlInsertWhere q = new SparqlInsertWhere(SemanticModel.IOT_PREFIXES, insert, where);
		this.processWriteQuery(q.toString());
	}

	@Override
	public List<String> getInstancesURI(String classURI)
	{
		classURI = SemanticModel.formatURIForSparql(classURI);
		List<String> instances = new ArrayList<String>();
		String select = "?s";
		String where = "?s rdf:type/rdfs:subClassOf* "+classURI+".";
		SparqlSelect q = new SparqlSelect(select, where);
		List<Map<String, String>> qResult = this.processReadQuery(q.toString());
		if(qResult.size() == 0)
		{
			System.out.println("Aucun élément de l'ontologie n'instancie la classe "+classURI);
		}
		for(Map<String, String> answer : qResult)
		{
			instances.add(answer.get("s"));
		}
		return instances;
	}
	
	@Override
	public boolean isOfType(String conceptURI, String typeURI)
	{
		String formattedConceptURI = formatURIForSparql(conceptURI);
		String formattedTypeURI = formatURIForSparql(typeURI);
		SparqlAsk sa = new SparqlAsk(IOT_PREFIXES, formattedConceptURI+" rdf:type/(rdfs:subClassOf|owl:sameAs)* "+formattedTypeURI+".");
		return this.askDataset(sa.toString());
	}

	@Override
	public String createInstance(String label, String type) {
		long id = new Random().nextLong();
		while(this.URIExists(BASE+id))
		{
			id = new Random().nextLong();
		}
		String individualURI = SemanticModel.formatURIForSparql(BASE+id);
		String typeURI = SemanticModel.formatURIForSparql(type);
		String insert = 
				individualURI+" rdf:type "+typeURI+"; "
				+ "rdfs:label \""+label+"\".";
		SparqlInsertData q = new SparqlInsertData(IOT_PREFIXES, insert);
		this.processWriteQuery(q.toString());
		return BASE+id;
	}

	@Override
	public void addObjectPropertyToIndividual(String subjectURI,
			String propertyURI, String objectURI) {
		SparqlInsertData q = new SparqlInsertData(IOT_PREFIXES, 
				SemanticModel.formatURIForSparql(subjectURI)+
				" "+SemanticModel.formatURIForSparql(propertyURI)+
				" "+SemanticModel.formatURIForSparql(objectURI)+".");
		this.processWriteQuery(q.toString());
	}

	@Override
	public void addDataPropertyToIndividual(String subjectURI,
			String propertyURI, String data) {
		SparqlInsertData q = new SparqlInsertData(IOT_PREFIXES, 
				SemanticModel.formatURIForSparql(subjectURI)+
				" "+SemanticModel.formatURIForSparql(propertyURI)+
				" \""+data+"\".");
		this.processWriteQuery(q.toString());
	}

	@Override
	public void exportModel(String path) 
	{	
		FileWriter out = null;
		try
		{
			out = new FileWriter(path);
			this.model.write(out, "TURTLE");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasDataPropertyValue(String subjectURI,
			String dataPropertyURI, String dataValue)
	{
		subjectURI = SemanticModel.formatURIForSparql(subjectURI);
		dataPropertyURI = SemanticModel.formatURIForSparql(dataPropertyURI);
		String ask = subjectURI+" "+dataPropertyURI+" \""+dataValue+"\".";
		SparqlAsk q = new SparqlAsk(IOT_PREFIXES, ask);
		return this.askDataset(q.toString());
	}
	
	public List<String> testLanguageTag(String label, String tag)
	{
		List<String> instances = new ArrayList<String>();
		String select = "?s ?l";
		String where;
//		if(tag != null && tag.length() > 0)
//		{
//			where = "?s rdfs:label \""+label+"@"+tag+"\".";
//		}
//		else
		{
//			where = "?s rdfs:label \""+label+"\".";
//			where = "?s rdfs:label ?l. FILTER (str(?l) = \""+label+"\")";
			where = "?s rdfs:label ?l.";
		}
		SparqlSelect q = new SparqlSelect(select, where);
		String query = ""
				+ "SELECT ?s"
				+ "WHERE {?s <http://www.w3.org/2000/01/rdf-schema#label> ?l. FILTER regex(?l, \""+label+"\")}";
		List<Map<String, String>> qResult = this.processReadQuery(query);
		for(Map<String, String> answer : qResult)
		{
			instances.add(answer.get("s"));
		}
		return instances;
	}

	@Override
	public List<String> getEntityURI(String label) 
	{
		List<String> instances = new ArrayList<String>();
		String query = ""
				+ "SELECT ?s "
				+ "WHERE {?s <http://www.w3.org/2000/01/rdf-schema#label> ?l. FILTER regex(?l, \""+label+"\")}";
		List<Map<String, String>> qResult = this.processReadQuery(query);
		for(Map<String, String> answer : qResult)
		{
			instances.add(answer.get("s"));
		}
		if(instances.size() == 0)
			System.out.println("Il n'y a pas d'entité portant le label "+label);
		return instances;
	}

	@Override
	public List<String> listLabels(String entityURI)
	{
		entityURI = SemanticModel.formatURIForSparql(entityURI);
		List<String> instances = new ArrayList<String>();
		String query = ""
				+ "SELECT ?l "
				+ "WHERE {"+entityURI+" <http://www.w3.org/2000/01/rdf-schema#label> ?l.}";
		List<Map<String, String>> qResult = this.processReadQuery(query);
		for(Map<String, String> answer : qResult)
		{
			// Labels contain language tags, in our case we won't consider them.
			instances.add(answer.get("l").split("@")[0]);
		}
		if(instances.size() == 0)
			System.out.println("Aucune entité n'est associée à l'entité "+entityURI);
		return instances;
	}

	@Override
	public List<List<String>> listProperties(String entityURI)
	{
		entityURI = SemanticModel.formatURIForSparql(entityURI);
		List<List<String>> instances = new ArrayList<List<String>>();
		String query = ""
				+ "SELECT ?p ?o "
				+ "WHERE {"+entityURI+" ?p ?o.}";
		List<Map<String, String>> qResult = this.processReadQuery(query);
		for(Map<String, String> answer : qResult)
		{
			ArrayList<String> l = new ArrayList<String>();
			l.add(answer.get("p"));
			l.add(answer.get("o"));
			instances.add(l);
		}
		return instances;
	}

	@Override
	public boolean hasObjectProperty(String subjectURI, String propertyURI,
			String objectURI)
	{
		subjectURI = SemanticModel.formatURIForSparql(subjectURI);
		propertyURI = SemanticModel.formatURIForSparql(propertyURI);
		objectURI = SemanticModel.formatURIForSparql(objectURI);
		String ask = subjectURI+" "+propertyURI+" "+objectURI+".";
		SparqlAsk q = new SparqlAsk(IOT_PREFIXES, ask);
		return this.askDataset(q.toString());
	}

	@Override
	public boolean hasSensorDoneIt(String obsURI, String sensorURI)
	{
		obsURI = SemanticModel.formatURIForSparql(obsURI);
		sensorURI = SemanticModel.formatURIForSparql(sensorURI);
		String ask = "?output ssn:isProducedBy "+sensorURI+";"
				+ "ssn:hasValue "+obsURI+".";
		SparqlAsk q = new SparqlAsk(SemanticModel.IOT_PREFIXES, ask);
		return this.askDataset(q.toString());
	}
}
