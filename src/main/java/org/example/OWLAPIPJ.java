package org.example;

import org.apache.jena.base.Sys;
import org.apache.jena.graph.*;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfs.GraphRDFS;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.GraphValidation;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.validation.ShaclPlainValidator;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.CONFIG;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.semanticweb.owlapi.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.rdf.rdfxml.parser.TranslatedUnloadableImportException;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import org.semanticweb.HermiT.*;

import org.eclipse.rdf4j.sail.shacl.*;

import java.io.*;

public class OWLAPIPJ {


    public static void main(String[] args)  {
        String filename="dla";
        String iri= "http://www.semanticweb.org/15295/ontologies/2023/1/"+filename;
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file= new File("src/main/resources/"+filename+".owl");
        File Aboxfile=new File("src/main/resources/"+filename+"Abox.xml");
        File Tboxfile=new File("src/main/resources/"+filename+"Tbox.xml");
        System.out.println(file.getAbsolutePath());
        OWLOntology onto;

        try {
            onto = man.loadOntologyFromOntologyDocument(file);
            OWLDataFactory df=man.getOWLDataFactory();
            OWLReasonerFactory rf =new ReasonerFactory();
            assert(onto!=null);

            //** seperate TBOX and ABOX
            IRI Aboxiri= IRI.create("http://www.semanticweb.org/"+filename+"/Abox");
            IRI Tboxiri= IRI.create("http://www.semanticweb.org/"+filename+"/Tbox");
            OWLOntology Abox=man.createOntology(Aboxiri);
            OWLOntology Tbox=man.createOntology(Tboxiri);
            Abox.addAxioms(onto.getABoxAxioms(Imports.INCLUDED).stream());
            Tbox.addAxioms(onto.getTBoxAxioms(Imports.INCLUDED).stream());
            man.saveOntology(Abox,new RDFXMLDocumentFormat(),new FileOutputStream(Aboxfile));
            man.saveOntology(Tbox,new RDFXMLDocumentFormat(),new FileOutputStream(Tboxfile));



//              ** Running Hermit *
            Configuration configuration=new Configuration();
            configuration.reasonerProgressMonitor = new ReasonerProgressMonitor() {

                public long start = 0 ;

                @Override
                public void reasonerTaskStarted(String taskName) {
                    start = System.currentTimeMillis();
                    System.out.println(taskName + " : Started");

                }

                @Override
                public void reasonerTaskStopped() {
                    long end = System.currentTimeMillis();
                    System.out.println("Time elapsed :"+ (end-start) + "ms");
                }
            };

            OWLReasoner r = rf.createReasoner(onto,configuration);
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY,InferenceType.CLASS_ASSERTIONS);

            Model shapeModel= ModelFactory.createDefaultModel();
            shapeModel.read("src/main/resources/"+filename+"-shapes.ttl");
            Shapes shapes= Shapes.parse(shapeModel);
            Graph data = RDFDataMgr.loadGraph("src/main/resources/"+filename+".owl", RDFFormat.RDFXML.getLang());
            ShaclValidator validator=new ShaclPlainValidator();
            ValidationReport validationReport= validator.validate(shapes,data);


            if (validationReport.conforms()) {
                System.out.println("Ontology conforms to SHACL shapes.");
            } else {
                System.out.println("Validation violations:");
                ShLib.printReport(validationReport);
            }







        }
        catch (Exception e ){
            e.printStackTrace();
        }

    }



}

