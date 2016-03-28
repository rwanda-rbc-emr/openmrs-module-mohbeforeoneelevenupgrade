package org.openmrs.module.mohbeforeoneelevenupgrade;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.GlobalProperty;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class PrepareToUpgradeOrderDoseUnitsAndFrequenciesTest extends BaseModuleContextSensitiveTest {
	
	private ConceptService conceptService = null;
	
	private PrepareToUpgradeOrderDoseUnitsAndFrequencies prepareInstance = null;
	
	@Before
	public void setupDatabase() throws Exception {
		conceptService = Context.getConceptService();
		prepareInstance = new PrepareToUpgradeOrderDoseUnitsAndFrequencies();
		
		executeDataSet("Concept-More-data.xml");
	}
	
	/**
	 * @see {@link PrepareToUpgradeOrderDoseUnitsAndFrequencies#addConceptSetMember(String, Concept)}
	 */
	@Test
	@Verifies(value = "should add a concept set member to an existing concept set", method = "addConceptSetMember(String, Concept)")
	public void addConceptSetMember_shouldWorkAsExpected() {
		Concept set = conceptService.getConceptByUuid("87560c3e-8fdd-44c3-9bf8-3be9c6d7c241");//dose units concept
		Concept member1 = prepareInstance.createConcept("kg", "UUID1", true);
		Concept member2 = prepareInstance.createConcept("ml", "UUID2", true);
		
		Assert.assertNotNull(set);
		Assert.assertTrue(set.isSet());
		Assert.assertEquals(set.getConceptSets().size(), 0);
		
		set = prepareInstance.addConceptSetMember(set, member1, 0.0);
		
		Assert.assertEquals(set.getConceptSets().size(), 1);
		
		set = prepareInstance.addConceptSetMember(set, member2, 1.0);
		
		Assert.assertEquals(set.getConceptSets().size(), 2);
		
		List<ConceptSet> doseunits = conceptService.getConceptSetsByConcept(set);
		
		Assert.assertTrue(doseunits.get(0).getConcept().getName().getName().equals("kg"));
		Assert.assertTrue(doseunits.get(0).getConceptSet().getName().getName().equals(set.getName().getName()));
		Assert.assertTrue(doseunits.get(1).getConcept().getName().getName().equals("ml"));
		Assert.assertTrue(doseunits.get(1).getConceptSet().getName().getName().equals(set.getName().getName()));
	}
	
	/**
	 * @see {@link PrepareToUpgradeOrderDoseUnitsAndFrequencies#createConcept(String, String, boolean)}
	 */
	@Test
	@Verifies(value = "should create and return created concept", method = "createConcept(String, String, boolean)")
	public void createConcept_shouldWorkAsExpected() {
		Concept createdConcept = prepareInstance.createConcept("Concept Name", "UUID", false);
		Assert.assertNotNull(createdConcept);
		Assert.assertEquals("UUID", createdConcept.getUuid());
		Assert.assertEquals("Concept Name", createdConcept.getName().getName());
		Assert.assertFalse(createdConcept.isSet());
		
		Concept createdConceptSet = prepareInstance.createConcept("Concept Set Name", "UUID2", true);
		Assert.assertNotNull(createdConceptSet);
		Assert.assertTrue(createdConceptSet.isSet());
	}
	
	@Test
	public void testReplacingSpaceToPrependBackSlash() {
		Assert.assertEquals("hi\\ there", "hi there".replace(" ", "\\ "));
	}
	
	/**
	 * @see {@link PrepareToUpgradeOrderDoseUnitsAndFrequencies#addDoseUnitOrFrequencyEntryToSettingFileContent(String, Integer)}
	 */
	@Test
	@Verifies(value = "only add an entry when it needs to", method = "addDoseUnitOrFrequencyEntryToSettingFileContent(String, Integer)")
	public void addDoseUnitOrFrequencyEntryToSettingFileContent_shouldWorkAright() {
		String fileContent = prepareInstance.addDoseUnitOrFrequencyEntryToSettingFileContent("mg", 13);
		fileContent = prepareInstance.addDoseUnitOrFrequencyEntryToSettingFileContent("ounces", 46);
		fileContent = prepareInstance.addDoseUnitOrFrequencyEntryToSettingFileContent("once a day", 65);
		
		Assert.assertEquals("mg=13\nounces=46\nonce\\ a\\ day=65\n", fileContent);
		
		fileContent = prepareInstance.removeLastOccurencyOf(fileContent, "\n");
		
		Assert.assertEquals("mg=13\nounces=46\nonce\\ a\\ day=65", fileContent);
	}
	
	@Test
	public void addStringToStringsList_shouldWorkAsExpected() {
		List<String> list = new ArrayList<String>();
		String string = "test";
		
		list.add("string1");
		
		Assert.assertEquals(1, list.size());
		prepareInstance.addStringToStringsList(list, string);
		Assert.assertEquals(2, list.size());
		
		Assert.assertEquals("string1", list.get(0));
		Assert.assertEquals("test", list.get(1));
		Assert.assertEquals(2, list.size());
		prepareInstance.addStringToStringsList(list, "string1");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("string1", list.get(0));
		Assert.assertEquals("test", list.get(1));
	}
	
	@Test
	public void updateGlobalProperty_shouldworkAsExpected() {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject("locale.allowed.list");
		
		Assert.assertEquals("en", gp.getPropertyValue());
		
		prepareInstance.updateOrSaveNewGlobalProperty("locale.allowed.list", "fr");
		
		Assert.assertEquals("fr", gp.getPropertyValue());
	}
}
