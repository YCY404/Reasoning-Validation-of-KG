package org.example;

import org.semanticweb.owlapi.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.rdf.rdfxml.parser.TranslatedUnloadableImportException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import org.semanticweb.HermiT.*;

import java.io.*;

public class OWLAPIPJ {


    public static void main(String[] args)  {
        String filename="cy";
        String iri= "http://www.semanticweb.org/15295/ontologies/2023/1/"+filename;
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file= new File("src/main/resources/"+filename+".xml");
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



              //  ** Running Hermit **
            OWLReasoner r = rf.createReasoner(onto);
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);


        }
        catch (Exception e ){
            e.printStackTrace();
        }

    }



}

