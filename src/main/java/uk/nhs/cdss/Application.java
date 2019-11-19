package uk.nhs.cdss;

import java.util.Arrays;

import org.hl7.fhir.dstu3.model.CareConnectCarePlan;
import org.hl7.fhir.dstu3.model.CareConnectCareTeam;
import org.hl7.fhir.dstu3.model.CareConnectCondition;
import org.hl7.fhir.dstu3.model.CareConnectEncounter;
import org.hl7.fhir.dstu3.model.CareConnectEpisodeOfCare;
import org.hl7.fhir.dstu3.model.CareConnectHealthcareService;
import org.hl7.fhir.dstu3.model.CareConnectLocation;
import org.hl7.fhir.dstu3.model.CareConnectMedication;
import org.hl7.fhir.dstu3.model.CareConnectObservation;
import org.hl7.fhir.dstu3.model.CareConnectOrganization;
import org.hl7.fhir.dstu3.model.CareConnectPatient;
import org.hl7.fhir.dstu3.model.CareConnectPractitioner;
import org.hl7.fhir.dstu3.model.CareConnectProcedure;
import org.hl7.fhir.dstu3.model.CareConnectProcedureRequest;
import org.hl7.fhir.dstu3.model.CareConnectRelatedPerson;
import org.hl7.fhir.dstu3.model.CareConnectSpecimen;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;


@ServletComponentScan
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public FhirContext fhirContext() {
		FhirContext fhirContext = FhirContext.forDstu3();
		fhirContext.setDefaultTypeForProfile("http://hl7.org/fhir/StructureDefinition/CarePlan", CareConnectCarePlan.class);
	
		return fhirContext;
	}

	@Bean
	public IParser fhirParser() {
		IParser fhirParser = fhirContext().newJsonParser();
		fhirParser.setPreferTypes(Arrays.asList(
			CareConnectCarePlan.class,
			CareConnectCareTeam.class,
			CareConnectCondition.class,
			CareConnectEncounter.class,
			CareConnectEpisodeOfCare.class,
			CareConnectHealthcareService.class,
			CareConnectLocation.class,
			CareConnectMedication.class,
			CareConnectObservation.class,
			CareConnectOrganization.class,
			CareConnectPatient.class, 
			CareConnectPractitioner.class, 
			CareConnectProcedure.class,
			CareConnectProcedureRequest.class,
			CareConnectRelatedPerson.class,
			CareConnectSpecimen.class
		));
		
		return fhirParser;
	}
}