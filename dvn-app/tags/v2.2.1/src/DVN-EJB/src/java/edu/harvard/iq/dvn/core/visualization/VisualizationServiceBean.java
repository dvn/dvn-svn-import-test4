/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dvn.core.visualization;

import edu.harvard.iq.dvn.core.study.DataTable;
import edu.harvard.iq.dvn.core.study.DataVariable;
import edu.harvard.iq.dvn.core.study.Study;
import edu.harvard.iq.dvn.core.visualization.VarGrouping.GroupingType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 *
 * @author skraffmiller
 */
@Stateful
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VisualizationServiceBean implements VisualizationServiceLocal {


    @PersistenceContext(type = PersistenceContextType.EXTENDED,unitName="VDCNet-ejbPU")
    EntityManager em;

    DataTable dt;

        /**
     *  Initialize the bean with a dataTable for editing
     */
    public void setDataTable(Long dataTableId ) {
        dt = em.find(DataTable.class,dataTableId);
        if (dt==null) {
            throw new IllegalArgumentException("Unknown data table id: "+dataTableId);
        }
    }

        @Override
    public void setDataTableFromStudyFileId(Long studyFileId) {
        String query = "SELECT d FROM  DataTable d where d.studyFile.id = " + studyFileId + "  ";
        dt = (DataTable) em.createQuery(query).getSingleResult();
        if (dt==null) {
            throw new IllegalArgumentException("Unknown data table  with study file id: "+studyFileId);
        }
    }

    @Override
    public Study getStudyFromStudyFileId(Long studyFileId) {
        String query = "SELECT s FROM  Study s, StudyFile f where s.id = " +
                "f.study.id and f.id = " + studyFileId + "  ";
        Study study = (Study) em.createQuery(query).getSingleResult();
        if (study==null) {
            throw new IllegalArgumentException("Unknown data table  with study file id: "+studyFileId);
        }
        return study;
    }

    public DataTable getDataTable( ) {
        return dt;
    }

    @Override
    public List <VarGrouping> getGroupings(Long dataTableId) {

        String query = "SELECT g FROM  VarGrouping g where g.dataTable.id = " + dataTableId + "  ORDER BY g.id";
        return (List) em.createQuery(query).getResultList();

    }

    @Override
    public List <VarGroupType> getGroupTypes(Long dataTableId) {

        String query = "SELECT v FROM  VarGroupType v, VarGrouping g where g.dataTable.id = " + dataTableId + " and g.id = v.varGrouping.id  ORDER BY g.id";
        return (List) em.createQuery(query).getResultList();

    }

    @Override
    public List <VarGroupType> getFilterGroupTypes(Long dataTableId) {

        String query = "SELECT v FROM  VarGroupType v, VarGrouping g where g.dataTable.id = " + dataTableId + " and g.id = v.varGrouping.id and v.varGrouping.groupingType = v.GroupingType.FILTER  ORDER BY g.id";
        return (List) em.createQuery(query).getResultList();

    }

    @Override
    public void updateGroupings(List<VarGrouping> groupings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean validateGroupings(Long dataTableId) {
        List <VarGrouping> groupings = getGroupings(dataTableId);
        return true;
    }

    @Override
    public List getVariableMappings(Long dataTableId) {

        String query = "SELECT m FROM  DataVariableMapping m where m.datatable.id = " + dataTableId + "  ORDER BY m.datavariable.id";
        return (List) em.createQuery(query).getResultList();

    }

    @Override
    public boolean validateOneMeasureMapping(DataTable dataTable, List returnListOfErrors){
        boolean valid = true;
       int countMeasures = 0;
       boolean hasMappings = false;
       boolean xAxis = false;
        List variableMappings = new ArrayList();
        List<DataVariable> dataVariables = (List<DataVariable>) dataTable.getDataVariables();

            for (DataVariable dataVariable: dataVariables){


                xAxis = false;
                hasMappings = false;
                countMeasures = 0;
                variableMappings = (List) dataVariable.getDataVariableMappings();
                if (!variableMappings.isEmpty()){
                    hasMappings = true;
                    Iterator iteratorMap = variableMappings.iterator();

                    while (iteratorMap.hasNext()) {
                        DataVariableMapping dataVariableMapping = (DataVariableMapping) iteratorMap.next();

                        if (dataVariableMapping.isX_axis()) xAxis = true;
                        if (!xAxis && dataVariableMapping.getVarGrouping() != null &&
                            dataVariableMapping.getVarGrouping().getGroupingType().equals(GroupingType.MEASURE)){
                            countMeasures++;
                        }
                    }
                }
                if (!xAxis && hasMappings && countMeasures != 1){
                    returnListOfErrors.add(dataVariable);
                    valid = false;
                }
                else{
                    xAxis = false;
                    hasMappings = false;
                    countMeasures = 0;
                }


        }




        return valid;
    }

    @Override
    public boolean validateMoreThanZeroMeasureMapping(DataTable dataTable, List returnListOfErrors){
        boolean valid = true;
       int countMeasures = 0;
       boolean hasMappings = false;
       boolean xAxis = false;
        List variableMappings = new ArrayList();
        List<DataVariable> dataVariables = (List<DataVariable>) dataTable.getDataVariables();
        List <DataVariable>  errorVariables = new ArrayList();
            for (DataVariable dataVariable: dataVariables){


                xAxis = false;
                hasMappings = false;
                countMeasures = 0;
                variableMappings = (List) dataVariable.getDataVariableMappings();
                if (!variableMappings.isEmpty()){
                    hasMappings = true;
                    Iterator iteratorMap = variableMappings.iterator();

                    while (iteratorMap.hasNext()) {
                        DataVariableMapping dataVariableMapping = (DataVariableMapping) iteratorMap.next();

                        if (dataVariableMapping.isX_axis()) xAxis = true;
                        if (!xAxis && dataVariableMapping.getVarGrouping() != null &&
                            dataVariableMapping.getVarGrouping().getGroupingType().equals(GroupingType.MEASURE)){
                            countMeasures++;
                        }
                    }
                }
                if (!xAxis && hasMappings && countMeasures == 0){
                    errorVariables.add(dataVariable);
                    valid = false;
                }
                else{
                    xAxis = false;
                    hasMappings = false;
                    countMeasures = 0;
                }


        }

        if (!errorVariables.isEmpty()){
            for (DataVariable dv : errorVariables){
                variableMappings = (List) dv.getDataVariableMappings();
                if (!variableMappings.isEmpty()){
                    Iterator iteratorMap = variableMappings.iterator();

                    while (iteratorMap.hasNext()) {
                        DataVariableMapping dataVariableMapping = (DataVariableMapping) iteratorMap.next();
                        returnListOfErrors.add(dv);
                        returnListOfErrors.add(dataVariableMapping);
                    }

                }


            }

        }



        return valid;
    }


    private boolean validateSingleVariableMeasureGroup(DataTable dataTable, DataVariable dataVariable){

       int countMeasures = 0;
       boolean hasMappings = false;
       boolean xAxis = false;
       List variableMappings = new ArrayList();
       VarGroup varGroupChk = new VarGroup();
       xAxis = false;
       hasMappings = false;
       countMeasures = 0;
       variableMappings = (List) dataVariable.getDataVariableMappings();

       if (!variableMappings.isEmpty()){
            hasMappings = true;
            Iterator iteratorMap = variableMappings.iterator();

            while (iteratorMap.hasNext()) {
                DataVariableMapping dataVariableMapping = (DataVariableMapping) iteratorMap.next();
                if (!xAxis && dataVariableMapping.getVarGrouping() != null &&
                    dataVariableMapping.getVarGrouping().getGroupingType().equals(GroupingType.MEASURE)){
                    varGroupChk = dataVariableMapping.getGroup();
                }
            }
        }

        List dataVariables = new ArrayList();
        dataVariables = dataTable.getDataVariables();
        if (!dataVariables.isEmpty())
        {
            Iterator iterator = dataVariables.iterator();
            while (iterator.hasNext()) {
                DataVariable dataVariableTest = (DataVariable) iterator.next();
                variableMappings = (List) dataVariableTest.getDataVariableMappings();
                for (Object dvMapping: variableMappings ){
                    DataVariableMapping dvMappingTest = (DataVariableMapping) dvMapping;
                    if (varGroupChk.equals(dvMappingTest.getGroup())){
                        countMeasures++;
                    }

                }

            }
            if (countMeasures > 1){
                return false;
            }
        }


        return true;
    }

    @Override
    public boolean validateXAxisMapping(DataTable dataTable, Long xAxisVariableId){

        List variableMappings = new ArrayList();
        List dataVariables = new ArrayList();

        dataVariables = dataTable.getDataVariables();
        if (!dataVariables.isEmpty())
        {
            Iterator iteratorV = dataVariables.iterator();
            while (iteratorV.hasNext()) {
                DataVariable dataVariable = (DataVariable) iteratorV.next();
                if (dataVariable.getId().equals(xAxisVariableId)){
                    variableMappings = (List) dataVariable.getDataVariableMappings();
                        if (variableMappings.size() != 1 ){
                            return false;
                        }
                }

            }
        }

        return true;
    }


    @Override
    public boolean validateAtLeastOneFilterMapping(DataTable dataTable, List returnListOfErrors){

       int countFilters = 0;
       boolean xAxis = false;
       boolean hasMappings = false;

        List<DataVariable> dataVariables = (List<DataVariable>) dataTable.getDataVariables();

            for (DataVariable dataVariable: dataVariables){
                
                xAxis = false;
                hasMappings = false;
                countFilters = 0;
                List <DataVariableMapping> variableMappings =  (List<DataVariableMapping>) dataVariable.getDataVariableMappings();
                for (DataVariableMapping dataVariableMapping: variableMappings){
                    hasMappings = true;

                    if (dataVariableMapping.isX_axis()) xAxis = true;
                    if (!xAxis && dataVariableMapping.getVarGrouping().getGroupingType().equals(GroupingType.FILTER)){
                        countFilters++;
                    }

                }

                    if (hasMappings  && !xAxis && !validateSingleVariableMeasureGroup(dataTable, dataVariable)){
                        if (countFilters < 1){
                            returnListOfErrors.add(dataVariable);
                            return false;
                        }
                        else{
                            xAxis = false;
                            hasMappings = false;
                            countFilters = 0;
                        }
                    }

            }

        return true;
    }

    /**
     * Test that for each variable, that there is  defined set of filters
     * such that the variable can be individually chosen in the Explore data page
     *
     * @return a list of variables that are not uniquely identified by measure & filters
     */

    public List getDuplicateMappings( DataTable datatable, List returnListOfErrors ) {
        List duplicateVariables = new ArrayList();
        Set<ArrayList<String>> set = new HashSet();
        Set<String> setString = new HashSet();
        List<DataVariable> variables = datatable.getDataVariables();
        List<VarGrouping> varGroupings = datatable.getVarGroupings();
        for (VarGrouping vg : varGroupings) {
            System.out.println(" varGrouping Name: "+ vg.getName());
        }
        for (DataVariable var: variables) {
            if (!var.getDataVariableMappings().isEmpty()){
                System.out.println("\n\n\nvar="+var.getName());
                ArrayList<String> groupMembership = getGroupMembership(var,varGroupings);
                String groups = "";
                for (String s: groupMembership) {
                    groups +=" "+s;
                }
                groups+=".";

                if (groupMembership.size() >0) {
                    Set <ArrayList<String>> groupSet = new HashSet();

                    groupSet.add(groupMembership);
                    System.out.println("\n\n\nvar="+var.getName()+", groupMembership = "+ groupMembership);
                    System.out.println("\n\n\nvar="+var.getName()+" group membership size > 0 ");
                    System.out.println("\n\n\nvar="+var.getName()+", set = "+set);

                    if (set.contains(groupMembership)) {
                        System.out.println("set contains groupMembership");
                        duplicateVariables.add(var);
                        returnListOfErrors.add(groupMembership);
                    } else {
                        set.add(groupMembership);
                        for(String groupString: groupMembership){
                            setString.add(groupString);
                        }
                    }
                }

            }

        }
        System.out.println("duplicate variables size=" + duplicateVariables.size());
        return duplicateVariables;
    }
    /*
     * For each grouping, get the group that this var belongs to, if any
     */
    private ArrayList<String> getGroupMembership(DataVariable var, List<VarGrouping> groupings ) {

        ArrayList<String> membership = new ArrayList<String>();
        System.out.println("var= "+var.getName());
        for (VarGrouping grouping : groupings) {
            System.out.println("grouping="+grouping.getName());
            for (DataVariableMapping dvm : grouping.getDataVariableMappings()) {
                System.out.println(" dvm - group "+ dvm.getGroup().getName());
                if (dvm.getDataVariable().getId().equals(var.getId())) {
                    membership.add(dvm.getGroup().getName());
                    System.out.println("var= "+var.getName());
                    System.out.println("adding group "+ dvm.getGroup().getName());
                }
            }
        }
        return membership;
    }

    private List getUniqueMappedVariables(Long dataTableId){
        String query = "SELECT distinct m.dataVariable.id FROM  datavariablemapping m where m.datatable.id = " + dataTableId + "  ORDER BY m.dataVariable.id";
        return (List) em.createQuery(query).getResultList();

    }

    private List getVariableMappingsById(Long dataVariableId){
        String query = "SELECT  m FROM  datavariablemapping m where m.dataVariable.id = " + dataVariableId + "  ";
        return (List) em.createQuery(query).getResultList();

    }

    private boolean getAreVariableMappingsDifferent(List <DataVariableMapping> mapping1, List <DataVariableMapping> mapping2){

        if (mapping1.size() != mapping2.size()){
            return true;
        }

        if (!getMeasureGroupIdFromMapping(mapping1).equals(getMeasureGroupIdFromMapping(mapping2)) ){
            return true;
        }

        Iterator iterator1 = mapping1.iterator();
        Iterator iterator2 = mapping2.iterator();
        int countMatches = 0;
        int compareMappings = mapping1.size();
        while (iterator1.hasNext()){
             DataVariableMapping dataVariableMapping = (DataVariableMapping) iterator1.next();
             VarGroup group1 = (VarGroup) dataVariableMapping.getGroup();
             while (iterator2.hasNext()){
                DataVariableMapping dataVariableMapping2 = (DataVariableMapping) iterator2.next();
                VarGroup group2 = (VarGroup) dataVariableMapping2.getGroup();
                if (group1.equals(group2)){
                    countMatches++;
                }
             }
        }

        if (countMatches != compareMappings){
            return true;
        }

        return false;
    }

    private Long getMeasureGroupIdFromMapping(List <DataVariableMapping> mapping){
        Iterator iterator = mapping.iterator();
        while (iterator.hasNext() ){
            DataVariableMapping dataVariableMapping = (DataVariableMapping) iterator.next();

            if (dataVariableMapping.getVarGrouping().getGroupingType().equals(GroupingType.MEASURE)){
                return dataVariableMapping.getGroup().getId();
            }

        }
        return new Long(0);
    }

    @Override
    public List getGroupsFromGroupTypeId(Long groupTypeId) {

        String query = "SELECT g.varGroups FROM  VarGroupType g where g.id = " + groupTypeId + "  ORDER BY g.name";
        return (List) em.createQuery(query).getResultList();
    }

    

    @Override
    public List getGroupTypesFromGroupingId(Long groupingId) {
        String query = "SELECT g.varGroupTypes FROM  VarGrouping g where g.id = " + groupingId + "  ORDER BY g.name";
        return (List) em.createQuery(query).getResultList();
    }

    @Override
    public VarGrouping getGroupingFromId(Long groupingId) {
        String query = "SELECT g FROM  VarGrouping g where g.id = " + groupingId + "  ORDER BY g.name";
        return (VarGrouping) em.createQuery(query).getSingleResult();
    }

    @Override
    public VarGroup getGroupFromId(Long groupId) {
        String query = "SELECT g FROM  VarGroup g where g.id = " + groupId + "  ORDER BY g.name";
        return (VarGroup) em.createQuery(query).getSingleResult();

    }

    @Override
    public DataVariable getXAxisVariable(Long dataTableId) {
        String query = "SELECT v FROM  DataVariableMapping m, DataVariable v where v.id = m.dataVariable.id " +
                " and m.x_axis = true " +
                " and m.dataTable.id = " + dataTableId + "  ORDER BY v.id";
        if (em.createQuery(query).getResultList().isEmpty()) {
            DataVariable dataVariable = new DataVariable();
            dataVariable.setId(new Long (0));
            return dataVariable;
        }

        return (DataVariable) em.createQuery(query).getSingleResult();
    }

    @Override
    public List getDataVariableMappingsFromGroupId(Long groupId) {
        if (groupId != null) {
            String query = "SELECT v FROM  DataVariableMapping m, DataVariable v where v.id = m.dataVariable.id " +
                " and m.varGroup.id = " + groupId + "  ORDER BY v.id";
            return (List) em.createQuery(query).getResultList();
        } else {
            return new ArrayList();
        }
    }

    @Override
    public List getGroupTypesFromGroupId(Long groupId) {
        String query = "SELECT g.groupTypes FROM  VarGroup g where g.id = " + groupId + "  ORDER BY g.name";
        return (List) em.createQuery(query).getResultList();
    }

    @Override
    public List getFilterGroupsFromMeasureId(Long measureId) {
        String query = "SELECT distinct g FROM  DataVariableMapping m, DataVariableMapping m2, VarGroup g where " +
                " m.varGroup.id = " + measureId + "  " +
                " and  m.dataVariable.id = m2.dataVariable.id " +
                " and m2.varGroup.id = g.id and g.id <> " + measureId + "  ORDER BY g.name";
        if (em.createQuery(query).getResultList() != null){
            return (List) em.createQuery(query).getResultList();
        } else {
            return new ArrayList();
        }

    }

    @Override
    public List getFilterGroupingsFromMeasureId(Long measureId) {

        String query = "select  DISTINCT po from VarGrouping po, VarGroup g2 " +
                " where g2.varGrouping.id = po.id  " +
                "  AND g2.id in ( " +
                " select distinct m2.varGroup.id from DataVariableMapping m2 " +
                " where m2.dataVariable.id in " +
                "   ( " +
                "  SELECT DISTINCT M.dataVariable.id FROM  DataVariableMapping m, DataVariable v, VarGroup g, VarGrouping p " +
                " where v.id = m.dataVariable.id " +
                " and m.varGroup.id = " + measureId + "  " +
                " and g.varGrouping.id = p.id " +
                " and m.varGroup.id = g.id ) ) ";
        List checkList = (List) em.createQuery(query).getResultList();
        return (List) checkList;
    }

    @Override
    public List getFilterMeasureAssociationFromDataTableId(Long dataTableId) {

        String query = "select  DISTINCT po.id, g2.id from VarGrouping po, VarGroup g2 " +
                " where g2.varGrouping.id = po.id  " +
                "  AND g2.id in ( " +
                " select distinct m2.varGroup.id from DataVariableMapping m2 " +
                " where m2.dataVariable.id in " +
                "   ( " +
                "  SELECT DISTINCT M.dataVariable.id FROM  DataVariableMapping m, DataVariable v, VarGroup g, VarGrouping p " +
                " where v.id = m.dataVariable.id " +
                " and m.dataTable.id = " + dataTableId + "  " +
                " and g.varGrouping.id = p.id " +
                " and m.varGroup.id = g.id ) ) ";
        List checkList = (List) em.createQuery(query).getResultList();
        return (List) checkList;
    }


    public void removeCollectionElement(Collection coll, Object elem) {
        coll.remove(elem);
        em.remove(elem);
    }

    public void removeCollectionElement(List list,int index) {
        System.out.println("index is "+index+", list size is "+list.size());
        em.remove(list.get(index));
        list.remove(index);
    }

    public void removeCollectionElement(Iterator iter, Object elem) {
        iter.remove();
        em.remove(elem);
    }

    @Remove
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void saveAll() {


    }

    @Remove
    public void cancel() {

    }

    @Override
    public boolean validateAtLeastOneMeasureMapping(DataTable dataTable) {
       int countMeasures = 0;

        List variableMappings = new ArrayList();
        List dataVariables = new ArrayList();
        dataVariables = dataTable.getDataVariables();
        Iterator iteratorV = dataVariables.iterator();
            while (iteratorV.hasNext()) {
                DataVariable dataVariable = (DataVariable) iteratorV.next();
                variableMappings = (List) dataVariable.getDataVariableMappings();
                Iterator iterator = variableMappings.iterator();
                while (iterator.hasNext()) {

                    DataVariableMapping dataVariableMapping = (DataVariableMapping) iterator.next();
                    if (dataVariableMapping.getVarGrouping() != null && dataVariableMapping.getVarGrouping().getGroupingType().equals(GroupingType.MEASURE)){
                        countMeasures++;
                    }
                }
            }
            if (countMeasures < 1){
                return false;
            }

        return true;
    }



    @Override
    public List getDataVariableMappingsFromDataTableGroup(DataTable dataTable, VarGroup varGroup) {

        List <DataVariableMapping> variableMappings = new ArrayList();
        List <DataVariable> dataVariables = dataTable.getDataVariables();
        List <DataVariable> returnDataVariables = new ArrayList();


        for (DataVariable dataVariable : dataVariables ){
            variableMappings = (List) dataVariable.getDataVariableMappings();
            for (DataVariableMapping dataVariableMapping: variableMappings){

                if (!dataVariableMapping.isX_axis() && dataVariableMapping.getGroup().equals(varGroup)){
                    returnDataVariables.add(dataVariable);
                }

            }

        }


        return returnDataVariables;
    }

    @Override
    public boolean checkForDuplicateEntries(VarGrouping varGrouping, String name, boolean group, Object testObject) {

        boolean hasDuplicates = false;

        List<String> set = new ArrayList();
        List checkList = new ArrayList();
        List editList = new ArrayList();

        if (group){
           checkList =  varGrouping.getVarGroups();

        } else {
           checkList = (List)  varGrouping.getVarGroupTypes();
        }

        for (Object obj: checkList) {
            if (group) {
                VarGroup varGroup = (VarGroup) obj;
                set.add(varGroup.getName());
                editList.add(varGroup);
            } else {
                VarGroupType varGroupType = (VarGroupType) obj;
                set.add(varGroupType.getName());
                editList.add(varGroupType);
            }
        }
        
        if (set.contains(name)) {
            hasDuplicates = true;
        }
        if (testObject!=null){
            if (group){
               VarGroup varGroupTest = (VarGroup) testObject;
               for(Object listObj: editList){
                   VarGroup varGroup = (VarGroup) listObj;

                   boolean checkForMatch =  (varGroup.getId() != null && varGroupTest.getId() != null ) && varGroup.getId().equals(varGroupTest.getId() ) ;
                   checkForMatch |= (varGroup.getId() == null && varGroupTest.getId() == null );
                   if (checkForMatch  && name.equals(varGroup.getName())){
                       hasDuplicates = false;
                   }
               }

            } else {

               VarGroupType  varGroupTypeTest = (VarGroupType) testObject;
               for(Object listObj: editList){
                   VarGroupType varGroupType = (VarGroupType) listObj;
                   boolean checkForMatch =  (varGroupType.getId() != null && varGroupTypeTest.getId() != null ) && varGroupType.getId().equals(varGroupTypeTest.getId() ) ;
                   checkForMatch |= (varGroupType.getId() == null && varGroupTypeTest.getId() == null );
                   if (checkForMatch  && name.equals(varGroupType.getName())){
                       hasDuplicates = false;
                   }
               }

            }


        }


        return hasDuplicates;
    }

    @Override
    public boolean checkForDuplicateGroupings(List filterGroupings, String name, Object testObject) {
    boolean hasDuplicates = false;

        List<String> set = new ArrayList();
        List editList = new ArrayList();

        for (Object filterGrouping: filterGroupings ){
            VarGrouping varGrouping = (VarGrouping) filterGrouping;
            set.add(varGrouping.getName());
            editList.add(varGrouping);
        }


        if (set.contains(name)) {
            hasDuplicates = true;
        }
        if (testObject!=null){

               VarGrouping varGroupingTest = (VarGrouping) testObject;
               for(Object listObj: editList){
                   VarGrouping varGrouping = (VarGrouping) listObj;

                   boolean checkForMatch =  (varGrouping.getId() != null && varGroupingTest.getId() != null ) && varGrouping.getId().equals(varGroupingTest.getId() ) ;
                   checkForMatch |= (varGrouping.getId() == null && varGroupingTest.getId() == null );
                   if (checkForMatch  && name.equals(varGrouping.getName())){
                       hasDuplicates = false;
                   }
               }

         }

        return hasDuplicates;
    }


}
